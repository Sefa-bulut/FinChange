import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';

const styles = {
    modalOverlay: {
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0,0,0,0.5)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 1000
    },
    modalContent: {
        background: 'white',
        padding: '30px',
        borderRadius: '8px',
        width: '500px',
        maxWidth: '90vw',
        maxHeight: '90vh',
        overflowY: 'auto'
    },
    modalHeader: {
        fontSize: '20px',
        fontWeight: 'bold',
        marginBottom: '20px',
        color: '#333',
        borderBottom: '2px solid #c8102e',
        paddingBottom: '10px'
    },
    formGroup: {
        marginBottom: '20px'
    },
    label: {
        display: 'block',
        marginBottom: '8px',
        fontWeight: '600',
        color: '#555'
    },
    input: {
        width: '100%',
        padding: '12px',
        border: '1px solid #ccc',
        borderRadius: '4px',
        fontSize: '16px',
        boxSizing: 'border-box'
    },
    select: {
        width: '100%',
        padding: '12px',
        border: '1px solid #ccc',
        borderRadius: '4px',
        fontSize: '16px',
        background: 'white',
        boxSizing: 'border-box'
    },
    buttonContainer: {
        display: 'flex',
        justifyContent: 'flex-end',
        gap: '15px',
        marginTop: '30px'
    },
    button: {
        padding: '12px 25px',
        border: 'none',
        borderRadius: '5px',
        cursor: 'pointer',
        fontSize: '16px',
        fontWeight: 'bold'
    },
    cancelButton: {
        backgroundColor: '#6c757d',
        color: 'white'
    },
    submitButton: {
        backgroundColor: '#c8102e',
        color: 'white'
    },
    orderInfo: {
        backgroundColor: '#f8f9fa',
        padding: '15px',
        borderRadius: '6px',
        marginBottom: '20px',
        border: '1px solid #dee2e6'
    },
    infoRow: {
        display: 'flex',
        justifyContent: 'space-between',
        marginBottom: '8px'
    },
    infoLabel: {
        fontWeight: '600',
        color: '#6c757d'
    },
    infoValue: {
        color: '#333'
    },
    errorMessage: {
        color: '#dc3545',
        fontSize: '14px',
        marginTop: '5px'
    }
};

export default function EditOrderModal({ isOpen, onClose, order, onUpdate }) {
    const [isLoading, setIsLoading] = useState(false);
    
    const { register, handleSubmit, watch, setValue, formState: { errors } } = useForm({
        defaultValues: {
            orderType: order?.orderType || 'LIMIT',
            limitPrice: order?.limitPrice || '',
            lotAmount: order?.initialLotAmount || 0
        }
    });

    const watchOrderType = watch("orderType");
    const minLot = Math.max(1, order?.filledLotAmount || 0);

    useEffect(() => {
        if (order) {
            setValue('orderType', order.orderType);
            setValue('limitPrice', order.limitPrice || '');
            setValue('lotAmount', order.initialLotAmount);
        }
    }, [order, setValue]);

    const onSubmit = async (formData) => {
        setIsLoading(true);
        try {
            const updateData = {
                orderType: formData.orderType,
                lotAmount: parseInt(formData.lotAmount, 10),
                limitPrice: formData.orderType === 'LIMIT' ? parseFloat(formData.limitPrice) : null
            };

            await onUpdate(order.id, updateData);
        } catch (error) {
            toast.error(error.message || 'Emir güncellenirken bir hata oluştu.');
        } finally {
            setIsLoading(false);
        }
    };

    if (!isOpen || !order) return null;

    return (
        <div style={styles.modalOverlay} onClick={onClose}>
            <div style={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                <h2 style={styles.modalHeader}>Emir Düzenle</h2>
                
                <div style={styles.orderInfo}>
                    <div style={styles.infoRow}>
                        <span style={styles.infoLabel}>Emir Kodu:</span>
                        <span style={styles.infoValue}>{order.orderCode}</span>
                    </div>
                    <div style={styles.infoRow}>
                        <span style={styles.infoLabel}>Hisse:</span>
                        <span style={styles.infoValue}>{order.bistCode}</span>
                    </div>
                    <div style={styles.infoRow}>
                        <span style={styles.infoLabel}>İşlem Türü:</span>
                        <span style={styles.infoValue}>{order.transactionType === 'BUY' ? 'Alım' : 'Satım'}</span>
                    </div>
                    <div style={styles.infoRow}>
                        <span style={styles.infoLabel}>Gerçekleşen Lot:</span>
                        <span style={styles.infoValue}>{order.filledLotAmount} / {order.initialLotAmount}</span>
                    </div>
                </div>

                <form onSubmit={handleSubmit(onSubmit)}>
                    <div style={styles.formGroup}>
                        <label style={styles.label}>Emir Türü</label>
                        <select style={styles.select} {...register("orderType", { required: "Emir türü seçiniz" })}>
                            <option value="LIMIT">Limit</option>
                            <option value="MARKET">Piyasa</option>
                        </select>
                        {errors.orderType && <div style={styles.errorMessage}>{errors.orderType.message}</div>}
                    </div>

                    {watchOrderType === 'LIMIT' && (
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Limit Fiyat (TL)</label>
                            <input
                                type="number"
                                step="0.01"
                                min="0.01"
                                style={styles.input}
                                {...register("limitPrice", { 
                                    required: watchOrderType === 'LIMIT' ? "Limit fiyat giriniz" : false,
                                    valueAsNumber: true,
                                    validate: (value) => (watchOrderType !== 'LIMIT' || (typeof value === 'number' && value > 0)) || "Fiyat 0'dan büyük olmalıdır"
                                })}
                            />
                            {errors.limitPrice && <div style={styles.errorMessage}>{errors.limitPrice.message}</div>}
                        </div>
                    )}

                    <div style={styles.formGroup}>
                        <label style={styles.label}>Lot Miktarı</label>
                        <input
                            type="number"
                            min={minLot}
                            style={styles.input}
                            {...register("lotAmount", { 
                                required: "Lot miktarı giriniz",
                                valueAsNumber: true,
                                validate: (value) => {
                                    if (typeof value !== 'number' || Number.isNaN(value)) return 'Geçerli bir sayı giriniz';
                                    if (value < minLot) {
                                        return order?.filledLotAmount && order.filledLotAmount > 0
                                            ? `Miktar, gerçekleşen lottan (${order.filledLotAmount}) az olamaz.`
                                            : 'Minimum miktar 1 olmalıdır.';
                                    }
                                    return true;
                                }
                            })}
                        />
                        {errors.lotAmount && <div style={styles.errorMessage}>{errors.lotAmount.message}</div>}
                        <small style={{ color: '#6c757d', fontSize: '12px' }}>
                            Minimum: {minLot} lot {order?.filledLotAmount > 0 ? '(gerçekleşen miktar)' : ''}
                        </small>
                    </div>

                    <div style={styles.buttonContainer}>
                        <button 
                            type="button" 
                            onClick={onClose} 
                            style={{...styles.button, ...styles.cancelButton}}
                            disabled={isLoading}
                        >
                            İptal
                        </button>
                        <button 
                            type="submit" 
                            style={{...styles.button, ...styles.submitButton}}
                            disabled={isLoading}
                        >
                            {isLoading ? 'Güncelleniyor...' : 'Güncelle'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}