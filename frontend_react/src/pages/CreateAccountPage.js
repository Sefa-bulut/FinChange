import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
// Güncellenmiş servisimiz, component'e temiz veri sunduğu için bu kullanım değişmeden, doğru şekilde çalışır.
import { createAccountForClient } from '../services/customerAccountService';

const CreateAccountPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    // state'ten gelen verileri güvenli bir şekilde alalım.
    const { clientId, customerCode } = location.state || {};

    const [formData, setFormData] = useState({
        accountName: '',
        currency: 'TRY',
        initialBalance: ''
    });
    const [error, setError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    // Eğer sayfaya gerekli bilgiler olmadan gelinmişse, kullanıcıyı bilgilendir.
    if (!clientId || !customerCode) {
        return (
            <div style={{ padding: '24px', maxWidth: '600px', margin: '40px auto', fontFamily: 'sans-serif' }}>
                <h2>Hata</h2>
                <p style={{ color: '#ef4444', marginBottom: '20px' }}>
                    Gerekli müşteri bilgileri eksik. Lütfen bu sayfaya müşteri detayları üzerinden gelin.
                </p>
                <button
                    onClick={() => navigate(-1)} // Bir önceki sayfaya döner
                    style={{
                        padding: '10px 18px',
                        backgroundColor: '#3b82f6',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontWeight: '500'
                    }}
                >
                    Geri Dön
                </button>
            </div>
        );
    }

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Form doğrulama (validation)
        if (!formData.accountName.trim()) {
            setError("Hesap adı boş bırakılamaz.");
            return;
        }
        if (formData.initialBalance === '' || parseFloat(formData.initialBalance) < 0) {
            setError("Başlangıç bakiyesi 0 veya daha büyük olmalıdır.");
            return;
        }

        setIsLoading(true);
        setError('');
        setSuccessMessage('');

        try {
            const accountData = {
                accountName: formData.accountName.trim(),
                currency: formData.currency,
                initialBalance: parseFloat(formData.initialBalance)
            };

            // Servis çağrısı, `SuccessResponse`'i işleyip bize doğrudan
            // oluşturulan hesap nesnesini veya null döndürür. Bu yüzden burası değişmez.
            const createdAccount = await createAccountForClient(clientId, accountData);

            // Servisten geçerli bir yanıt gelip gelmediğini kontrol et.
            if (createdAccount && createdAccount.accountName) {
                // Başarı mesajını göster. Sunucudan dönen adı kullanmak daha güvenilirdir.
                setSuccessMessage(`'${createdAccount.accountName}' adlı hesap başarıyla oluşturuldu.`);

                // Başarılı işlem sonrası formu sıfırlayıp bekletmek kullanıcı deneyimini artırır.
                setFormData({ accountName: '', currency: 'TRY', initialBalance: '' });

                // 3 saniye sonra yönlendir
                setTimeout(() => {
                    navigate('/dashboard/account-records', {
                        state: { customerCode },
                        replace: true // Tarayıcı geçmişinde bu sayfanın üzerine yazar
                    });
                }, 3000);
            } else {
                // Servis `null` döndürdüyse veya beklenen formatta değilse
                throw new Error("Hesap oluşturuldu ancak sunucudan geçerli bir yanıt alınamadı.");
            }

        } catch (err) {
            // Interceptor'dan gelen formatlanmış hata mesajını göster.
            setError(`Hesap oluşturulamadı: ${err.message || "Bilinmeyen bir sunucu hatası oluştu."}`);
        } finally {
            // Hata olsa da olmasa da, işlem bitince loading durumunu kapat.
            setIsLoading(false); 
        }
    };

    const handleCancel = () => {
        navigate(-1); // Bir önceki sayfaya gider
    };

    return (
        <div style={{
            fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
            padding: '24px',
            maxWidth: '600px',
            margin: '40px auto',
            backgroundColor: '#f9fafb',
            borderRadius: '8px',
            boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
        }}>
            <h1>Yeni Hesap Oluştur</h1>
            <p style={{ color: '#4b5563', marginTop: '-10px', marginBottom: '24px' }}>
                Müşteri Kodu: <strong>{customerCode}</strong>
            </p>

            {error && (
                <div style={{
                    color: '#991b1b',
                    backgroundColor: '#fee2e2',
                    padding: '12px',
                    borderRadius: '6px',
                    marginBottom: '16px',
                    border: '1px solid #fca5a5'
                }}>
                    <strong>Hata:</strong> {error}
                </div>
            )}

            {successMessage && (
                <div style={{
                    color: '#166534',
                    backgroundColor: '#dcfce7',
                    padding: '12px',
                    borderRadius: '6px',
                    marginBottom: '16px',
                    border: '1px solid #a7f3d0',
                    fontWeight: '500'
                }}>
                    {successMessage}
                </div>
            )}

            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '16px' }}>
                    <label htmlFor="accountName" style={{ display: 'block', marginBottom: '4px', fontWeight: '500' }}>
                        Hesap Adı
                    </label>
                    <input
                        id="accountName"
                        type="text"
                        name="accountName"
                        value={formData.accountName}
                        onChange={handleChange}
                        placeholder="Örn: Maaş Hesabı, Birikim Hesabı"
                        required
                        style={{
                            width: '100%',
                            boxSizing: 'border-box',
                            padding: '10px',
                            border: '1px solid #d1d5db',
                            borderRadius: '6px',
                            fontSize: '16px'
                        }}
                    />
                </div>

                <div style={{ marginBottom: '16px' }}>
                    <label htmlFor="currency" style={{ display: 'block', marginBottom: '4px', fontWeight: '500' }}>
                        Para Birimi
                    </label>
                    <select
                        id="currency"
                        name="currency"
                        value={formData.currency}
                        onChange={handleChange}
                        style={{
                            width: '100%',
                            boxSizing: 'border-box',
                            padding: '10px',
                            border: '1px solid #d1d5db',
                            borderRadius: '6px',
                            fontSize: '16px',
                            backgroundColor: 'white'
                        }}
                    >
                        <option value="TRY">TRY - Türk Lirası</option>
                        <option value="USD">USD - ABD Doları</option>
                        <option value="EUR">EUR - Euro</option>
                        <option value="GBP">GBP - İngiliz Sterlini</option>
                    </select>
                </div>

                <div style={{ marginBottom: '24px' }}>
                    <label htmlFor="initialBalance" style={{ display: 'block', marginBottom: '4px', fontWeight: '500' }}>
                        Başlangıç Bakiyesi
                    </label>
                    <input
                        id="initialBalance"
                        type="number"
                        step="0.01"
                        min="0"
                        name="initialBalance"
                        value={formData.initialBalance}
                        onChange={handleChange}
                        placeholder="0.00"
                        required
                        style={{
                            width: '100%',
                            boxSizing: 'border-box',
                            padding: '10px',
                            border: '1px solid #d1d5db',
                            borderRadius: '6px',
                            fontSize: '16px'
                        }}
                    />
                </div>

                <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                    <button
                        type="button"
                        onClick={handleCancel}
                        disabled={!!successMessage} // Başarılı olunca iptal butonunu devre dışı bırak
                        style={{
                            padding: '10px 20px',
                            backgroundColor: '#6b7280',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontWeight: '500'
                        }}
                    >
                        İptal
                    </button>
                    <button
                        type="submit"
                        disabled={isLoading || !!successMessage}
                        style={{
                            padding: '10px 20px',
                            backgroundColor: isLoading || successMessage ? '#9ca3af' : '#10b981',
                            color: 'white',
                            border: 'none',
                            borderRadius: '6px',
                            cursor: isLoading || successMessage ? 'not-allowed' : 'pointer',
                            fontWeight: '500',
                            minWidth: '150px' // Buton içeriği değişince zıplamaması için
                        }}
                    >
                        {isLoading ? 'Oluşturuluyor...' : (successMessage ? 'Başarılı ✓' : 'Hesabı Oluştur')}
                    </button>
                </div>
            </form>
        </div>
    );
};

export default CreateAccountPage;