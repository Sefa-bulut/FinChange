import React, { useState } from 'react';
import { createClient } from '../services/customerService';

// A custom AlertModal component to avoid using window.alert()
const AlertModal = ({ message, onClose }) => (
  <div style={{
    position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    zIndex: 1000
  }}>
    <div style={{
      backgroundColor: '#fff', padding: '20px', borderRadius: '10px',
      maxWidth: '400px', width: '90%', textAlign: 'center'
    }}>
      <p style={{ margin: '0 0 20px 0', fontSize: '16px' }}>{message}</p>
      <button onClick={onClose} style={{
        backgroundColor: '#a22217', color: '#fff', padding: '10px 20px',
        border: 'none', borderRadius: '8px', cursor: 'pointer'
      }}>
        Tamam
      </button>
    </div>
  </div>
);

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

export default function CorporateClientForm() {
  const [currentStep, setCurrentStep] = useState(1);
  const [completedSteps, setCompletedSteps] = useState([]);
  
  // DEĞİŞİKLİK: musteriKodu state'i artık sadece kullanıcı tarafından girilen kısmı tutacak.
  const [companyInfo, setCompanyInfo] = useState({
    sirketUnvani: '',
    vergiKimlikNo: '',
    adres: '',
    mersisNo: '',
    musteriKodu: '', // Sadece 'TUZEL-' sonrası girilen kısmı tutar
  });

  // DEĞİŞİKLİK: telefon state'i artık sadece 10 haneli numarayı tutacak.
  const [authorizedPerson, setAuthorizedPerson] = useState({
    yetkiliKisiAdSoyad: '',
    telefon: '', // Sadece '5xxxxxxxxx' kısmını tutar
    email: '',
  });

  const [suitabilityProfile, setSuitabilityProfile] = useState({
    sirketYatirimStratejisi: '',
    riskYonetimiPolitikasi: '',
    finansalDurumTuzel: '',
    yatirimSuresiVadeTuzel: '',
    yatirimAmaci: '',
    riskToleransi: '',
    likiditeIhtiyaci: '',
  });

  const [documents, setDocuments] = useState({
    vergiLevhasi: null,
    kvkkBelgesi: null,
    portfoyYonetimSozlesmesi: null,
    elektronikBildirimIzni: null,
  });

  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [modalMessage, setModalMessage] = useState(null);

  // Validation functions for each step
  const validateCompanyInfo = () => {
    const newErrors = {};
    
    if (!companyInfo.sirketUnvani.trim()) {
      newErrors.sirketUnvani = 'Şirket ünvanı zorunludur.';
    }
    
    if (!companyInfo.vergiKimlikNo) {
      newErrors.vergiKimlikNo = 'Vergi Kimlik No zorunludur.';
    } else if (!/^\d{10}$/.test(companyInfo.vergiKimlikNo)) {
      newErrors.vergiKimlikNo = 'Vergi Kimlik No sadece 10 rakamdan oluşmalıdır.';
    }
    
    if (!companyInfo.adres.trim()) {
      newErrors.adres = 'Adres zorunludur.';
    }

    // DEĞİŞİKLİK: Müşteri kodu validasyonu güncellendi. Artık sadece son eki kontrol ediyoruz.
    if (!companyInfo.musteriKodu.trim()) {
      newErrors.musteriKodu = 'Müşteri kodu zorunludur.';
    } else if (!/^[A-Za-z0-9]+$/.test(companyInfo.musteriKodu)) {
      newErrors.musteriKodu = 'Müşteri kodu sadece harf ve rakam içerebilir.';
    }
    
    if (companyInfo.mersisNo && !/^\d{16}$/.test(companyInfo.mersisNo)) {
      newErrors.mersisNo = 'Mersis No 16 haneli rakamlardan oluşmalıdır.';
    }

    return newErrors;
  };

  const validateAuthorizedPerson = () => {
    const newErrors = {};
    
    if (!authorizedPerson.yetkiliKisiAdSoyad.trim()) {
      newErrors.yetkiliKisiAdSoyad = 'Yetkili kişi adı soyadı zorunludur.';
    } else if (!/^[a-zA-ZğüşıöçĞÜŞİÖÇ\s]+$/.test(authorizedPerson.yetkiliKisiAdSoyad.trim())) {
      newErrors.yetkiliKisiAdSoyad = 'Ad Soyad sadece harf içermelidir.';
    }
    
    if (!authorizedPerson.telefon) {
      newErrors.telefon = 'Telefon zorunludur.';
    } else if (!/^\d{10}$/.test(authorizedPerson.telefon)) {
      newErrors.telefon = 'Telefon 10 haneli rakam olmalıdır.';
    } else if (!authorizedPerson.telefon.startsWith('5')) {
      newErrors.telefon = 'Telefon 5 ile başlamalıdır.';
    } else if (/[a-zA-Z\-\s]/.test(authorizedPerson.telefon)) {
      newErrors.telefon = 'Telefon sadece rakam içermelidir.';
    }
    
    if (!authorizedPerson.email) {
      newErrors.email = 'E-posta zorunludur.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(authorizedPerson.email)) {
      newErrors.email = 'Geçersiz e-posta adresi.';
    }

    return newErrors;
  };

  const validateSuitabilityProfile = () => {
    const newErrors = {};
    
    if (!suitabilityProfile.sirketYatirimStratejisi) newErrors.sirketYatirimStratejisi = 'Şirket yatırım stratejisi seçiniz.';
    if (!suitabilityProfile.riskYonetimiPolitikasi) newErrors.riskYonetimiPolitikasi = 'Risk yönetimi politikası seçiniz.';
    if (!suitabilityProfile.finansalDurumTuzel) newErrors.finansalDurumTuzel = 'Finansal durum seçiniz.';
    if (!suitabilityProfile.yatirimSuresiVadeTuzel) newErrors.yatirimSuresiVadeTuzel = 'Yatırım süresi seçiniz.';
    if (!suitabilityProfile.yatirimAmaci) newErrors.yatirimAmaci = 'Yatırım amacı seçiniz.';
    if (!suitabilityProfile.riskToleransi) newErrors.riskToleransi = 'Risk toleransı seçiniz.';
    if (!suitabilityProfile.likiditeIhtiyaci) newErrors.likiditeIhtiyaci = 'Likidite ihtiyacı seçiniz.';

    return newErrors;
  };

  const handleCompanyInfoChange = (e) => {
    const { name, value } = e.target;
    // DEĞİŞİKLİK: Müşteri kodu için sadece alfanümerik karakterlere izin ver
    if (name === 'musteriKodu') {
        const sanitizedValue = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
        setCompanyInfo((prev) => ({ ...prev, [name]: sanitizedValue }));
    } else {
        setCompanyInfo((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleAuthorizedPersonChange = (e) => {
    const { name, value } = e.target;
    setAuthorizedPerson((prev) => ({ ...prev, [name]: value }));
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
      validationErrors = validateCompanyInfo();
      if (Object.keys(validationErrors).length === 0) {
        setCompletedSteps(prev => [...prev.filter(s => s !== 1), 1]);
        setCurrentStep(2);
      }
    } else if (currentStep === 2) {
      validationErrors = validateAuthorizedPerson();
      if (Object.keys(validationErrors).length === 0) {
        setCompletedSteps(prev => [...prev.filter(s => s !== 2), 2]);
        setCurrentStep(3);
      }
    } else if (currentStep === 3) {
      validationErrors = validateSuitabilityProfile();
      if (Object.keys(validationErrors).length === 0) {
        setCompletedSteps(prev => [...prev.filter(s => s !== 3), 3]);
        setCurrentStep(4);
      }
    } else if (currentStep === 4) {
      setCompletedSteps(prev => [...prev.filter(s => s !== 4), 4]);
      setCurrentStep(5);
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
      musteriKodu: `TUZEL-${companyInfo.musteriKodu}`,
      customerType: 'TUZEL',
      telefon: `+90${authorizedPerson.telefon}`,
      email: authorizedPerson.email,
      adres: companyInfo.adres,
      sirketUnvani: companyInfo.sirketUnvani,
      vergiKimlikNo: companyInfo.vergiKimlikNo,
      mersisNo: companyInfo.mersisNo || null,
      yetkiliKisiAdSoyad: authorizedPerson.yetkiliKisiAdSoyad,
      suitabilityProfile: suitabilityProfile,
    };

    try {
      const resp = await createClient(clientData, documents);
      setModalMessage('Kurumsal müşteri başarıyla kaydedildi.');
      console.log('API Yanıtı:', resp);
      
      // Müşteri listesi sayfasına yönlendir
      setTimeout(() => {
        window.location.href = '/dashboard/clients';
      }, 2000); // 2 saniye sonra yönlendir (modal mesajını görmek için)
      return;
      
      // Reset form (artık çalışmayacak çünkü redirect oldu)
      setCompanyInfo({
        sirketUnvani: '',
        vergiKimlikNo: '',
        adres: '',
        mersisNo: '',
        musteriKodu: '',
      });
      setAuthorizedPerson({
        yetkiliKisiAdSoyad: '',
        telefon: '',
        email: '',
      });
      setSuitabilityProfile({
        sirketYatirimStratejisi: '',
        riskYonetimiPolitikasi: '',
        finansalDurumTuzel: '',
        yatirimSuresiVadeTuzel: '',
        yatirimAmaci: '',
        riskToleransi: '',
        likiditeIhtiyaci: '',
      });
      setDocuments({
        vergiLevhasi: null,
        kvkkBelgesi: null,
        portfoyYonetimSozlesmesi: null,
        elektronikBildirimIzni: null,
      });
      setCurrentStep(1);
      setCompletedSteps([]);
      setErrors({});
    } catch (err) {
      console.error('Kayıt işlemi başarısız. Hata Detayları:', err.response?.data || err.message);
      const errorDetail = err.response?.data?.message || err.response?.data || err.message;
      setModalMessage(`Kayıt hatası: ${typeof errorDetail === 'string' ? errorDetail : JSON.stringify(errorDetail, null, 2)}`);
    } finally {
      setIsLoading(false);
    }
  };

  // Render step content
  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return renderCompanyInfoStep();
      case 2:
        return renderAuthorizedPersonStep();
      case 3:
        return renderSuitabilityProfileStep();
      case 4:
        return renderDocumentsStep();
      case 5:
        return renderSummaryStep();
      default:
        return null;
    }
  };

  const renderCompanyInfoStep = () => (
    <div>
      <h3 style={{ marginBottom: '24px', color: '#2c3e50' }}>Şirket Bilgileri</h3>
      
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
              TUZEL-
            </span>
            <input
              type="text"
              name="musteriKodu"
              value={companyInfo.musteriKodu}
              onChange={handleCompanyInfoChange}
              placeholder="Örn: ABC123"
              style={{
                ...inputStyle(errors.musteriKodu),
                borderRadius: '0 8px 8px 0',
                marginTop: 0, // span ile aynı hizada olması için
              }}
            />
        </div>
        {errors.musteriKodu && <div style={errorStyle}>{errors.musteriKodu}</div>}
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
          Şirket Ünvanı *
        </label>
        <input
          type="text"
          name="sirketUnvani"
          value={companyInfo.sirketUnvani}
          onChange={handleCompanyInfoChange}
          style={inputStyle(errors.sirketUnvani)}
        />
        {errors.sirketUnvani && <div style={errorStyle}>{errors.sirketUnvani}</div>}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Vergi Kimlik No *
          </label>
          <input
            type="text"
            name="vergiKimlikNo"
            value={companyInfo.vergiKimlikNo}
            onChange={(e) => {
              // Sadece rakam girişine izin ver
              const value = e.target.value.replace(/[^0-9]/g, '');
              handleCompanyInfoChange({ target: { name: 'vergiKimlikNo', value } });
            }}
            maxLength={10}
            placeholder="10 haneli Vergi Kimlik No"
            style={inputStyle(errors.vergiKimlikNo)}
          />
          {errors.vergiKimlikNo && <div style={errorStyle}>{errors.vergiKimlikNo}</div>}
        </div>
        
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Mersis No (Opsiyonel)
          </label>
          <input
            type="text"
            name="mersisNo"
            value={companyInfo.mersisNo}
            onChange={(e) => {
              // Sadece rakam girişine izin ver
              const value = e.target.value.replace(/[^0-9]/g, '');
              handleCompanyInfoChange({ target: { name: 'mersisNo', value } });
            }}
            maxLength={16}
            placeholder="16 haneli Mersis No (opsiyonel)"
            style={inputStyle(errors.mersisNo)}
          />
          {errors.mersisNo && <div style={errorStyle}>{errors.mersisNo}</div>}
        </div>
      </div>

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
          Merkez Adres *
        </label>
        <textarea
          name="adres"
          value={companyInfo.adres}
          onChange={handleCompanyInfoChange}
          style={inputStyle(errors.adres)}
          rows={3}
        />
        {errors.adres && <div style={errorStyle}>{errors.adres}</div>}
      </div>
    </div>
  );

  const renderAuthorizedPersonStep = () => (
    <div>
      <h3 style={{ marginBottom: '24px', color: '#2c3e50' }}>Yetkili Kişi Bilgileri</h3>
      
      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
          Yetkili Kişi Ad Soyad *
        </label>
        <input
          type="text"
          name="yetkiliKisiAdSoyad"
          value={authorizedPerson.yetkiliKisiAdSoyad}
          onChange={handleAuthorizedPersonChange}
          style={inputStyle(errors.yetkiliKisiAdSoyad)}
        />
        {errors.yetkiliKisiAdSoyad && <div style={errorStyle}>{errors.yetkiliKisiAdSoyad}</div>}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        {/* DEĞİŞİKLİK: Telefon input'u zaten doğru yapıdaydı, sadece state'in nasıl çalıştığını teyit ediyoruz. */}
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
              value={authorizedPerson.telefon}
              onChange={(e) => {
                const value = e.target.value.replace(/[^0-9]/g, '');
                handleAuthorizedPersonChange({ target: { name: 'telefon', value } });
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
            value={authorizedPerson.email}
            onChange={handleAuthorizedPersonChange}
            style={inputStyle(errors.email)}
          />
          {errors.email && <div style={errorStyle}>{errors.email}</div>}
        </div>
      </div>
    </div>
  );

  const renderSuitabilityProfileStep = () => (
    <div>
      <h3 style={{ marginBottom: '24px', color: '#2c3e50' }}>Yerindelik Profili</h3>
      
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Şirket Yatırım Stratejisi *
          </label>
          <select
            name="sirketYatirimStratejisi"
            value={suitabilityProfile.sirketYatirimStratejisi}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.sirketYatirimStratejisi)}
          >
            <option value="">Seçiniz</option>
            <option value="Muhafazakar">Muhafazakar</option>
            <option value="Dengeli">Dengeli</option>
            <option value="Agresif">Agresif</option>
            <option value="Spekülatif">Spekülatif</option>
          </select>
          {errors.sirketYatirimStratejisi && <div style={errorStyle}>{errors.sirketYatirimStratejisi}</div>}
        </div>
        
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Risk Yönetimi Politikası *
          </label>
          <select
            name="riskYonetimiPolitikasi"
            value={suitabilityProfile.riskYonetimiPolitikasi}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.riskYonetimiPolitikasi)}
          >
            <option value="">Seçiniz</option>
            <option value="Sıkı Risk Kontrolü">Sıkı Risk Kontrolü</option>
            <option value="Orta Düzey Risk Kontrolü">Orta Düzey Risk Kontrolü</option>
            <option value="Esnek Risk Kontrolü">Esnek Risk Kontrolü</option>
          </select>
          {errors.riskYonetimiPolitikasi && <div style={errorStyle}>{errors.riskYonetimiPolitikasi}</div>}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Finansal Durum *
          </label>
          <select
            name="finansalDurumTuzel"
            value={suitabilityProfile.finansalDurumTuzel}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.finansalDurumTuzel)}
          >
            <option value="">Seçiniz</option>
            <option value="Zayıf">Zayıf</option>
            <option value="Orta">Orta</option>
            <option value="İyi">İyi</option>
            <option value="Çok İyi">Çok İyi</option>
          </select>
          {errors.finansalDurumTuzel && <div style={errorStyle}>{errors.finansalDurumTuzel}</div>}
        </div>
        
        <div>
          <label style={{ display: 'block', marginBottom: '4px', fontWeight: '600', color: '#2c3e50' }}>
            Yatırım Süresi *
          </label>
          <select
            name="yatirimSuresiVadeTuzel"
            value={suitabilityProfile.yatirimSuresiVadeTuzel}
            onChange={handleSuitabilityChange}
            style={inputStyle(errors.yatirimSuresiVadeTuzel)}
          >
            <option value="">Seçiniz</option>
            <option value="Kısa Vadeli (1 yıldan az)">Kısa Vadeli (1 yıldan az)</option>
            <option value="Orta Vadeli (1-3 yıl)">Orta Vadeli (1-3 yıl)</option>
            <option value="Uzun Vadeli (3+ yıl)">Uzun Vadeli (3+ yıl)</option>
          </select>
          {errors.yatirimSuresiVadeTuzel && <div style={errorStyle}>{errors.yatirimSuresiVadeTuzel}</div>}
        </div>
      </div>

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
      </div>

      <div style={{ marginBottom: '20px' }}>
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
  );

  const renderDocumentsStep = () => (
    <div>
      <h3 style={{ marginBottom: '24px', color: '#2c3e50' }}>Müşteri Belgeleri</h3>
      
      <div style={{ marginBottom: '24px' }}>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', color: '#2c3e50' }}>
          Vergi Levhası (PDF) *
        </label>
        <input
          type="file"
          name="vergiLevhasi"
          accept=".pdf"
          onChange={handleFileChange}
          style={inputStyle(false)}
        />
        {documents.vergiLevhasi && (
          <p style={{ color: '#27ae60', fontSize: '12px', marginTop: '8px' }}>
            ✓ {documents.vergiLevhasi.name}
          </p>
        )}
      </div>

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
        <h4 style={{ marginBottom: '16px', color: '#2c3e50' }}>Şirket Bilgileri</h4>
        {/* DEĞİŞİKLİK: Özet ekranında tam müşteri kodu gösteriliyor */}
        <p><strong>Müşteri Kodu:</strong> TUZEL-{companyInfo.musteriKodu}</p>
        <p><strong>Şirket Ünvanı:</strong> {companyInfo.sirketUnvani}</p>
        <p><strong>Vergi Kimlik No:</strong> {companyInfo.vergiKimlikNo}</p>
        <p><strong>Mersis No:</strong> {companyInfo.mersisNo || 'Belirtilmedi'}</p>
        <p><strong>Adres:</strong> {companyInfo.adres}</p>
      </div>

      <div style={{ backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '24px' }}>
        <h4 style={{ marginBottom: '16px', color: '#2c3e50' }}>Yetkili Kişi Bilgileri</h4>
        <p><strong>Ad Soyad:</strong> {authorizedPerson.yetkiliKisiAdSoyad}</p>
        {/* DEĞİŞİKLİK: Özet ekranında tam telefon numarası gösteriliyor */}
        <p><strong>Telefon:</strong> +90 {authorizedPerson.telefon}</p>
        <p><strong>E-posta:</strong> {authorizedPerson.email}</p>
      </div>

      <div style={{ backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '24px' }}>
        <h4 style={{ marginBottom: '16px', color: '#2c3e50' }}>Yerindelik Profili</h4>
        <p><strong>Şirket Yatırım Stratejisi:</strong> {suitabilityProfile.sirketYatirimStratejisi}</p>
        <p><strong>Risk Yönetimi Politikası:</strong> {suitabilityProfile.riskYonetimiPolitikasi}</p>
        <p><strong>Finansal Durum:</strong> {suitabilityProfile.finansalDurumTuzel}</p>
        <p><strong>Yatırım Süresi:</strong> {suitabilityProfile.yatirimSuresiVadeTuzel}</p>
        <p><strong>Yatırım Amacı:</strong> {suitabilityProfile.yatirimAmaci}</p>
        <p><strong>Risk Toleransı:</strong> {suitabilityProfile.riskToleransi}</p>
        <p><strong>Likidite İhtiyacı:</strong> {suitabilityProfile.likiditeIhtiyaci}</p>
      </div>

      <div style={{ backgroundColor: '#f8f9fa', padding: '20px', borderRadius: '8px', marginBottom: '24px' }}>
        <h4 style={{ marginBottom: '16px', color: '#2c3e50' }}>Yüklenen Belgeler</h4>
        <p><strong>Vergi Levhası:</strong> {documents.vergiLevhasi ? documents.vergiLevhasi.name : 'Yüklenmedi'}</p>
        <p><strong>KVKK Metni:</strong> {documents.kvkkBelgesi ? documents.kvkkBelgesi.name : 'Yüklenmedi'}</p>
        <p><strong>Portföy Yönetim Sözleşmesi:</strong> {documents.portfoyYonetimSozlesmesi ? documents.portfoyYonetimSozlesmesi.name : 'Yüklenmedi'}</p>
        <p><strong>Elektronik Bildirim İzni:</strong> {documents.elektronikBildirimIzni ? documents.elektronikBildirimIzni.name : 'Yüklenmedi'}</p>
      </div>
    </div>
  );

  return (
    <>
      {modalMessage && <AlertModal message={modalMessage} onClose={() => setModalMessage(null)} />}
      <div style={{ maxWidth: '800px', margin: '40px auto', padding: '20px' }}>
        <h2 style={{ textAlign: 'center', marginBottom: '40px', color: '#2c3e50' }}>
          Kurumsal Müşteri Kayıt Formu
        </h2>
        
        {/* Step Indicator */}
        <div style={stepperStyle}>
          <div style={stepStyle(currentStep === 1, completedSteps.includes(1))} onClick={() => goToStep(1)}>
            <div style={stepCircleStyle(currentStep === 1, completedSteps.includes(1))}>1</div>
            Şirket Bilgileri
          </div>
          <div style={stepStyle(currentStep === 2, completedSteps.includes(2))} onClick={() => goToStep(2)}>
            <div style={stepCircleStyle(currentStep === 2, completedSteps.includes(2))}>2</div>
            Yetkili Kişi
          </div>
          <div style={stepStyle(currentStep === 3, completedSteps.includes(3))} onClick={() => goToStep(3)}>
            <div style={stepCircleStyle(currentStep === 3, completedSteps.includes(3))}>3</div>
            Yerindelik Profili
          </div>
          <div style={stepStyle(currentStep === 4, completedSteps.includes(4))} onClick={() => goToStep(4)}>
            <div style={stepCircleStyle(currentStep === 4, completedSteps.includes(4))}>4</div>
            Belgeler
          </div>
          <div style={stepStyle(currentStep === 5, completedSteps.includes(5))} onClick={() => goToStep(5)}>
            <div style={stepCircleStyle(currentStep === 5, completedSteps.includes(5))}>5</div>
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
          
          {currentStep < 5 ? (
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
    </>
  );
}