import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { forgotPasswordSchema } from '../validation/userSchemas';
import { forgotPassword } from '../services/authService';
import finchangeLogo from '../assets/images/finchange-logo.png';
import { toast } from 'react-toastify';

const ForgotPasswordPage = () => {
    const navigate = useNavigate();
    // Sunucudan gelen hata ve başarı mesajlarını yönetmek için ayrı state'ler
    const [serverError, setServerError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
        resolver: yupResolver(forgotPasswordSchema),
        defaultValues: {
            email: '',
        }
    });

    const onSubmit = async (data) => {
        setServerError('');
        setSuccessMessage('');
        try {
            // GÜNCELLENDİ: Servis artık doğrudan sunucudan gelen başarı mesajını (string) döndürür.
            const messageFromServer = await forgotPassword(data.email);

            if (messageFromServer) {
                // Sunucudan gelen mesajı gösteriyoruz.
                toast.success(messageFromServer);
                setSuccessMessage(messageFromServer + " Yönlendiriliyorsunuz...");

                // Başarılı olduğunda 3 saniye sonra ResetPasswordPage'e yönlendir.
                setTimeout(() => {
                    navigate('/reset-password');
                }, 3000);
            } else {
                // Beklenmedik bir durum: Servis `null` döndürdü.
                throw new Error("Sunucudan geçerli bir yanıt alınamadı.");
            }
        } catch (err) {
            // Interceptor'dan gelen formatlanmış hata mesajını göster.
            toast.error(err.message || 'Bir hata oluştu.');
            setServerError(err.message || 'Bir hata oluştu.');
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f0f2f5' }}>
            <div style={{ padding: '40px', background: 'white', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', textAlign: 'center', width: '400px' }}>
                <img src={finchangeLogo} alt="FINCHANGE" style={{ maxWidth: '150px', marginBottom: '20px' }} />
                <h2 style={{ marginBottom: '10px' }}>Şifremi Unuttum</h2>
                <p style={{ color: '#666', marginBottom: '20px' }}>Sıfırlama kodu almak için e-posta adresinizi girin.</p>
                
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
                        type="email"
                        placeholder="E-Posta Adresiniz"
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            marginBottom: '5px',
                            boxSizing: 'border-box', 
                            border: `1px solid ${errors.email ? 'red' : '#ccc'}`, 
                            borderRadius: '4px' 
                        }}
                        {...register('email')}
                    />
                    {errors.email && <div style={{ color: 'red', marginBottom: '15px', fontSize: '14px', textAlign: 'left' }}>{errors.email.message}</div>}
                    
                    <button 
                        type="submit" 
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
                            marginTop: '10px'
                        }}
                    >
                        {isSubmitting ? 'GÖNDERİLİYOR...' : (successMessage ? 'GÖNDERİLDİ' : 'SIFIRLAMA KODU GÖNDER')}
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
                            fontSize: '16px',
                            marginTop: '15px'
                        }}
                    >
                        Giriş Ekranına Geri Dön
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ForgotPasswordPage;