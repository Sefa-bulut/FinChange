import React from 'react';
import { useAuth } from '../context/AuthContext'; // AuthContext'inizin yolunu kontrol edin

const DashboardPage = () => {
    const { user } = useAuth();

    // Not: Bu veriler başlangıç için sahtedir.
    // Daha sonra API'den çekilecek şekilde güncellenebilir.
    const dashboardData = {
        activeClients: 78,
        openOrders: 15,
        totalPortfolioValue: '₺2,450,110.21',
        pendingApprovals: 4
    };

    return (
        <div>
            {/* Karşılama Başlığı */}
            <h1 style={{ fontSize: '2rem', fontWeight: '600', marginBottom: '2rem' }}>
                Hoş Geldiniz, {user ? user.name : 'Kullanıcı'}!
            </h1>

            {/* İstatistik Kartları */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px' }}>
                {/* Bu alana birazdan oluşturacağımız kartları koyacağız */}
                <div style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                    <h3 style={{ margin: '0 0 5px 0', color: '#666', fontSize: '0.9rem' }}>Aktif Müşteriler</h3>
                    <p style={{ margin: '0', color: '#111', fontSize: '1.8rem', fontWeight: '600' }}>{dashboardData.activeClients}</p>
                </div>
                <div style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                    <h3 style={{ margin: '0 0 5px 0', color: '#666', fontSize: '0.9rem' }}>Açık Emirler</h3>
                    <p style={{ margin: '0', color: '#111', fontSize: '1.8rem', fontWeight: '600' }}>{dashboardData.openOrders}</p>
                </div>
                <div style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                    <h3 style={{ margin: '0 0 5px 0', color: '#666', fontSize: '0.9rem' }}>Toplam Portföy Değeri</h3>
                    <p style={{ margin: '0', color: '#111', fontSize: '1.8rem', fontWeight: '600' }}>{dashboardData.totalPortfolioValue}</p>
                </div>
                <div style={{ background: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
                    <h3 style={{ margin: '0 0 5px 0', color: '#666', fontSize: '0.9rem' }}>Onay Bekleyenler</h3>
                    <p style={{ margin: '0', color: '#111', fontSize: '1.8rem', fontWeight: '600' }}>{dashboardData.pendingApprovals}</p>
                </div>
            </div>
        </div>
    );
};

export default DashboardPage;