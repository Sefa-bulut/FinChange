import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { inviteUserSchema } from '../validation/userSchemas';
import { inviteUser } from '../services/userService';
import { toast } from 'react-toastify';

// === MEVCUT STİLLERİNİZE EK OLARAK MODAL STİLLERİ EKLENDİ ===
const styles = {
    page: { padding: '30px', backgroundColor: '#f8f9fa' },
    header: { fontSize: '28px', fontWeight: 'bold', color: '#333', marginBottom: '30px', textAlign: 'center' },
    formContainer: { backgroundColor: 'white', padding: '30px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)', maxWidth: '700px', margin: '0 auto' },
    formGroup: { marginBottom: '20px' },
    label: { display: 'block', marginBottom: '8px', fontWeight: '600', color: '#555' },
    input: { width: '100%', padding: '12px', boxSizing: 'border-box', border: '1px solid #ccc', borderRadius: '4px', fontSize: '16px' },
    checkboxContainer: { display: 'flex', flexWrap: 'wrap', gap: '20px', padding: '10px 0', border: '1px solid #e0e0e0', borderRadius: '4px' },
    checkboxLabel: { display: 'flex', alignItems: 'center', cursor: 'pointer', fontSize: '16px' },
    checkbox: { marginRight: '8px', width: '18px', height: '18px' },
    buttonContainer: { marginTop: '30px', display: 'flex', justifyContent: 'flex-end', gap: '15px' },
    button: { padding: '12px 25px', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px', fontWeight: 'bold' },
    submitButton: { backgroundColor: '#c8102e', color: 'white' },
    cancelButton: { backgroundColor: '#f0f0f0', color: '#333' },
    serverError: {
        backgroundColor: '#ffebee', color: '#d32f2f', border: '1px solid #ef9a9a',
        borderRadius: '4px', padding: '15px', marginBottom: '20px', fontSize: '16px', fontWeight: '500'
    },
    // Modal stilleri
    modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1050 },
    modalContent: { background: 'white', padding: '2rem', borderRadius: '8px', width: '450px', textAlign: 'center', boxShadow: '0 5px 15px rgba(0,0,0,0.3)' },
    modalButtons: { marginTop: '1.5rem', display: 'flex', justifyContent: 'center', gap: '1rem' },
};

// === YENİ MODAL BİLEŞENİ ===
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

export default function NewUserPage() {
    const navigate = useNavigate();
    const [serverError, setServerError] = useState('');
    // === YENİ STATE'LER (MODAL YÖNETİMİ İÇİN) ===
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [pendingRoleChange, setPendingRoleChange] = useState(null); // { role, action }

    const { register, handleSubmit, control, formState: { errors, isSubmitting } } = useForm({
        resolver: yupResolver(inviteUserSchema),
        defaultValues: {
            firstName: '', lastName: '', email: '', personnelCode: '', roleNames: [],
        }
    });

    const onSubmit = async (data) => {
        setServerError('');
        try {
            await inviteUser(data);
            toast.success("Personel daveti başarıyla gönderildi!");
            setTimeout(() => {
                navigate('/dashboard/users');
            }, 2000);
        } catch (err) {
            const errorMessage = err.message || 'Personel davet edilirken bir hata oluştu.';
            setServerError(errorMessage);
            toast.error(errorMessage);
        }
    };

    return (
        <div style={styles.page}>
            <div style={styles.formContainer}>
                <h1 style={styles.header}>Yeni Personel Kayıt</h1>
                
                {serverError && (
                    <div style={styles.serverError}>
                        <strong>Hata:</strong> {serverError}
                    </div>
                )}
                
                <form onSubmit={handleSubmit(onSubmit)}>
                    <div style={styles.formGroup}><label style={styles.label}>Ad*</label><input style={{...styles.input, borderColor: errors.firstName ? 'red' : '#ccc'}} {...register('firstName')} />{errors.firstName && <p style={{ color: 'red', marginTop: '5px' }}>{errors.firstName.message}</p>}</div>
                    <div style={styles.formGroup}><label style={styles.label}>Soyad*</label><input style={{...styles.input, borderColor: errors.lastName ? 'red' : '#ccc'}} {...register('lastName')} />{errors.lastName && <p style={{ color: 'red', marginTop: '5px' }}>{errors.lastName.message}</p>}</div>
                    <div style={styles.formGroup}><label style={styles.label}>E-Posta*</label><input style={{...styles.input, borderColor: errors.email ? 'red' : '#ccc'}} type="email" {...register('email')} />{errors.email && <p style={{ color: 'red', marginTop: '5px' }}>{errors.email.message}</p>}</div>
                    <div style={styles.formGroup}><label style={styles.label}>Personel Kodu*</label><input style={{...styles.input, borderColor: errors.personnelCode ? 'red' : '#ccc'}} {...register('personnelCode')} />{errors.personnelCode && <p style={{ color: 'red', marginTop: '5px' }}>{errors.personnelCode.message}</p>}</div>
                    
                    <div style={styles.formGroup}>
                        <label style={styles.label}>Yetki*</label>
                        <Controller
                            name="roleNames"
                            control={control}
                            render={({ field }) => {
                                // === YENİ MANTIK BURADA BAŞLIYOR ===
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
                                        <ConfirmationModal
                                            isOpen={isModalOpen}
                                            onClose={handleCloseModal}
                                            onConfirm={handleConfirmRoleChange}
                                            title="Yetki Değişikliği Onayı"
                                        >
                                            <strong>{pendingRoleChange?.role}</strong> yetkisini bu yeni personele <strong>{pendingRoleChange?.action}</strong> istediğinizden emin misiniz?
                                        </ConfirmationModal>
                                        
                                        <div style={styles.checkboxContainer}>
                                            {availableRoles.map(role => (
                                                <label key={role} style={styles.checkboxLabel}>
                                                    <input
                                                        type="checkbox"
                                                        style={styles.checkbox}
                                                        checked={field.value.includes(role)}
                                                        onChange={() => handleRoleCheckboxClick(role)}
                                                    />
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
                    
                    <div style={styles.buttonContainer}>
                        <button type="button" style={{...styles.button, ...styles.cancelButton}} onClick={() => navigate('/dashboard/users')}>
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