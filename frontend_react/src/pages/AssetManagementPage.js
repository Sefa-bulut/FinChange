import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { toast } from 'react-toastify';
import { assetSchema } from '../validation/assetSchemas';
import { createAsset } from '../services/AssetService';

const styles = {
    page: { maxWidth: '800px', margin: '40px auto', padding: '20px' },
    formContainer: { backgroundColor: 'white', padding: '32px', borderRadius: '12px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)' },
    header: { fontSize: '28px', fontWeight: 'bold', color: '#333', marginBottom: '30px' },
    formGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px 30px' },
    formGroup: { marginBottom: '15px' },
    label: { display: 'block', marginBottom: '8px', fontWeight: '600', color: '#555' },
    input: { width: '100%', padding: '12px', border: '1px solid #ccc', borderRadius: '4px', fontSize: '16px' },
    errorText: { color: 'red', marginTop: '5px', fontSize: '14px' },
    buttonContainer: { gridColumn: '2 / 3', marginTop: '20px', display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: '15px' },
    button: { padding: '12px 25px', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' },
    submitButton: { backgroundColor: '#c8102e', color: 'white' },
};

export default function AssetManagementPage() {
    const navigate = useNavigate();
    const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm({
        resolver: yupResolver(assetSchema),
        defaultValues: {
            isinCode: '',
            bistCode: '',
            companyName: '',
            sector: '',
            currency: '',
            settlementDays: '',
            maxOrderValue: '',
        }
    });

    const onSubmit = async (data) => {
        try {
            const payload = {
                ...data,
                settlementDays: Number(data.settlementDays),
                maxOrderValue: data.maxOrderValue ? Number(data.maxOrderValue) : null
            };
            await createAsset(payload);
            toast.success('Varlık başarıyla tanımlandı!');
            navigate('/dashboard/assets');
        } catch (error) {
            toast.error(error.message || 'Varlık oluşturulurken bir hata oluştu.');
        }
    };

    return (
        <div style={styles.page}>
            <div style={styles.formContainer}>
                <h1 style={styles.header}>Varlık Tanımlama</h1>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <div style={styles.formGrid}>
                        <div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>ISIN Kodu*</label>
                                <input style={{ ...styles.input, borderColor: errors.isinCode ? 'red' : '#ccc' }} {...register('isinCode')} maxLength={12} />
                                {errors.isinCode && <p style={styles.errorText}>{errors.isinCode.message}</p>}
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>BIST İşlem Kodu*</label>
                                <input style={{ ...styles.input, borderColor: errors.bistCode ? 'red' : '#ccc' }} {...register('bistCode')} maxLength={10} />
                                {errors.bistCode && <p style={styles.errorText}>{errors.bistCode.message}</p>}
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Şirket Adı*</label>
                                <input style={{ ...styles.input, borderColor: errors.companyName ? 'red' : '#ccc' }} {...register('companyName')} />
                                {errors.companyName && <p style={styles.errorText}>{errors.companyName.message}</p>}
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Para Birimi*</label>
                                <input style={{ ...styles.input, borderColor: errors.currency ? 'red' : '#ccc' }} {...register('currency')} maxLength={3} />
                                {errors.currency && <p style={styles.errorText}>{errors.currency.message}</p>}
                            </div>
                        </div>
                        <div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Sektör</label>
                                <input style={styles.input} {...register('sector')} />
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Takas Süresi (Gün)*</label>
                                <input type="number" style={{ ...styles.input, borderColor: errors.settlementDays ? 'red' : '#ccc' }} {...register('settlementDays')} />
                                {errors.settlementDays && <p style={styles.errorText}>{errors.settlementDays.message}</p>}
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Maksimum Emir Değeri</label>
                                <input type="number" style={{ ...styles.input, borderColor: errors.maxOrderValue ? 'red' : '#ccc' }} {...register('maxOrderValue')} />
                                {errors.maxOrderValue && <p style={styles.errorText}>{errors.maxOrderValue.message}</p>}
                            </div>
                        </div>
                        <div style={styles.buttonContainer}>
                            <button type="submit" style={{ ...styles.button, ...styles.submitButton }} disabled={isSubmitting}>
                                {isSubmitting ? 'KAYDEDİLİYOR...' : 'KAYDET'}
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}