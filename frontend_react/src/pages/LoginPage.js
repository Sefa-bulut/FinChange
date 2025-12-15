import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { loginSchema } from '../validation/userSchemas';
import { useAuth } from '../context/AuthContext';
import finchangeLogo from '../assets/images/finchange-logo.png';

const LoginPage = () => {
    const navigate = useNavigate();
    const { login } = useAuth();
    const { register, handleSubmit, formState: { errors, isSubmitting }, setError: setFormError } = useForm({
        resolver: yupResolver(loginSchema),
        defaultValues: {
            email: '',
            password: '',
        }
    });

    const onSubmit = async (data) => {
        try {
            const { mustChangePassword } = await login(data.email, data.password);
            if (mustChangePassword) {
                navigate('/force-change-password');
            } else {
                navigate('/dashboard');
            }
        } catch (err) {
            setFormError('root', { 
                type: 'server', 
                message: err.message || 'Giriş başarısız. Lütfen bilgilerinizi kontrol edin.' 
            });
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f0f2f5' }}>
            <div style={{ padding: '40px', background: 'white', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)', textAlign: 'center', width: '400px' }}>
                <img src={finchangeLogo} alt="FINCHANGE" style={{ maxWidth: '150px', marginBottom: '20px' }} />
                <form onSubmit={handleSubmit(onSubmit)}>
                    <input
                        type="email"
                        placeholder="E-Posta"
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            marginBottom: '5px', 
                            border: `1px solid ${errors.email ? 'red' : '#ccc'}`, 
                            borderRadius: '4px' 
                        }}
                        {...register('email')}
                    />
                    {errors.email && <div style={{ color: 'red', marginBottom: '10px', fontSize: '14px' }}>{errors.email.message}</div>}
                    
                    <input
                        type="password"
                        placeholder="Şifre"
                        style={{ 
                            width: '100%', 
                            padding: '12px', 
                            marginBottom: '5px', 
                            border: `1px solid ${errors.password ? 'red' : '#ccc'}`, 
                            borderRadius: '4px' 
                        }}
                        {...register('password')}
                    />
                    {errors.password && <div style={{ color: 'red', marginBottom: '15px', fontSize: '14px' }}>{errors.password.message}</div>}
                    
                    <button type="submit" disabled={isSubmitting} style={{ width: '100%', padding: '12px', background: '#c8102e', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '16px' }}>
                        {isSubmitting ? 'GİRİŞ YAPILIYOR...' : 'GİRİŞ YAP'}
                    </button>
                    {errors.root && <div style={{ color: 'red', marginTop: '15px' }}>{errors.root.message}</div>}
                    <a href="/forgot-password" style={{ display: 'block', marginTop: '15px', color: '#555', textDecoration: 'none' }}>
                        Şifremi Unuttum
                    </a>
                </form>
            </div>
        </div>
    );
};

export default LoginPage;