import React, { useState, useEffect, useCallback } from 'react';
import OrderTable from '../components/trading/OrderTable';
import Pagination from '../components/common/Pagination';
import ConfirmationModal from '../components/common/Modal';
import EditOrderModal from '../components/trading/EditOrderModal';
import OrderFilterPanel from '../components/trading/OrderFilterPanel';
import { getOrders, cancelOrder, updateOrder } from '../services/orderService';
import useDebounce from '../hooks/useDebounce';
import { subscribe } from '../services/websocketService';
import { toast } from 'react-toastify'; 

const styles = {
    page: { padding: '30px' },
    headerContainer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' },
    header: { fontSize: '28px', fontWeight: 'bold', color: '#333', margin: 0 },
    statsText: { fontSize: '14px', color: '#6c757d', fontWeight: '500' },
    content: { backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }
};


export default function OrderListPage() {
    const [orders, setOrders] = useState([]);
    const [pageData, setPageData] = useState({ number: 0, totalPages: 0, totalElements: 0 });
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const initialFilters = { orderCode: '', customerId: '', assetId: '', status: '', startDate: '', endDate: '', page: 0, size: 10, sort: 'createdAt,desc' };
    const [filters, setFilters] = useState(initialFilters);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [orderToCancel, setOrderToCancel] = useState(null);
    
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [orderToEdit, setOrderToEdit] = useState(null);

    const debouncedFilters = useDebounce(filters, 500);

    const fetchOrders = useCallback(async (currentFilters) => {
        setIsLoading(true);
        setError('');
        try {
            const data = await getOrders(currentFilters);
            setOrders(data.content || []);
            setPageData({ number: data.number, totalPages: data.totalPages, totalElements: data.totalElements });
        } catch (err) {
            setError('Emirler yüklenirken bir hata oluştu. Veritabanı bağlantısını kontrol edin.');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => { fetchOrders(debouncedFilters); }, [debouncedFilters, fetchOrders]);

    useEffect(() => {
        const subscription = subscribe('/topic/order-events', (eventData) => {
            console.log('Yeni emir olayı alındı:', eventData);
            updateOrderFromEvent(eventData);
        });

        return () => {
            subscription.unsubscribe();
        };
    }, []); 

    const updateOrderFromEvent = (eventData) => {
        const updatedOrder = eventData;
        
        if (updatedOrder.newStatus === 'FILLED' || updatedOrder.newStatus === 'PARTIALLY_FILLED') {
            const isBuy = updatedOrder.transactionType === 'BUY';
            const message = `$${updatedOrder.customerCode} için ${updatedOrder.executedLots} lot ${isBuy ? 'alımı' : 'satımı'} gerçekleşti.`;
            toast.success(message);
        } else if (updatedOrder.newStatus === 'CANCELLED') {
            const message = `${updatedOrder.bistCode} için ${updatedOrder.cancelledLots} lotluk emir iptal edildi.`;
            toast.info(message);
        }

        let highlightClass = '';
        if (updatedOrder.newStatus) {
             if (updatedOrder.newStatus === 'FILLED') highlightClass = 'flash-green';
             else if (updatedOrder.newStatus === 'PARTIALLY_FILLED') highlightClass = 'flash-yellow';
             else if (updatedOrder.newStatus === 'CANCELLED') highlightClass = 'flash-red';
        }

        setOrders(currentOrders => {
            const newOrders = currentOrders.map(order => {
                if (order.id === updatedOrder.orderId) {
                    return { 
                        ...order, 
                        status: updatedOrder.newStatus,
                        filledLotAmount: order.initialLotAmount - updatedOrder.remainingLots,
                        executedPrice: updatedOrder.executedPrice ?? order.executedPrice,
                        highlight: highlightClass 
                    };
                }
                return order;
            });
            return newOrders;
        });

        setTimeout(() => {
            setOrders(currentOrders =>
                currentOrders.map(order =>
                    order.id === updatedOrder.orderId ? { ...order, highlight: '' } : order
                )
            );
        }, 2000);
    };
    const handleFilterChange = (name, value) => {
        setFilters(prevFilters => ({
            ...prevFilters,
            [name]: value,
            page: 0,
        }));
    };

    const clearFilters = () => setFilters(initialFilters);

    const handlePageChange = (newPage) => {
        setFilters(prevFilters => ({ ...prevFilters, page: newPage }));
    };

    const handleCancelClick = (order) => { setOrderToCancel(order); setIsModalOpen(true); };
    
    const handleEditClick = (order) => { setOrderToEdit(order); setIsEditModalOpen(true); };

    const handleConfirmCancel = async () => {
        if (!orderToCancel) {
            console.error("İptal edilecek emir bulunamadı.");
            setIsModalOpen(false);
            return;
        }

        try {
            await cancelOrder(orderToCancel.id);
            toast.success('Emir iptal edildi.');
            await fetchOrders(filters);
        } catch (err) {
            console.error("Emir iptal hatası detayları:", err.response);

            let errorMessage = "Sistem hatası: Emir iptal edilemedi. Lütfen tekrar deneyiniz.";

            if (err.response) {
                const responseData = err.response.data;
                if (responseData && typeof responseData === 'object' && responseData.message) {
                    errorMessage = responseData.message;
                } else if (responseData && typeof responseData === 'string' && responseData.includes("Bu emri iptal etme yetkiniz yok")) {
                    errorMessage = "Bu emri iptal etme yetkiniz yok.";
                } else if (err.response.status === 403) {
                    errorMessage = "Bu işlemi yapmak için yetkiniz bulunmamaktadır.";
                } else if (err.response.status === 500) {
                    errorMessage = "Sunucuda beklenmedik bir hata oluştu. Lütfen daha sonra tekrar deneyin.";
                }
            }
            toast.error(errorMessage);

        } finally {
            setIsModalOpen(false);
            setOrderToCancel(null);
        }
    };

    const handleUpdateOrder = async (orderId, updateData) => {
        try {
            await updateOrder(orderId, updateData);
            await fetchOrders(debouncedFilters);
            setIsEditModalOpen(false);
            setOrderToEdit(null);
            toast.success('Emir başarıyla güncellendi.');
        } catch (err) {
            console.error("Emir güncelleme hatası:", err.response);
            
            let errorMessage = "Sistem hatası: Emir güncellenemedi. Lütfen tekrar deneyiniz.";
            
            if (err.response) {
                const responseData = err.response.data;
                if (responseData && typeof responseData === 'object' && responseData.message) {
                    errorMessage = responseData.message;
                } else if (err.response.status === 403) {
                    errorMessage = "Bu emri düzenleme yetkiniz bulunmamaktadır.";
                } else if (err.response.status === 500) {
                    errorMessage = "Sunucuda beklenmedik bir hata oluştu. Lütfen daha sonra tekrar deneyin.";
                }
            }
            throw new Error(errorMessage);
        }
    };

    return (
        <div style={styles.page}>
            <div style={styles.headerContainer}>
                <h1 style={styles.header}>Emir İzleme Paneli</h1>
                <div style={styles.statsText}>
                    {!isLoading && `${pageData.totalElements || 0} emir bulundu`}
                </div>
            </div>

            <OrderFilterPanel
                filters={filters}
                onFilterChange={handleFilterChange}
                onClearFilters={clearFilters}
            />

            <div style={styles.content}>
                {isLoading && <p style={{ textAlign: 'center' }}>Emirler yükleniyor...</p>}
                {error && <p style={{ textAlign: 'center', color: 'red' }}>HATA: {error}</p>}

                {!isLoading && !error && (
                    <>
                        <OrderTable orders={orders} onCancel={handleCancelClick} onEdit={handleEditClick} />
                        <Pagination
                            currentPage={pageData.number}
                            totalPages={pageData.totalPages}
                            onPageChange={handlePageChange}
                        />
                    </>
                )}
            </div>

            <ConfirmationModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onConfirm={handleConfirmCancel}
                title="Emir İptal Onayı"
            >
                <p>
                    Seçili emrin gerçekleşmemiş kısmı iptal edilecektir.<br/>
                    <strong>Yeni bir emir AÇILMAYACAKTIR.</strong><br/><br/>
                    Bu işlemi onaylıyor musunuz?
                </p>
            </ConfirmationModal>

            <EditOrderModal
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)}
                order={orderToEdit}
                onUpdate={handleUpdateOrder}
            />
        </div>
    );
}