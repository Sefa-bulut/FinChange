import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { resetPasswordSchema } from '../validation/userSchemas';
import { resetPassword } from '../services/authService';
import finchangeLogo from '../assets/images/finchange-logo.png';
import { toast } from 'react-toastify';

const ResetPasswordPage = () => {
    const navigate = useNavigate();
    // Component'in kendi hata ve başarı durumlarını yönetmesi için state'ler
    const [serverError, setServerError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
        resolver: yupResolver(resetPasswordSchema),
        defaultValues: {
            token: '',
            newPassword: '',
            confirmPassword: '',
        }
    });

    const onSubmit = async (data) => {
        // Her denemede eski mesajları temizle
        setServerError('');
        setSuccessMessage('');

        try {
            // Güncellenmiş servis, artık doğrudan sunucudan gelen başarı mesajını (string) döndürür.
            const messageFromServer = await resetPassword(data.token, data.newPassword);

            if (messageFromServer) {
                // Sunucudan gelen mesajı gösteriyoruz.
                toast.success(messageFromServer);
                setSuccessMessage(messageFromServer + " Giriş sayfasına yönlendiriliyorsunuz...");
                
                // Başarılı olduktan sonra 3 saniye bekleyip yönlendiriyoruz.
                setTimeout(() => {
                    navigate('/login');
                }, 3000);
            } else {
                // Beklenmedik bir durum: Servis `null` döndürdü.
                throw new Error("Sunucudan geçerli bir yanıt alınamadı.");
            }
        } catch (err) {
            // Interceptor'dan gelen formatlanmış hata mesajını gösteriyoruz.
            toast.error(err.message || 'Şifre sıfırlanırken bir hata oluştu.');
            setServerError(err.message || 'Bir hata oluştu.');
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f0f2f5' }}>
            <div style={{ padding: '40px', background: 'white', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', textAlign: 'center', width: '400px' }}>
                <img src={finchangeLogo} alt="FINCHANGE" style={{ maxWidth: '150px', marginBottom: '20px' }} />
                <h2 style={{ marginBottom: '10px' }}>Şifre Sıfırla</h2>
                <p style={{ color: '#666', marginBottom: '20px' }}>E-postanıza gönderilen kodu girin ve yeni şifrenizi belirleyin.</p>
                
                {/* Sunucu Hata Mesajı Alanı */}
                {serverError && (
                    <div style={{ color: 'red', marginBottom: '15px', padding: '10px', backgroundColor: '#ffebee', border: '1px solid red', borderRadius: '4px' }}>
                        {serverError}
                    </div>
                )}

                {/* Başarı Mesajı Alanı */}
                {successMessage && (
                     <div style={{ color: 'green', marginBottom: '15px', padding: '10px', backgroundColor: '#e8f5e9', border: '1px solid green', borderRadius: '4px' }}>
                        {successMessage}
                    </div>
                )}
                
                <form onSubmit={handleSubmit(onSubmit)}>
                    <input
                        type="text"
                        placeholder="Sıfırlama Kodu"
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            marginBottom: '5px', 
                            boxSizing: 'border-box',
                            border: `1px solid ${errors.token ? 'red' : '#ccc'}`, 
                            borderRadius: '4px' 
                        }}
                        {...register('token')}
                    />
                    {errors.token && <div style={{ color: 'red', marginBottom: '10px', fontSize: '14px', textAlign: 'left' }}>{errors.token.message}</div>}
                    
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
                        // Başarılı olunca da butonu devre dışı bırakalım
                        disabled={isSubmitting || !!successMessage} 
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            background: isSubmitting || successMessage ? '#9ca3af' : '#c8102e', 
                            color: 'white', 
                            border: 'none', 
                            borderRadius: '4px', 
                            cursor: 'pointer', 
                            fontSize: '16px',
                            marginBottom: '15px'
                        }}
                    >
                        {isSubmitting ? 'İŞLEM SÜRÜYOR...' : (successMessage ? 'BAŞARILI' : 'ŞİFREYİ SIFIRLA')}
                    </button>
                    
                    <button 
                        type="button" 
                        onClick={() => navigate('/login')}
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            background: 'transparent', 
                            color: '#666', 
                            border: '1px solid #ccc', 
                            borderRadius: '4px', 
                            cursor: 'pointer', 
                            fontSize: '16px'
                        }}
                    >
                        Giriş Sayfasına Dön
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ResetPasswordPage;