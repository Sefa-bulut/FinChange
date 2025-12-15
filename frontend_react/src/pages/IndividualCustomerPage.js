import React, { useState } from 'react';
import { createClient } from '../services/customerService';

// DEĞİŞİKLİK: T.C. Kimlik No doğrulama fonksiyonu esnekleştirildi.
const isValidTurkishId = (tckn) => {
  // Geliştirme ve test için esnek validasyon:
  // Sadece 11 haneli mi, ilk rakam 0 mı ve son rakam çift mi diye kontrol eder.
  if (typeof tckn !== 'string' || tckn.length !== 11) return false;

  const digits = tckn.split('').map(Number);

  // KURAL 1: İlk rakam 0 olamaz.
  if (digits[0] === 0) {
    console.log("TCKN Hata: İlk rakam 0 olamaz.");
    return false;
  }

  // KURAL 2: Son rakam çift olmalı.
  if (digits[10] % 2 !== 0) {
    console.log("TCKN Hata: Son rakam çift olmalı.");
    return false;
  }

  // Bu iki kuralı geçiyorsa, test için geçerli kabul et.
  return true;

  /*
  // --- GERÇEK HAYAT ALGORİTMASI (İLERİDE AKTİF EDİLECEK) ---
  // Uygulamanız canlıya çıkarken aşağıdaki bloğu aktif edip üsttekini silebilirsiniz.

  if (typeof tckn !== 'string' || tckn.length !== 11) return false;

  const digits = tckn.split('').map(Number);

  if (digits[0] === 0) return false;
  if (digits[10] % 2 !== 0) return false;

  const oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
  const evenSum = digits[1] + digits[3] + digits[5] + digits[7];
  const tenthDigit = ((oddSum * 7) - evenSum + 10) % 10;

  if (tenthDigit !== digits[9]) return false;

  const sumOfFirst10 = digits.slice(0, 10).reduce((sum, digit) => sum + digit, 0);
  const eleventhDigit = sumOfFirst10 % 10;

  return eleventhDigit === digits[10];
  */
};

const inputStyle = (hasError) => ({
  width: '100%',
  padding: '12px',
  borderRadius: '8px',
  border: `2px solid ${hasError ? '#e74c3c' : '#ddd'}`,
  marginTop: '8px',
  fontSize: '14px',
  transition: 'border-color 0.3s ease',
});

const errorStyle = {
  color: '#e74c3c',
  fontSize: '12px',
  marginTop: '4px',
  fontWeight: '500',
};

const stepperStyle = {
  display: 'flex',
  justifyContent: 'center',
  marginBottom: '40px',
  padding: '0 20px',
};

const stepStyle = (isActive, isCompleted) => ({
  display: 'flex',
  alignItems: 'center',
  margin: '0 20px',
  color: isActive ? '#a22217' : isCompleted ? '#27ae60' : '#bdc3c7',
  fontWeight: isActive ? 'bold' : 'normal',
});

const stepCircleStyle = (isActive, isCompleted) => ({
  width: '32px',
  height: '32px',
  borderRadius: '50%',
  backgroundColor: isActive ? '#a22217' : isCompleted ? '#27ae60' : '#bdc3c7',
  color: 'white',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  marginRight: '8px',
  fontSize: '14px',
  fontWeight: 'bold',
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
    : (disabled ? '#ecf0f1' : '#ecf0f1'),
  color: variant === 'primary'
    ? 'white'
    : (disabled ? '#bdc3c7' : '#2c3e50'),
  opacity: disabled ? 0.6 : 1,
});

export default function IndividualClientFormPage() {
  const [currentStep, setCurrentStep] = useState(1);
  const [completedSteps, setCompletedSteps] = useState([]);

  // DEĞİŞİKLİK: State'ler sadece kullanıcı girdisini tutacak şekilde ayarlandı.
  const [personalInfo, setPersonalInfo] = useState({
    ad: '',
    soyad: '',
    dogumTarihi: '',
    tckn: '',
    telefon: '', // Sadece '5xxxxxxxxx'
    email: '',
    adres: '',
    musteriKodu: '', // Sadece 'BRYSL-' sonrası
  });

  const [suitabilityProfile, setSuitabilityProfile] = useState({
    yatirimAmaci: '',
    yatirimSuresi: '',
    riskToleransi: '',
    maliDurum: '',
    yatirimDeneyimi: '',
    likiditeIhtiyaci: '',
    vergiDurumu: '',
  });

  const [documents, setDocuments] = useState({
    kvkkBelgesi: null,
    portfoyYonetimSozlesmesi: null,
    elektronikBildirimIzni: null,
  });

  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);

  // Validation functions for each step
  const validatePersonalInfo = () => {
    const newErrors = {};

    if (!personalInfo.ad.trim()) {
      newErrors.ad = 'Ad zorunludur.';
    } else if (!/^[a-zA-ZğüşıöçĞÜŞİÖÇ\s]+$/.test(personalInfo.ad.trim())) {
      newErrors.ad = 'Ad sadece harf içermelidir.';
    }

    if (!personalInfo.soyad.trim()) {
      newErrors.soyad = 'Soyad zorunludur.';
    } else if (!/^[a-zA-ZğüşıöçĞÜŞİÖÇ\s]+$/.test(personalInfo.soyad.trim())) {
      newErrors.soyad = 'Soyad sadece harf içermelidir.';
    }

    if (!personalInfo.dogumTarihi) {
      newErrors.dogumTarihi = 'Doğum tarihi zorunludur.';
    } else if (new Date(personalInfo.dogumTarihi) > new Date()) {
      newErrors.dogumTarihi = 'Gelecekte bir tarih girilemez.';
    }

    // DEĞİŞİKLİK: TCKN validasyonu güncellendi.
    if (!personalInfo.tckn) {
      newErrors.tckn = 'T.C. Kimlik No zorunludur.';
    } else if (!/^\d{11}$/.test(personalInfo.tckn)) {
      newErrors.tckn = 'T.C. Kimlik No 11 rakamdan oluşmalıdır.';
    } else if (!isValidTurkishId(personalInfo.tckn)) {
      // isValidTurkishId fonksiyonu zaten ilk rakamın 0 olmamasını ve son rakamın çift olmasını kontrol ediyor.
      newErrors.tckn = 'Geçersiz T.C. Kimlik No. Lütfen kontrol ediniz.';
    }

    if (!personalInfo.telefon) {
      newErrors.telefon = 'Telefon zorunludur.';
    } else if (!/^\d{10}$/.test(personalInfo.telefon)) {
      newErrors.telefon = 'Telefon 10 haneli rakam olmalıdır.';
    } else if (!personalInfo.telefon.startsWith('5')) {
      newErrors.telefon = 'Telefon 5 ile başlamalıdır.';
    } else if (/[a-zA-Z\-\s]/.test(personalInfo.telefon)) {
      newErrors.telefon = 'Telefon sadece rakam içermelidir.';
    }

    if (!personalInfo.email) {
      newErrors.email = 'E-posta zorunludur.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(personalInfo.email)) {
      newErrors.email = 'Geçersiz e-posta adresi.';
    }

    if (!personalInfo.adres.trim()) {
      newErrors.adres = 'Adres zorunludur.';
    }

    // DEĞİŞİKLİK: Müşteri kodu validasyonu güncellendi.
    if (!personalInfo.musteriKodu.trim()) {
      newErrors.musteriKodu = 'Müşteri kodu zorunludur.';
    } else if (!/^[A-Za-z0-9]+$/.test(personalInfo.musteriKodu)) {
      newErrors.musteriKodu = 'Müşteri kodu sadece harf ve rakam içerebilir.';
    }

    return newErrors;
  };

  const validateSuitabilityProfile = () => {
    const newErrors = {};

    if (!suitabilityProfile.yatirimAmaci) newErrors.yatirimAmaci = 'Yatırım amacı seçiniz.';
    if (!suitabilityProfile.yatirimSuresi) newErrors.yatirimSuresi = 'Yatırım süresi seçiniz.';
    if (!suitabilityProfile.riskToleransi) newErrors.riskToleransi = 'Risk toleransı seçiniz.';
    if (!suitabilityProfile.maliDurum) newErrors.maliDurum = 'Mali durum seçiniz.';
    if (!suitabilityProfile.yatirimDeneyimi) newErrors.yatirimDeneyimi = 'Yatırım deneyimi seçiniz.';
    if (!suitabilityProfile.likiditeIhtiyaci) newErrors.likiditeIhtiyaci = 'Likidite ihtiyacı seçiniz.';
    if (!suitabilityProfile.vergiDurumu) newErrors.vergiDurumu = 'Vergi durumu seçiniz.';

    return newErrors;
  };

  // Input change handlers
  const handlePersonalInfoChange = (e) => {
    const { name, value } = e.target;
    // DEĞİŞİKLİK: Müşteri kodu için sadece alfanümerik karakterlere izin ver
    if (name === 'musteriKodu') {
      const sanitizedValue = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
      setPersonalInfo((prev) => ({ ...prev, [name]: sanitizedValue }));
    } else {
      setPersonalInfo((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSuitabilityChange = (e) => {
    const { name, value } = e.target;
    setSuitabilityProfile((prev) => ({ ...prev, [name]: value }));
  };

  const handleFileChange = (e) => {
    const { name, files } = e.target;
    if (files && files[0]) {
      setDocuments((prev) => ({ ...prev, [name]: files[0] }));
    }
  };

  // Step navigation
  const nextStep = () => {
    let validationErrors = {};

    if (currentStep === 1) {
      validationErrors = validatePersonalInfo();
      if (Object.keys(validationErrors).length === 0) {
        setCompletedSteps(prev => [...prev.filter(s => s !== 1), 1]);
        setCurrentStep(2);
      }
    } else if (currentStep === 2) {
      validationErrors = validateSuitabilityProfile();
      if (Object.keys(validationErrors).length === 0) {
        setCompletedSteps(prev => [...prev.filter(s => s !== 2), 2]);
        setCurrentStep(3);
      }
    } else if (currentStep === 3) {
      setCompletedSteps(prev => [...prev.filter(s => s !== 3), 3]);
      setCurrentStep(4);
    }

    setErrors(validationErrors);
  };

  const prevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
      setErrors({});
    }
  };

  const goToStep = (step) => {
    if (completedSteps.includes(step - 1) || step === 1) {
      setCurrentStep(step);
      setErrors({});
    }
  };

  // Final submit
  const handleSubmit = async () => {
    setIsLoading(true);

    // DEĞİŞİKLİK: Backend'e göndermeden önce musteriKodu ve telefon birleştiriliyor.
    const clientData = {
      musteriKodu: `BRYSL-${personalInfo.musteriKodu}`,
      customerType: 'GERCEK',
      telefon: `+90${personalInfo.telefon}`,
      email: personalInfo.email,
      adres: personalInfo.adres,
      ad: personalInfo.ad,
      soyad: personalInfo.soyad,
      tcKimlikNo: personalInfo.tckn,
      dogumTarihi: personalInfo.dogumTarihi,
      suitabilityProfile: suitabilityProfile,
    };

    try {
      await createClient(clientData, documents);
      alert('Müşteri başarıyla kaydedildi!');

      // Müşteri listesi sayfasına yönlendir
      window.location.href = '/dashboard/clients';
      return;

    } catch (error) {
      console.error('Kayıt hatası:', error.response?.data || error.message);
      alert('Kayıt sırasında hata oluştu. Konsolu kontrol edin.');
    } finally {
      setIsLoading(false);
    }
  };

  // Render step content
  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return renderPersonalInfoStep();
      case 2:
        return renderSuitabilityProfileStep();
      case 3:
        return renderDocumentsStep();
      case 4:
        return renderSummaryStep();
      default:
        return null;
    }
  };

  const renderPersonalInfoStep = () => (
    <div>
      <h3 style={{ marginBottom: '24px', color: '#2c3e50' }}>Kişisel Bilgiler</h3>

      {/* DEĞİŞİKLİK: Müşteri Kodu input'u güncellendi */}
      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
          Müşteri Kodu *
        </label>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <span style={{
            padding: '12px',
            backgroundColor: '#e9ecef',
            border: '2px solid #ddd',
            borderRight: 'none',
            borderRadius: '8px 0 0 8px',
            fontSize: '14px',
            color: '#495057',
            fontWeight: 'bold'
          }}>
            BRYSL-
          </span>
          <input
            type="text"
            name="musteriKodu"
            value={personalInfo.musteriKodu}
            onChange={handlePersonalInfoChange}
            placeholder="Örn: XYZ456"
            style={{
              ...inputStyle(errors.musteriKodu),
              borderRadius: '0 8px 8px 0',
              marginTop: 0,
            }}
          />
        </div>
        {errors.musteriKodu && <div style={errorStyle}>{errors.musteriKodu}</div>}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Ad *
          </label>
          <input
            type="text"
            name="ad"
            value={personalInfo.ad}
            onChange={handlePersonalInfoChange}
            style={inputStyle(errors.ad)}
          />
          {errors.ad && <div style={errorStyle}>{errors.ad}</div>}
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Soyad *
          </label>
          <input
            type="text"
            name="soyad"
            value={personalInfo.soyad}
            onChange={handlePersonalInfoChange}
            style={inputStyle(errors.soyad)}
          />
          {errors.soyad && <div style={errorStyle}>{errors.soyad}</div>}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            T.C. Kimlik No *
          </label>
          <input
            type="text"
            name="tckn"
            value={personalInfo.tckn}
            onChange={(e) => {
              // Sadece rakam girişine izin ver
              const value = e.target.value.replace(/[^0-9]/g, '');
              handlePersonalInfoChange({ target: { name: 'tckn', value } });
            }}
            maxLength={11}
            placeholder="11 haneli T.C. Kimlik No"
            style={inputStyle(errors.tckn)}
          />
          {errors.tckn && <div style={errorStyle}>{errors.tckn}</div>}
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Doğum Tarihi *
          </label>
          <input
            type="date"
            name="dogumTarihi"
            value={personalInfo.dogumTarihi}
            onChange={handlePersonalInfoChange}
            style={inputStyle(errors.dogumTarihi)}
          />
          {errors.dogumTarihi && <div style={errorStyle}>{errors.dogumTarihi}</div>}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Telefon *
          </label>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <span style={{
              padding: '12px',
              backgroundColor: '#e9ecef',
              border: '2px solid #ddd',
              borderRight: 'none',
              borderRadius: '8px 0 0 8px',
              fontSize: '14px',
              color: '#495057'
            }}>
              +90
            </span>
            <input
              type="tel"
              name="telefon"
              value={personalInfo.telefon}
              onChange={(e) => {
                const value = e.target.value.replace(/[^0-9]/g, '');
                handlePersonalInfoChange({ target: { name: 'telefon', value } });
              }}
              placeholder="5xxxxxxxxx"
              maxLength="10"
              style={{
                ...inputStyle(errors.telefon),
                borderRadius: '0 8px 8px 0',
                marginTop: 0,
              }}
            />
          </div>
          {errors.telefon && <div style={errorStyle}>{errors.telefon}</div>}
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            E-Posta *
          </label>
          <input
            type="email"
            name="email"
            value={personalInfo.email}
            onChange={handlePersonalInfoChange}
            style={inputStyle(errors.email)}
          />
          {errors.email && <div style={errorStyle}>{errors.email}</div>}
        </div>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
          Adres *
        </label>
        <textarea
          name="adres"
          value={personalInfo.adres}
          onChange={handlePersonalInfoChange}
          style={inputStyle(errors.adres)}
          rows={3}
        />
        {errors.adres && <div style={errorStyle}>{errors.adres}</div>}
      </div>
    </div>
  );

  const renderSuitabilityProfileStep = () => (
    <div>
      <h3 style={{ marginBottom: '24px', color: '#2c3e50' }}>Yerindelik Profili</h3>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Yatırım Amacı *
          </label>
          <select
            name="yatirimAmaci"
            value={suitabilityProfile.yatirimAmaci}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.yatirimAmaci)}
          >
            <option value="">Seçiniz</option>
            <option value="Sermaye Korunması">Sermaye Korunması</option>
            <option value="Gelir Elde Etme">Gelir Elde Etme</option>
            <option value="Büyüme">Büyüme</option>
            <option value="Spekülatif">Spekülatif</option>
          </select>
          {errors.yatirimAmaci && <div style={errorStyle}>{errors.yatirimAmaci}</div>}
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Yatırım Süresi *
          </label>
          <select
            name="yatirimSuresi"
            value={suitabilityProfile.yatirimSuresi}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.yatirimSuresi)}
          >
            <option value="">Seçiniz</option>
            <option value="Kısa Vadeli (1 yıldan az)">Kısa Vadeli (1 yıldan az)</option>
            <option value="Orta Vadeli (1-3 yıl)">Orta Vadeli (1-3 yıl)</option>
            <option value="Uzun Vadeli (3+ yıl)">Uzun Vadeli (3+ yıl)</option>
          </select>
          {errors.yatirimSuresi && <div style={errorStyle}>{errors.yatirimSuresi}</div>}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Risk Toleransı *
          </label>
          <select
            name="riskToleransi"
            value={suitabilityProfile.riskToleransi}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.riskToleransi)}
          >
            <option value="">Seçiniz</option>
            <option value="Düşük">Düşük</option>
            <option value="Orta">Orta</option>
            <option value="Yüksek">Yüksek</option>
          </select>
          {errors.riskToleransi && <div style={errorStyle}>{errors.riskToleransi}</div>}
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Mali Durum *
          </label>
          <select
            name="maliDurum"
            value={suitabilityProfile.maliDurum}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.maliDurum)}
          >
            <option value="">Seçiniz</option>
            <option value="Düşük">Düşük</option>
            <option value="Orta">Orta</option>
            <option value="İyi">İyi</option>
            <option value="Çok İyi">Çok İyi</option>
          </select>
          {errors.maliDurum && <div style={errorStyle}>{errors.maliDurum}</div>}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Yatırım Deneyimi *
          </label>
          <select
            name="yatirimDeneyimi"
            value={suitabilityProfile.yatirimDeneyimi}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.yatirimDeneyimi)}
          >
            <option value="">Seçiniz</option>
            <option value="Yok">Yok</option>
            <option value="Az">Az</option>
            <option value="Orta">Orta</option>
            <option value="İyi">İyi</option>
            <option value="Çok İyi">Çok İyi</option>
          </select>
          {errors.yatirimDeneyimi && <div style={errorStyle}>{errors.yatirimDeneyimi}</div>}
        </div>

        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Likidite İhtiyacı *
          </label>
          <select
            name="likiditeIhtiyaci"
            value={suitabilityProfile.likiditeIhtiyaci}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.likiditeIhtiyaci)}
          >
            <option value="">Seçiniz</option>
            <option value="Yüksek">Yüksek</option>
            <option value="Orta">Orta</option>
            <option value="Düşük">Düşük</option>
          </select>
          {errors.likiditeIhtiyaci && <div style={errorStyle}>{errors.likiditeIhtiyaci}</div>}
        </div>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
          Vergi Durumu *
        </label>
        <select
          name="vergiDurumu"
          value={suitabilityProfile.vergiDurumu}
          onChange={handleSuitabilityChange}
          style={inputStyle(errors.vergiDurumu)}
        >
          <option value="">Seçiniz</option>
          <option value="Vergi Mükellefi">Vergi Mükellefi</option>
          <option value="Vergi Mükellefi Değil">Vergi Mükellefi Değil</option>
        </select>
        {errors.vergiDurumu && <div style={errorStyle}>{errors.vergiDurumu}</div>}
      </div>
    </div>
  );

  const renderDocumentsStep = () => (
    <div>
      <h3 style={{ marginBottom: '24px', color: '#2c3e50' }}>Müşteri Belgeleri</h3>

      <div style={{ marginBottom: '24px' }}>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#2c3e50' }}>
          KVKK Metni (PDF)
        </label>
        <input
          type="file"
          name="kvkkBelgesi"
          accept=".pdf"
          onChange={handleFileChange}
          style={inputStyle(false)}
        />
        {documents.kvkkBelgesi && (
          <p style={{ color: '#27ae60', fontSize: '12px', marginTop: '8px' }}>
            ✓ {documents.kvkkBelgesi.name}
          </p>
        )}
      </div>

      <div style={{ marginBottom: '24px' }}>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#2c3e50' }}>
          Portföy Yönetim Sözleşmesi (PDF)
        </label>
        <input
          type="file"
          name="portfoyYonetimSozlesmesi"
          accept=".pdf"
          onChange={handleFileChange}
          style={inputStyle(false)}
        />
        {documents.portfoyYonetimSozlesmesi && (
          <p style={{ color: '#27ae60', fontSize: '12px', marginTop: '8px' }}>
            ✓ {documents.portfoyYonetimSozlesmesi.name}
          </p>
        )}
      </div>

      <div style={{ marginBottom: '24px' }}>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#2c3e50' }}>
          Elektronik Bildirim İzni (PDF)
        </label>
        <input
          type="file"
          name="elektronikBildirimIzni"
          accept=".pdf"
          onChange={handleFileChange}
          style={inputStyle(false)}
        />
        {documents.elektronikBildirimIzni && (
          <p style={{ color: '#27ae60', fontSize: '12px', marginTop: '8px' }}>
            ✓ {documents.elektronikBildirimIzni.name}
          </p>
        )}
      </div>
    </div>
  );

  const renderSummaryStep = () => (
    <div>
      <h3 style={{ marginBottom: '24px', color: '#2c3e50' }}>Özet ve Onay</h3>

      <div style={{ backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '24px' }}>
        <h4 style={{ marginBottom: '16px', color: '#2c3e50' }}>Kişisel Bilgiler</h4>
        {/* DEĞİŞİKLİK: Özet ekranında tam müşteri kodu ve telefon gösteriliyor */}
        <p><strong>Müşteri Kodu:</strong> BRYSL-{personalInfo.musteriKodu}</p>
        <p><strong>Ad Soyad:</strong> {personalInfo.ad} {personalInfo.soyad}</p>
        <p><strong>T.C. Kimlik No:</strong> {personalInfo.tckn}</p>
        <p><strong>Doğum Tarihi:</strong> {personalInfo.dogumTarihi}</p>
        <p><strong>Telefon:</strong> +90 {personalInfo.telefon}</p>
        <p><strong>E-posta:</strong> {personalInfo.email}</p>
        <p><strong>Adres:</strong> {personalInfo.adres}</p>
      </div>

      <div style={{ backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '24px' }}>
        <h4 style={{ marginBottom: '16px', color: '#2c3e50' }}>Yerindelik Profili</h4>
        <p><strong>Yatırım Amacı:</strong> {suitabilityProfile.yatirimAmaci}</p>
        <p><strong>Yatırım Süresi:</strong> {suitabilityProfile.yatirimSuresi}</p>
        <p><strong>Risk Toleransı:</strong> {suitabilityProfile.riskToleransi}</p>
        <p><strong>Mali Durum:</strong> {suitabilityProfile.maliDurum}</p>
        <p><strong>Yatırım Deneyimi:</strong> {suitabilityProfile.yatirimDeneyimi}</p>
        <p><strong>Likidite İhtiyacı:</strong> {suitabilityProfile.likiditeIhtiyaci}</p>
        <p><strong>Vergi Durumu:</strong> {suitabilityProfile.vergiDurumu}</p>
      </div>

      <div style={{ backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '24px' }}>
        <h4 style={{ marginBottom: '16px', color: '#2c3e50' }}>Yüklenen Belgeler</h4>
        <p><strong>KVKK Metni:</strong> {documents.kvkkBelgesi ? documents.kvkkBelgesi.name : 'Yüklenmedi'}</p>
        <p><strong>Portföy Yönetim Sözleşmesi:</strong> {documents.portfoyYonetimSozlesmesi ? documents.portfoyYonetimSozlesmesi.name : 'Yüklenmedi'}</p>
        <p><strong>Elektronik Bildirim İzni:</strong> {documents.elektronikBildirimIzni ? documents.elektronikBildirimIzni.name : 'Yüklenmedi'}</p>
      </div>
    </div>
  );

  return (
    <div style={{ maxWidth: '800px', margin: '40px auto', padding: '20px' }}>
      <h2 style={{ textAlign: 'center', marginBottom: '40px', color: '#2c3e50' }}>
        Bireysel Müşteri Kayıt Formu
      </h2>

      {/* Step Indicator */}
      <div style={stepperStyle}>
        <div style={stepStyle(currentStep === 1, completedSteps.includes(1))} onClick={() => goToStep(1)}>
          <div style={stepCircleStyle(currentStep === 1, completedSteps.includes(1))}>1</div>
          Kişisel Bilgiler
        </div>
        <div style={stepStyle(currentStep === 2, completedSteps.includes(2))} onClick={() => goToStep(2)}>
          <div style={stepCircleStyle(currentStep === 2, completedSteps.includes(2))}>2</div>
          Yerindelik Profili
        </div>
        <div style={stepStyle(currentStep === 3, completedSteps.includes(3))} onClick={() => goToStep(3)}>
          <div style={stepCircleStyle(currentStep === 3, completedSteps.includes(3))}>3</div>
          Belgeler
        </div>
        <div style={stepStyle(currentStep === 4, completedSteps.includes(4))} onClick={() => goToStep(4)}>
          <div style={stepCircleStyle(currentStep === 4, completedSteps.includes(4))}>4</div>
          Özet
        </div>
      </div>

      {/* Step Content */}
      <div style={{ backgroundColor: 'white', padding: '32px', borderRadius: '12px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)' }}>
        {renderStepContent()}
      </div>

      {/* Navigation Buttons */}
      <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '32px' }}>
        <button
          onClick={prevStep}
          disabled={currentStep === 1}
          style={buttonStyle('secondary', currentStep === 1)}
        >
          Geri
        </button>

        {currentStep < 4 ? (
          <button
            onClick={nextStep}
            style={buttonStyle('primary', false)}
          >
            İleri
          </button>
        ) : (
          <button
            onClick={handleSubmit}
            disabled={isLoading}
            style={buttonStyle('primary', isLoading)}
          >
            {isLoading ? 'Kaydediliyor...' : 'Kaydet'}
          </button>
        )}
      </div>
    </div>
  );
}