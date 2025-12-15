import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { updateUserSchema } from '../validation/userSchemas'; 
import { getUserById, updateUserRoles, updateUserStatus, updateUserInfo } from '../services/userService';
import { toast } from 'react-toastify';

// === STİL DEĞİŞİKLİKLERİ BURADA ===
const styles = {
    page: { padding: '30px', backgroundColor: '#f8f9fa' },
    // Başlığı ortalamak için textAlign: 'center' ekliyoruz
    header: { fontSize: '28px', fontWeight: 'bold', color: '#333', marginBottom: '30px', textAlign: 'center' },
    // Form konteynerini sayfanın ortasına getirmek için margin: '0 auto' ekliyoruz
    formContainer: { backgroundColor: 'white', padding: '30px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(255, 253, 253, 0.05)', maxWidth: '700px', margin: '0 auto' },
    formGroup: { marginBottom: '20px' },
    label: { display: 'block', marginBottom: '8px', fontWeight: '600', color: '#555' },
    input: { width: '100%', padding: '12px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px', fontSize: '16px' },
    checkboxContainer: { display: 'flex', flexWrap: 'wrap', gap: '20px', padding: '10px 0', border: '1px solid #e0e0e0', borderRadius: '4px' },
    checkboxLabel: { display: 'flex', alignItems: 'center', cursor: 'pointer', fontSize: '16px' },
    checkbox: { marginRight: '8px', width: '18px', height: '18px' },
    radioContainer: { display: 'flex', gap: '20px', padding: '10px 0' },
    radioLabel: { display: 'flex', alignItems: 'center', cursor: 'pointer', fontSize: '16px' },
    radio: { marginRight: '8px', width: '18px', height: '18px' },
    buttonContainer: { marginTop: '30px', display: 'flex', justifyContent: 'flex-end', gap: '15px' },
    button: { padding: '12px 25px', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' },
    submitButton: { backgroundColor: '#c8102e', color: 'white' },
    cancelButton: { backgroundColor: '#f0f0f0', color: '#333' },
    modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1050 },
    modalContent: { background: 'white', padding: '2rem', borderRadius: '8px', width: '450px', textAlign: 'center', boxShadow: '0 5px 15px rgba(0,0,0,0.3)' },
    modalButtons: { marginTop: '1.5rem', display: 'flex', justifyContent: 'center', gap: '1rem' },
};

const ConfirmationModal = ({ isOpen, onClose, onConfirm, title, children }) => {
    if (!isOpen) return null;
    return (
        <div style={styles.modalOverlay} onClick={onClose}>
            <div style={styles.modalContent} onClick={e => e.stopPropagation()}>
                <h3 style={{marginTop: 0}}>{title}</h3>
                <p>{children}</p>
                <div style={styles.modalButtons}>
                    <button style={{...styles.button,color: 'white' , backgroundColor: '#6c757d'}} onClick={onClose}>İptal</button>
                    <button style={{...styles.button, color: 'white' ,backgroundColor: '#c8102e'}} onClick={onConfirm}>Onayla</button>
                </div>
            </div>
        </div>
    );
};

const availableRoles = ['ADMIN', 'TRADER', 'ANALYST', 'AUDITOR'];

export default function UserDetailPage() {
    const { userId } = useParams();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [pendingRoleChange, setPendingRoleChange] = useState(null);

    const { register, handleSubmit, control, reset, formState: { errors, isSubmitting, isDirty } } = useForm({
        resolver: yupResolver(updateUserSchema),
        defaultValues: {
            firstName: '', lastName: '', email: '', personnelCode: '', roleNames: [], isActive: true,
        }
    });

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const response = await getUserById(userId); 
                if (response && response.isSuccess && response.result) {
                    const userData = response.result;
                    reset({
                        firstName: userData.firstName, lastName: userData.lastName, email: userData.email,
                        personnelCode: userData.personnelCode, roleNames: userData.roles || [], isActive: userData.isActive,
                    });
                } else { throw new Error(response.message || "Kullanıcı bulunamadı."); }
            } catch (err) {
                setError('Kullanıcı bilgileri yüklenemedi.');
                toast.error(err.message || 'Kullanıcı bilgileri yüklenemedi.');
            } finally { setIsLoading(false); }
        };
        fetchUserData();
    }, [userId, reset]);

    const onSubmit = async (data) => {
        try {
            await updateUserInfo(userId, { firstName: data.firstName, lastName: data.lastName, personnelCode: data.personnelCode });
            await updateUserRoles(userId, data.roleNames);
            await updateUserStatus(userId, data.isActive);
            toast.success('Personel başarıyla güncellendi.');
            setTimeout(() => navigate('/dashboard/users'), 1500);
        } catch (err) {
            toast.error(err.message || 'Güncelleme sırasında bir hata oluştu.');
        }
    };

    if (isLoading) return <div style={styles.page}><p>Yükleniyor...</p></div>;
    if (error) return <div style={styles.page}><p style={{ color: 'red' }}>{error}</p></div>;

    return (
        <div style={styles.page}>
            {/* === DEĞİŞİKLİK BURADA: Başlık ve form aynı konteyner içine alındı === */}
            <div style={styles.formContainer}>
                <h1 style={styles.header}>Personel Detayları</h1>
                <form onSubmit={handleSubmit(onSubmit)}>
                    {/* Form Alanları */}
                    <div style={styles.formGroup}><label style={styles.label}>Ad*</label><input style={styles.input} {...register('firstName')} />{errors.firstName && <p style={{ color: 'red', marginTop: '5px' }}>{errors.firstName.message}</p>}</div>
                    <div style={styles.formGroup}><label style={styles.label}>Soyad*</label><input style={styles.input} {...register('lastName')} />{errors.lastName && <p style={{ color: 'red', marginTop: '5px' }}>{errors.lastName.message}</p>}</div>
                    <div style={styles.formGroup}><label style={styles.label}>E-posta (Değiştirilemez)</label><input style={{...styles.input, backgroundColor: '#e9ecef'}} {...register('email')} disabled /></div>
                    <div style={styles.formGroup}><label style={styles.label}>Personel Kodu*</label><input style={styles.input} {...register('personnelCode')} />{errors.personnelCode && <p style={{ color: 'red', marginTop: '5px' }}>{errors.personnelCode.message}</p>}</div>

                    {/* Rol Seçimi */}
                    <div style={styles.formGroup}>
                        <label style={styles.label}>Yetki*</label>
                        <Controller
                            name="roleNames"
                            control={control}
                            render={({ field }) => {
                                const handleRoleCheckboxClick = (role) => {
                                    const action = field.value.includes(role) ? 'kaldırmak' : 'eklemek';
                                    setPendingRoleChange({ role, action });
                                    setIsModalOpen(true);
                                };
                                const handleConfirmRoleChange = () => {
                                    if (!pendingRoleChange) return;
                                    const { role, action } = pendingRoleChange;
                                    const newValue = action === 'eklemek'
                                        ? [...field.value, role]
                                        : field.value.filter(r => r !== role);
                                    field.onChange(newValue);
                                    setIsModalOpen(false);
                                    setPendingRoleChange(null);
                                };
                                const handleCloseModal = () => {
                                    setIsModalOpen(false);
                                    setPendingRoleChange(null);
                                };
                                return (
                                    <>
                                        <ConfirmationModal isOpen={isModalOpen} onClose={handleCloseModal} onConfirm={handleConfirmRoleChange} title="Yetki Değişikliği Onayı">
                                            <strong>{pendingRoleChange?.role}</strong> yetkisini bu kullanıcıdan <strong>{pendingRoleChange?.action}</strong> istediğinizden emin misiniz?
                                        </ConfirmationModal>
                                        <div style={styles.checkboxContainer}>
                                            {availableRoles.map(role => (
                                                <label key={role} style={styles.checkboxLabel}>
                                                    <input type="checkbox" style={styles.checkbox} checked={field.value.includes(role)} onChange={() => handleRoleCheckboxClick(role)} />
                                                    {role}
                                                </label>
                                            ))}
                                        </div>
                                    </>
                                );
                            }}
                        />
                        {errors.roleNames && <p style={{ color: 'red', marginTop: '5px' }}>{errors.roleNames.message}</p>}
                    </div>

                    {/* Durum Seçimi */}
                    <div style={styles.formGroup}>
                        <label style={styles.label}>Durum*</label>
                        <Controller
                            name="isActive"
                            control={control}
                            render={({ field }) => (
                                <div style={styles.radioContainer}>
                                    <label style={styles.radioLabel}><input type="radio" style={styles.radio} value={true} checked={field.value === true} onChange={() => field.onChange(true)} /> Aktif</label>
                                    <label style={styles.radioLabel}><input type="radio" style={styles.radio} value={false} checked={field.value === false} onChange={() => field.onChange(false)} /> Pasif</label>
                                </div>
                            )}
                        />
                        {errors.isActive && <p style={{ color: 'red', marginTop: '5px' }}>{errors.isActive.message}</p>}
                    </div>

                    <div style={styles.buttonContainer}>
                        <button type="button" style={{...styles.button, ...styles.cancelButton}} onClick={() => navigate('/dashboard/users')}>İptal</button>
                        <button type="submit" style={{...styles.button, ...styles.submitButton}} disabled={!isDirty || isSubmitting}>{isSubmitting ? 'KAYDEDİLİYOR...' : 'KAYDET'}</button>
                    </div>
                </form>
            </div>
        </div>
    );
}