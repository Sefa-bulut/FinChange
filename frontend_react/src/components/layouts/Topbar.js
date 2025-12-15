import React from 'react';
import { FaUserCircle, FaSignOutAlt } from 'react-icons/fa';
import { useAuth } from '../../context/AuthContext';
import api from '../../services/api';
import { toast } from 'react-toastify';
import { openSettlementOverride, closeSettlementOverride } from '../../services/marketSessionService';

const styles = {
  topbar: {
    height: '60px',
    backgroundColor: '#ffffff',
    borderBottom: '1px solid #e9ecef',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 30px',
    color: '#495057',
  },
  userInfo: {
    display: 'flex',
    alignItems: 'center',
  },
  userIcon: {
    fontSize: '24px',
    marginRight: '10px',
  },
  logoutIcon: {
    fontSize: '24px',
    marginLeft: '30px',
    cursor: 'pointer',
    color: '#c8102e',
  },
  controls: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  pill: {
    padding: '4px 10px',
    borderRadius: '12px',
    fontSize: '12px',
    border: '1px solid #dee2e6',
    color: '#495057',
    backgroundColor: '#f8f9fa',
  },
  button: {
    padding: '6px 10px',
    borderRadius: '6px',
    border: '1px solid #ced4da',
    backgroundColor: '#ffffff',
    cursor: 'pointer',
    fontSize: '12px',
  },
};

export default function Topbar() {
  const { user, logout, permissions } = useAuth();
  const username = user ? user.name : '';

  const [status, setStatus] = React.useState({ open: false, overrideSimulation: false, overrideTrading: false, simulationActive: false, settlementControlsActive: true });

  const canToggle = permissions?.includes('order:create');

  const fetchStatus = React.useCallback(async () => {
    try {
      const res = await api.get('/v1/market-session/status');
      const payload = res?.result || res?.data?.result || res?.data; 
      if (payload) setStatus(payload);
    } catch (e) {
      
    }
  }, []);

  React.useEffect(() => {
    fetchStatus();
    const id = setInterval(fetchStatus, 5000);
    return () => clearInterval(id);
  }, [fetchStatus]);

  const handleLogout = () => { logout(); };

  const toggleSimulation = async () => {
    try {
      const path = status.overrideSimulation ? '/v1/market-session/override/simulation/close' : '/v1/market-session/override/simulation/open';
      await api.post(path);
      toast.success(`Simülasyon ${status.overrideSimulation ? 'kapandı' : 'açıldı'}`);
      fetchStatus();
    } catch (e) {
      toast.error(e?.message || 'Simülasyon toggle başarısız');
    }
  };

  const toggleTrading = async () => {
    try {
      const path = status.overrideTrading ? '/v1/market-session/override/trading/close' : '/v1/market-session/override/trading/open';
      await api.post(path);
      toast.success(`Market ${status.overrideTrading ? 'kapandı' : 'açıldı'}`);
      fetchStatus();
    } catch (e) {
      toast.error(e?.message || 'Market toggle başarısız');
    }
  };

  const toggleSettlement = async () => {
    try {
      if (status.settlementControlsActive) {
        await closeSettlementOverride();
        toast.success('Takas kontrolleri kapatıldı, blokajlar serbest bırakıldı');
      } else {
        await openSettlementOverride();
        toast.success('Takas kontrolleri açıldı');
      }
      fetchStatus();
    } catch (e) {
      toast.error(e?.message || 'Takas toggle başarısız');
    }
  };

  return (
    <div style={styles.topbar}>
      <div style={styles.userInfo}>
        <FaUserCircle style={styles.userIcon} />
        <span>{username}</span>
      </div>

      <div style={styles.controls}>
        <span style={styles.pill} title="Piyasa Açık mı?">{status.open ? 'Açık' : 'Kapalı'}</span>
        {canToggle && (
          <>
            <button style={styles.button} onClick={toggleSimulation} title="Fiyat Simülasyonu Aç/Kapat">
              Simülasyon: {status.simulationActive ? 'Açık' : 'Kapalı'}
            </button>
            <button style={styles.button} onClick={toggleTrading} title="Market Aç/Kapat">
              Market: {status.overrideTrading ? 'Açık' : 'Kapalı'}
            </button>
            <button style={styles.button} onClick={toggleSettlement} title="Takas Kontrolleri Aç/Kapat">
              Takas: {status.settlementControlsActive ? 'Açık' : 'Kapalı'}
            </button>
          </>
        )}
        <FaSignOutAlt 
          style={styles.logoutIcon} 
          onClick={handleLogout} 
          title="Çıkış Yap" 
        />
      </div>
    </div>
  );
}