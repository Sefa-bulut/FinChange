import React from 'react';
import { useNavigate } from 'react-router-dom';
import { getTransactionTypeDisplay, getOrderTypeDisplay, getOrderStatusDisplay } from '../../utils/enumUtils';

const styles = {
    table: { width: '100%', borderCollapse: 'collapse', fontSize: '14px', whiteSpace: 'nowrap' },
    th: { backgroundColor: '#f8f9fa', borderBottom: '2px solid #dee2e6', padding: '12px 15px', textAlign: 'left', fontWeight: '600', color: '#495057' },
    td: { borderBottom: '1px solid #e9ecef', padding: '12px 15px', verticalAlign: 'middle' },
    row: { transition: 'background-color 1s ease-in-out' },
    actionsCell: { display: 'flex', gap: '5px' },
    actionButton: { padding: '6px 12px', fontSize: '13px', border: 'none', borderRadius: '5px', cursor: 'pointer', color: 'white' },
    cancelButton: { backgroundColor: '#dc3545' },
    editButton: { backgroundColor: '#ffc107' },
    detailButton: { backgroundColor: '#0dcaf0' },
    disabledButton: { backgroundColor: '#6c757d', cursor: 'not-allowed' },
    emptyState: { textAlign: 'center', padding: '50px', color: '#6c757d' },
};

const formatNumber = (num) => {
    if (typeof num !== 'number') return '-';
    return new Intl.NumberFormat('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(num);
};

const formatDate = (dateString) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('tr-TR', { day: '2-digit', month: '2-digit', year: 'numeric' });
};


const OrderTable = ({ orders, onCancel, onEdit }) => {
    const navigate = useNavigate();

    if (!orders || orders.length === 0) {
        return <div style={styles.emptyState}><p>Gösterilecek emir bulunamadı.</p></div>;
    }

    return (
        <div style={{ overflowX: 'auto' }}>
            <table style={styles.table}>
                <thead>
                <tr>
                    <th style={styles.th}>Emir Kodu</th>
                    <th style={styles.th}>Müşteri Kodu</th>
                    <th style={styles.th}>Hisse Kodu</th>
                    <th style={styles.th}>İşlem Tipi</th>
                    <th style={styles.th}>Lot Miktarı</th>
                    <th style={styles.th}>Emir Fiyatı</th>
                    <th style={styles.th}>Gerçekleşen Fiyat</th>
                    <th style={styles.th}>Toplam Tutar</th>
                    <th style={styles.th}>Emir Durumu</th>
                    <th style={styles.th}>Emir Tarihi</th>
                    <th style={styles.th}>Emir Gerçekleşme Tarihi</th>
                    <th style={styles.th}>Aksiyonlar</th>
                </tr>
                </thead>
                <tbody>
                {orders.map((order) => {
                    const canBeCancelled = order.status === 'ACTIVE' || order.status === 'PARTIALLY_FILLED';
                    const canBeEdited = order.status === 'ACTIVE';
                    
                    const rowClassName = `order-row ${order.highlight || ''}`;
                    const totalAmount = order.limitPrice ? order.initialLotAmount * order.limitPrice : null;
                    const isFilled = order.status === 'FILLED' || order.status === 'PARTIALLY_FILLED';

                    return (
                        <tr key={order.id} style={styles.row} className={rowClassName}>
                            <td style={styles.td}>{order.orderCode?.substring(0, 9) || '-'}</td>
                            <td style={styles.td}>{order.customerCode}</td>
                            <td style={styles.td}>{order.bistCode}</td>
                            <td style={styles.td}>{getTransactionTypeDisplay(order.transactionType)}</td>
                            <td style={styles.td}>{`${order.filledLotAmount} / ${order.initialLotAmount}`}</td>
                            <td style={styles.td}>{order.limitPrice ? formatNumber(order.limitPrice) : 'Piyasa'}</td>
                            <td style={styles.td}>{
                                (typeof order.executedPrice === 'number')
                                    ? formatNumber(order.executedPrice)
                                    : (typeof order.limitPrice === 'number' ? formatNumber(order.limitPrice) : '-')
                            }</td>
                            <td style={styles.td}>{formatNumber(totalAmount)}</td>
                            <td style={styles.td}>{getOrderStatusDisplay(order.status)}</td>
                            <td style={styles.td}>{formatDate(order.createdAt)}</td>
                            <td style={styles.td}>{isFilled ? formatDate(order.updatedAt) : '-'}</td>
                            <td style={styles.td}>
                                <div style={styles.actionsCell}>
                                    <button
                                        style={{ ...styles.actionButton, ...styles.cancelButton, ...(!canBeCancelled && styles.disabledButton) }}
                                        onClick={() => onCancel(order)}
                                        disabled={!canBeCancelled}
                                        title="Emri İptal Et"
                                    >
                                        İptal
                                    </button>
                                    <button
                                        style={{ ...styles.actionButton, ...styles.editButton, ...(!canBeEdited && styles.disabledButton) }}
                                        onClick={() => onEdit && onEdit(order)}
                                        disabled={!canBeEdited}
                                        title="Emri Düzenle"
                                    >
                                        Düzenle
                                    </button>
                                </div>
                            </td>
                        </tr>
                    );
                })}
                </tbody>
            </table>
        </div>
    );
};

export default OrderTable;