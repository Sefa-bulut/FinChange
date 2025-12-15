import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getClientById, updateClient } from '../services/customerService';

// --- Stil Objeleri ---
const inputStyle = (hasError) => ({
    width: '100%',
    padding: '12px',
    borderRadius: '8px',
    border: `2px solid ${hasError ? '#e74c3c' : '#ddd'}`,
    marginTop: '8px',
    marginBottom: '16px',
    fontSize: '14px',
    transition: 'border-color 0.3s ease',
    backgroundColor: '#fff',
});

const buttonStyle = (variant = 'primary', disabled = false) => ({
    padding: '12px 24px',
    borderRadius: '8px',
    border: 'none',
    fontSize: '14px',
    fontWeight: '600',
    cursor: disabled ? 'not-allowed' : 'pointer',
    transition: 'all 0.3s ease',
    backgroundColor: variant === 'primary'
        ? (disabled ? '#bdc3c7' : '#a22217')
        : '#ecf0f1',
    color: variant === 'primary' ? 'white' : '#2c3e50',
    opacity: disabled ? 0.6 : 1,
});

const sectionStyle = {
    backgroundColor: '#fff',
    padding: '24px',
    borderRadius: '12px',
    marginBottom: '24px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.05)',
};

const labelStyle = {
    display: 'block',
    fontWeight: '600',
    color: '#495057',
    marginBottom: '4px',
};

// --- Ana Bileşen ---
export default function CustomerEditPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [clientData, setClientData] = useState(null);
    const [formData, setFormData] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [error, setError] = useState(null);

    // 1. Mevcut müşteri verilerini yükle
    useEffect(() => {
        getClientById(id)
            .then(data => {
                setClientData(data);
                // Formu backend'den gelen verilerle doldur
                setFormData({
                    telefon: data.telefon || '',
                    email: data.email || '',
                    adres: data.adres || '',
                    status: data.durum || 'Aktif', // GÖREV 1: Durum alanı eklendi
                    ad: data.ad || '',
                    soyad: data.soyad || '',
                    tcKimlikNo: data.tckn || '',
                    dogumTarihi: data.dogumTarihi || '',
                    sirketUnvani: data.sirketUnvani || '',
                    vergiKimlikNo: data.vergiNo || '',
                    mersisNo: data.mersisNo || '',
                    yetkiliKisiAdSoyad: data.yetkiliKisiAdSoyad || '',
                    suitabilityProfile: data.suitabilityProfile || {},
                });
                setIsLoading(false);
            })
            .catch(err => {
                setError('Müşteri verileri yüklenemedi.');
                setIsLoading(false);
            });
    }, [id]);

    // 2. Formdaki değişiklikleri state'e yansıt
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleProfileChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            suitabilityProfile: {
                ...prev.suitabilityProfile,
                [name]: value,
            },
        }));
    };

    // 3. Formu gönder
    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSaving(true);
        setError(null);

        try {
            // Backend'e gönderilecek payload'ı hazırla
            const updatePayload = { ...formData };

            await updateClient(id, updatePayload);
            alert('Müşteri başarıyla güncellendi!');
            navigate(`/dashboard/clients/${id}`);
        } catch (err) {
            setError(err.message || 'Güncelleme sırasında bir hata oluştu.');
        } finally {
            setIsSaving(false);
        }
    };

    if (isLoading || !formData) return <div style={{ padding: '2rem' }}>Yükleniyor...</div>;
    if (error) return <div style={{ color: 'red', padding: '2rem' }}>HATA: {error}</div>;

    const isCorporate = clientData.customerType === 'TUZEL';

    // --- RENDER KISMI ---
    return (
        <div style={{ maxWidth: '900px', margin: '40px auto', padding: '20px', backgroundColor: '#f8f9fa' }}>
            <h2 style={{ marginBottom: '30px', color: '#2c3e50', textAlign: 'center' }}>
                Müşteri Düzenle: {clientData.gorunenAd}
            </h2>

            <form onSubmit={handleSubmit}>
                {/* Bireysel veya Kurumsal Bilgiler */}
                <div style={sectionStyle}>
                    <h3 style={{ marginBottom: '20px' }}>{isCorporate ? 'Şirket Bilgileri' : 'Kişisel Bilgiler'}</h3>
                    {isCorporate ? (
                        <>
                            <label style={labelStyle}>Şirket Ünvanı</label>
                            <input name="sirketUnvani" value={formData.sirketUnvani} onChange={handleChange} style={inputStyle()} />
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                                <div><label style={labelStyle}>Vergi Kimlik No</label><input name="vergiKimlikNo" value={formData.vergiKimlikNo} onChange={handleChange} style={inputStyle()} /></div>
                                <div><label style={labelStyle}>Mersis No</label><input name="mersisNo" value={formData.mersisNo} onChange={handleChange} style={inputStyle()} /></div>
                            </div>
                            <label style={labelStyle}>Yetkili Kişi</label>
                            <input name="yetkiliKisiAdSoyad" value={formData.yetkiliKisiAdSoyad} onChange={handleChange} style={inputStyle()} />
                        </>
                    ) : (
                        <>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                                <div><label style={labelStyle}>Ad</label><input name="ad" value={formData.ad} onChange={handleChange} style={inputStyle()} /></div>
                                <div><label style={labelStyle}>Soyad</label><input name="soyad" value={formData.soyad} onChange={handleChange} style={inputStyle()} /></div>
                            </div>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                                <div><label style={labelStyle}>T.C. Kimlik No</label><input name="tcKimlikNo" value={formData.tcKimlikNo} onChange={handleChange} style={inputStyle()} /></div>
                                <div><label style={labelStyle}>Doğum Tarihi</label><input type="date" name="dogumTarihi" value={formData.dogumTarihi} onChange={handleChange} style={inputStyle()} /></div>
                            </div>
                        </>
                    )}
                </div>

                {/* İletişim ve Durum Bilgileri */}
                <div style={sectionStyle}>
                    <h3 style={{ marginBottom: '20px' }}>İletişim ve Durum</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        <div><label style={labelStyle}>E-posta</label><input type="email" name="email" value={formData.email} onChange={handleChange} style={inputStyle()} /></div>
                        <div><label style={labelStyle}>Telefon</label><input type="tel" name="telefon" value={formData.telefon} onChange={handleChange} style={inputStyle()} /></div>
                    </div>
                    <label style={labelStyle}>Adres</label>
                    <textarea name="adres" value={formData.adres} onChange={handleChange} style={{ ...inputStyle(), minHeight: '80px' }} rows={3} />
                    <div>
                        <label style={labelStyle}>Müşteri Durumu</label>
                        <select name="status" value={formData.status} onChange={handleChange} style={inputStyle()}>
                            <option value="Aktif">Aktif</option>
                            <option value="Pasif">Pasif</option>

                        </select>
                    </div>
                </div>

                {/* GÖREV 2: Yerindelik Profili (Tüm Alanlar) */}
                <div style={sectionStyle}>
                    <h3 style={{ marginBottom: '20px' }}>Yerindelik Profili</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        {isCorporate ? (
                            <>
                                <div><label style={labelStyle}>Şirket Yatırım Stratejisi</label><select name="sirketYatirimStratejisi" value={formData.suitabilityProfile.sirketYatirimStratejisi || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Muhafazakar">Muhafazakar</option><option value="Dengeli">Dengeli</option><option value="Agresif">Agresif</option></select></div>
                                <div><label style={labelStyle}>Risk Yönetimi Politikası</label><select name="riskYonetimiPolitikasi" value={formData.suitabilityProfile.riskYonetimiPolitikasi || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Sıkı Risk Kontrolü">Sıkı Risk Kontrolü</option><option value="Orta Düzey Risk Kontrolü">Orta Düzey</option></select></div>
                                <div><label style={labelStyle}>Finansal Durum</label><select name="finansalDurumTuzel" value={formData.suitabilityProfile.finansalDurumTuzel || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Zayıf">Zayıf</option><option value="Orta">Orta</option><option value="İyi">İyi</option></select></div>
                                <div><label style={labelStyle}>Yatırım Vadesi</label><select name="yatirimSuresiVadeTuzel" value={formData.suitabilityProfile.yatirimSuresiVadeTuzel || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Kısa Vadeli (1 yıldan az)">Kısa Vade</option><option value="Orta Vadeli (1-3 yıl)">Orta Vade</option><option value="Uzun Vadeli (3+ yıl)">Uzun Vade</option></select></div>
                                <div>
            <label style={labelStyle}>Yatırım Amacı (Ortak)</label>
            <select name="yatirimAmaci" value={formData.suitabilityProfile.yatirimAmaci || ''} onChange={handleProfileChange} style={inputStyle()}>
                <option value="">Seçiniz</option>
                <option value="Sermaye Korunması">Sermaye Korunması</option>
                <option value="Gelir Elde Etme">Gelir Elde Etme</option>
                <option value="Büyüme">Büyüme</option>
            </select>
        </div>
        <div>
            <label style={labelStyle}>Risk Toleransı (Ortak)</label>
            <select name="riskToleransi" value={formData.suitabilityProfile.riskToleransi || ''} onChange={handleProfileChange} style={inputStyle()}>
                <option value="">Seçiniz</option>
                <option value="Düşük">Düşük</option>
                <option value="Orta">Orta</option>
                <option value="Yüksek">Yüksek</option>
            </select>
        </div>
        <div>
            <label style={labelStyle}>Likidite İhtiyacı (Ortak)</label>
            <select name="likiditeIhtiyaci" value={formData.suitabilityProfile.likiditeIhtiyaci || ''} onChange={handleProfileChange} style={inputStyle()}>
                <option value="">Seçiniz</option>
                <option value="Yüksek">Yüksek</option>
                <option value="Orta">Orta</option>
                <option value="Düşük">Düşük</option>
            </select>
        </div>
                            </>
                        ) : (
                            <>
                                <div><label style={labelStyle}>Yatırım Amacı</label><select name="yatirimAmaci" value={formData.suitabilityProfile.yatirimAmaci || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Sermaye Korunması">Sermaye Korunması</option><option value="Gelir Elde Etme">Gelir</option><option value="Büyüme">Büyüme</option></select></div>
                                <div><label style={labelStyle}>Yatırım Süresi</label><select name="yatirimSuresi" value={formData.suitabilityProfile.yatirimSuresi || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Kısa Vadeli (1 yıldan az)">Kısa Vade</option><option value="Orta Vadeli (1-3 yıl)">Orta Vade</option><option value="Uzun Vadeli (3+ yıl)">Uzun Vade</option></select></div>
                                <div><label style={labelStyle}>Risk Toleransı</label><select name="riskToleransi" value={formData.suitabilityProfile.riskToleransi || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Düşük">Düşük</option><option value="Orta">Orta</option><option value="Yüksek">Yüksek</option></select></div>
                                <div><label style={labelStyle}>Mali Durum</label><select name="maliDurum" value={formData.suitabilityProfile.maliDurum || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Düşük">Düşük</option><option value="Orta">Orta</option><option value="İyi">İyi</option></select></div>
                                <div><label style={labelStyle}>Yatırım Deneyimi</label><select name="yatirimDeneyimi" value={formData.suitabilityProfile.yatirimDeneyimi || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Yok">Yok</option><option value="Az">Az</option><option value="Orta">Orta</option><option value="İyi">İyi</option></select></div>
                                <div><label style={labelStyle}>Likidite İhtiyacı</label><select name="likiditeIhtiyaci" value={formData.suitabilityProfile.likiditeIhtiyaci || ''} onChange={handleProfileChange} style={inputStyle()}><option value="">Seçiniz</option><option value="Yüksek">Yüksek</option><option value="Orta">Orta</option><option value="Düşük">Düşük</option></select></div>
                            </>
                        )}
                    </div>
                </div>

                {/* Butonlar */}
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '30px' }}>
                    <button type="button" onClick={() => navigate(`/dashboard/clients/${id}`)} style={buttonStyle('secondary')}>İptal</button>
                    <button type="submit" disabled={isSaving} style={buttonStyle('primary', isSaving)}>{isSaving ? 'Kaydediliyor...' : 'Değişiklikleri Kaydet'}</button>
                </div>
            </form>
        </div>
    );
}