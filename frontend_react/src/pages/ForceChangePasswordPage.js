import React, { useState } from 'react'; // 'useState' eklendi
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { passwordSchema } from '../validation/userSchemas';
import { forceChangePassword } from '../services/authService';
import finchangeLogo from '../assets/images/finchange-logo.png';
import { toast } from 'react-toastify'; // 'alert' yerine 'toast' kullanalım

const ForceChangePasswordPage = () => {
    const navigate = useNavigate();
    // Sunucudan gelen hataları yönetmek için state
    const [serverError, setServerError] = useState('');

    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
        resolver: yupResolver(passwordSchema),
        defaultValues: {
            newPassword: '',
            confirmPassword: '',
        }
    });

    const onSubmit = async (data) => {
        setServerError(''); // Her denemede eski hatayı temizle
        try {
            // GÜNCELLENDİ: Servis artık işlemin başarılı olup olmadığını boolean olarak döndürür.
            const isSuccess = await forceChangePassword(data.newPassword, data.confirmPassword);

            if (isSuccess) {
                toast.success('Şifreniz başarıyla güncellendi. Panele yönlendiriliyorsunuz...');
                
                // Başarılı olduktan 2 saniye sonra yönlendir
                setTimeout(() => {
                    navigate('/dashboard');
                }, 2000);
            } else {
                // Beklenmedik bir durum: Servis `false` veya `null` döndürdü.
                throw new Error("Sunucudan güncelleme onayı alınamadı.");
            }
        } catch (err) {
            // Interceptor'dan gelen formatlanmış hata mesajını göster
            toast.error(err.message || 'Şifre güncellenirken bir hata oluştu.');
            setServerError(err.message || 'Bir hata oluştu.');
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f0f2f5' }}>
            <div style={{ padding: '40px', background: 'white', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', textAlign: 'center', width: '400px' }}>
                <img src={finchangeLogo} alt="FINCHANGE" style={{ maxWidth: '150px', marginBottom: '20px' }} />
                <h2 style={{ marginBottom: '10px' }}>Yeni Şifre Belirle</h2>
                <p style={{ color: '#666', marginBottom: '20px' }}>Güvenliğiniz için lütfen yeni bir şifre oluşturun.</p>
                
                {/* Sunucu Hata Mesajı Alanı */}
                {serverError && (
                    <div style={{ color: 'red', marginBottom: '15px', padding: '10px', backgroundColor: '#ffebee', border: '1px solid red', borderRadius: '4px' }}>
                        {serverError}
                    </div>
                )}
                
                <form onSubmit={handleSubmit(onSubmit)}>
                    <input
                        type="password"
                        placeholder="Yeni Şifre"
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            marginBottom: '5px',
                            boxSizing: 'border-box',
                            border: `1px solid ${errors.newPassword ? 'red' : '#ccc'}`, 
                            borderRadius: '4px' 
                        }}
                        {...register('newPassword')}
                    />
                    {errors.newPassword && <div style={{ color: 'red', marginBottom: '10px', fontSize: '14px', textAlign: 'left' }}>{errors.newPassword.message}</div>}
                    
                    <input
                        type="password"
                        placeholder="Yeni Şifre (Tekrar)"
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            marginBottom: '5px', 
                            boxSizing: 'border-box',
                            border: `1px solid ${errors.confirmPassword ? 'red' : '#ccc'}`, 
                            borderRadius: '4px' 
                        }}
                        {...register('confirmPassword')}
                    />
                    {errors.confirmPassword && <div style={{ color: 'red', marginBottom: '15px', fontSize: '14px', textAlign: 'left' }}>{errors.confirmPassword.message}</div>}
                    
                    <button 
                        type="submit" 
                        disabled={isSubmitting} 
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            background: isSubmitting ? '#9ca3af' : '#c8102e', 
                            color: 'white', 
                            border: 'none', 
                            borderRadius: '4px', 
                            cursor: 'pointer', 
                            fontSize: '16px',
                            marginTop: '10px'
                        }}
                    >
                        {isSubmitting ? 'KAYDEDİLİYOR...' : 'ŞİFREYİ GÜNCELLE'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ForceChangePasswordPage;