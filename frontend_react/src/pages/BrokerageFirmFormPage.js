import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getFirmById, createFirm, updateFirm } from '../services/brokerageService';

const schema = yup.object().shape({
    kurumAdi: yup.string().required('Kurum adı zorunludur.'),
    kurumKodu: yup.string().required('Kurum kodu zorunludur.'),
    email: yup.string().email('Geçerli bir e-posta giriniz.').required('E-posta zorunludur.'),
    commissionRatePercent: yup
        .number()
        .typeError('Lütfen geçerli bir sayı girin.')
        .min(0, 'Komisyon oranı en az 0 olabilir.')
        .max(100, 'Komisyon oranı en fazla 100 olabilir.')
        .required('Komisyon oranı zorunludur.'),
    status: yup.string().oneOf(['ACTIVE', 'INACTIVE']).required(),
    apiUrl: yup.string().url('Geçerli bir URL giriniz.').nullable(),
    username: yup.string().nullable(),
    integrationType: yup.string().nullable(),
});

const styles = {
    page: { maxWidth: '800px', margin: '40px auto', padding: '20px' },
    formContainer: { backgroundColor: 'white', padding: '32px', borderRadius: '12px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)' },
    header: { fontSize: '28px', fontWeight: 'bold', color: '#333', marginBottom: '30px' },
    formGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px 30px' },
    formGroup: { marginBottom: '15px' },
    label: { display: 'block', marginBottom: '8px', fontWeight: '600', color: '#555' },
    input: { width: '100%', padding: '12px', border: '1px solid #ccc', borderRadius: '4px', fontSize: '16px' },
    errorText: { color: 'red', marginTop: '5px', fontSize: '14px' },
    buttonContainer: { gridColumn: '1 / -1', marginTop: '20px', display: 'flex', justifyContent: 'flex-end', gap: '15px' },
    button: { padding: '12px 25px', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' },
    submitButton: { backgroundColor: '#c8102e', color: 'white' },
    cancelButton: { backgroundColor: '#6c757d', color: 'white' },
};

export default function BrokerageFirmFormPage() {
    const { id } = useParams();
    const isEditMode = !!id;
    const navigate = useNavigate();

    const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm({
        resolver: yupResolver(schema),
        defaultValues: { status: 'INACTIVE' }
    });

    useEffect(() => {
        if (isEditMode) {
            const fetchFirm = async () => {
                try {
                    const firm = await getFirmById(id);
                    reset({
                        ...firm,
                        commissionRatePercent: firm.commissionRatePercent.toFixed(4)
                    });
                } catch (error) {
                    toast.error(error.message || 'Kurum bilgileri getirilemedi.');
                    navigate('/dashboard/brokerage-firms');
                }
            };
            fetchFirm();
        }
    }, [id, isEditMode, navigate, reset]);

    const onSubmit = async (data) => {
        try {
            const payload = {
                ...data,
                commissionRatePercent: parseFloat(data.commissionRatePercent)
            };

            if (isEditMode) {
                await updateFirm(id, payload);
                toast.success('Aracı kurum başarıyla güncellendi.');
            } else {
                await createFirm(payload);
                toast.success('Aracı kurum başarıyla oluşturuldu.');
            }
            navigate('/dashboard/brokerage-firms');
        } catch (error) {
            toast.error(error.message || 'İşlem sırasında bir hata oluştu.');
        }
    };

    return (
        <div style={styles.page}>
            <div style={styles.formContainer}>
                <h1 style={styles.header}>{isEditMode ? 'Aracı Kurum Düzenle' : 'Yeni Aracı Kurum Tanımla'}</h1>
                <form onSubmit={handleSubmit(onSubmit)}>
                    <div style={styles.formGrid}>
                        <div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Kurum Adı*</label>
                                <input style={styles.input} {...register('kurumAdi')} />
                                {errors.kurumAdi && <p style={styles.errorText}>{errors.kurumAdi.message}</p>}
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Kurum Kodu*</label>
                                <input style={styles.input} {...register('kurumKodu')} />
                                {errors.kurumKodu && <p style={styles.errorText}>{errors.kurumKodu.message}</p>}
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>E-posta*</label>
                                <input style={styles.input} {...register('email')} />
                                {errors.email && <p style={styles.errorText}>{errors.email.message}</p>}
                            </div>
                             <div style={styles.formGroup}>
                                <label style={styles.label}>Komisyon Oranı (%)*</label>
                                <input type="number" step="0.0001" style={styles.input} {...register('commissionRatePercent')} />
                                {errors.commissionRatePercent && <p style={styles.errorText}>{errors.commissionRatePercent.message}</p>}
                            </div>
                        </div>
                        <div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>API URL</label>
                                <input style={styles.input} {...register('apiUrl')} />
                                {errors.apiUrl && <p style={styles.errorText}>{errors.apiUrl.message}</p>}
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Kullanıcı Adı</label>
                                <input style={styles.input} {...register('username')} />
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Entegrasyon Tipi</label>
                                <input style={styles.input} {...register('integrationType')} />
                            </div>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>Durum*</label>
                                <div>
                                    <input type="radio" id="active" value="ACTIVE" {...register('status')} />
                                    <label htmlFor="active" style={{ marginRight: '15px' }}> Aktif</label>
                                    <input type="radio" id="inactive" value="INACTIVE" {...register('status')} />
                                    <label htmlFor="inactive"> Pasif</label>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div style={styles.buttonContainer}>
                        <button type="button" style={{...styles.button, ...styles.cancelButton}} onClick={() => navigate('/dashboard/brokerage-firms')}>
                            İptal
                        </button>
                        <button type="submit" style={{...styles.button, ...styles.submitButton}} disabled={isSubmitting}>
                            {isSubmitting ? 'KAYDEDİLİYOR...' : 'KAYDET'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
