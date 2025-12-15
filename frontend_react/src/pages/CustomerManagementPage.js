import React from 'react';
import { useNavigate } from 'react-router-dom';

export default function CustomerManagementPage() {
  const navigate = useNavigate();

  const buttonStyle = {
    backgroundColor: '#a22217',
    color: 'white',
    padding: '18px 32px',
    fontSize: '16px',
    borderRadius: '12px',
    border: 'none',
    cursor: 'pointer',
    margin: '0 20px',
  };

  const containerStyle = {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: '80vh',
    backgroundColor: '#fff',
  };

  return (
    <div style={containerStyle}>
      <h1 style={{ fontSize: '24px', fontWeight: '600', marginBottom: '30px' }}>
        YENİ MÜŞTERİ KAYIT
      </h1>
      <div>
        <button style={buttonStyle} onClick={() => navigate('/dashboard/clients/individual')}>
          GERÇEK KİŞİ KAYIT
        </button>
        <button style={buttonStyle} onClick={() => navigate('/dashboard/clients/corporate')}>
          TÜZEL KİŞİ KAYIT
        </button>
      </div>
    </div>
  );
}