// CustomerDetailPage.js

import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getClientById } from '../services/customerService';


const pageStyle = {
  padding: '2rem',
  fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
  color: '#212529',
  backgroundColor: '#f8f9fa',
  minHeight: '100vh',
};

const cardStyle = {
  background: '#fff',
  borderRadius: '8px',
  padding: '1.5rem',
  marginBottom: '1.5rem',
  boxShadow: '0 2px 10px rgba(0,0,0,0.075)',
};

const headerStyle = {
  borderBottom: '1px solid #dee2e6',
  paddingBottom: '1rem',
  marginBottom: '1.5rem',
};

const gridStyle = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
  gap: '1.5rem',
};

const infoItemStyle = {
  lineHeight: '1.7',
  fontSize: '1rem',
};

// --- Yardımcı Bileşenler ---
const InfoItem = ({ label, value }) => (
  <div style={infoItemStyle}>
    <strong style={{ color: '#495057', minWidth: '150px', display: 'inline-block' }}>{label}:</strong> 
    {value || <span style={{ color: '#adb5bd' }}>-</span>}
  </div>
);

const SectionTitle = ({ children }) => (
  <h2 style={{ fontSize: '1.25rem', color: '#343a40', borderBottom: '2px solid #a22217', paddingBottom: '0.5rem', marginBottom: '1rem' }}>
    {children}
  </h2>
);

// --- Ana Sayfa Bileşeni ---
export default function CustomerDetailPage() {
  const { id } = useParams();
  const [client, setClient] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (id) {
      setLoading(true);
      getClientById(id)
        .then(data => {
          setClient(data);
          setLoading(false);
        })
        .catch(err => {
          console.error("Müşteri detayı alınamadı:", err);
          setError("Müşteri bilgileri yüklenirken bir hata oluştu.");
          setLoading(false);
        });
    }
  }, [id]);

  if (loading) return <div style={pageStyle}><h2>Yükleniyor...</h2></div>;
  if (error) return <div style={{ ...pageStyle, color: 'red' }}><h2>{error}</h2></div>;
  if (!client) return <div style={pageStyle}><h2>Müşteri bulunamadı.</h2></div>;

  const isCorporate = client.customerType === 'TUZEL';
  const profile = client.suitabilityProfile || {};

  const renderIndividualDetails = () => (
    <>
      <InfoItem label="Ad Soyad" value={`${client.ad} ${client.soyad}`} />
      <InfoItem label="T.C. Kimlik No" value={client.tckn} />
      <InfoItem label="Doğum Tarihi" value={client.dogumTarihi} />
    </>
  );

  const renderCorporateDetails = () => (
    <>
      <InfoItem label="Şirket Ünvanı" value={client.sirketUnvani} />
      <InfoItem label="Vergi Kimlik No" value={client.vergiNo} />
      <InfoItem label="Mersis No" value={client.mersisNo} />
      <InfoItem label="Yetkili Kişi" value={client.yetkiliKisiAdSoyad} />
    </>
  );

  const renderIndividualProfile = () => (
    <>
      <InfoItem label="Yatırım Amacı" value={profile.yatirimAmaci} />
      <InfoItem label="Yatırım Süresi" value={profile.yatirimSuresi} />
      <InfoItem label="Risk Toleransı" value={profile.riskToleransi} />
      <InfoItem label="Mali Durum" value={profile.maliDurum} />
      <InfoItem label="Yatırım Deneyimi" value={profile.yatirimDeneyimi} />
      <InfoItem label="Likidite İhtiyacı" value={profile.likiditeIhtiyaci} />
      <InfoItem label="Vergi Durumu" value={profile.vergiDurumu} />
    </>
  );

  const renderCorporateProfile = () => (
    <>
      <InfoItem label="Şirket Yatırım Stratejisi" value={profile.sirketYatirimStratejisi} />
      <InfoItem label="Risk Yönetimi Politikası" value={profile.riskYonetimiPolitikasi} />
      <InfoItem label="Finansal Durum" value={profile.finansalDurumTuzel} />
      <InfoItem label="Yatırım Vadesi" value={profile.yatirimSuresiVadeTuzel} />
      {/* Ortak sorular da gösterilebilir */}
      <InfoItem label="Yatırım Amacı" value={profile.yatirimAmaci} />
      <InfoItem label="Risk Toleransı" value={profile.riskToleransi} />
    </>
  );

  return (
    <div style={pageStyle}>
      <div style={{ ...headerStyle, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>{client.gorunenAd}</h1>
          <p style={{ color: '#6c757d', marginTop: 0 }}>
            Müşteri Kodu: <strong>{client.musteriKodu}</strong> | Durum: <strong>{client.durum}</strong>
          </p>
        </div>
        {/* --- YENİ DÜZENLE BUTONU --- */}
        <Link to={`/dashboard/clients/${id}/edit`} style={{
          textDecoration: 'none',
          backgroundColor: '#a22217',
          color: 'white',
          padding: '10px 20px',
          borderRadius: '8px',
          fontWeight: '600',
          transition: 'background-color 0.2s'
        }}>
          Düzenle
        </Link>
        {/* ------------------------- */}
      </div>

      <div style={cardStyle}>
        <SectionTitle>{isCorporate ? 'Şirket Detayları' : 'Kişisel Detaylar'}</SectionTitle>
        <div style={gridStyle}>
          {isCorporate ? renderCorporateDetails() : renderIndividualDetails()}
        </div>
      </div>

      <div style={cardStyle}>
        <SectionTitle>İletişim Bilgileri</SectionTitle>
        <div style={gridStyle}>
          <InfoItem label="Telefon Numarası" value={client.telefon} />
          <InfoItem label="E-posta Adresi" value={client.email} />
          <InfoItem label={isCorporate ? "Merkez Adresi" : "İkametgah Adresi"} value={client.adres} />
        </div>
      </div>

      <div style={cardStyle}>
        <SectionTitle>Yerindelik Profili</SectionTitle>
        <div style={gridStyle}>
          {isCorporate ? renderCorporateProfile() : renderIndividualProfile()}
        </div>
      </div>
    </div>
  );
}