import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import marketDataService from '../services/marketDataService';

// localStorage için bir anahtar (key) tanımlayalım.
const WATCHLIST_STORAGE_KEY = 'finchangeUserWatchlist';

// Stil nesneleri
const styles = {
  container: {
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    padding: '24px',
    maxWidth: '1200px',
    margin: '40px auto',
    backgroundColor: '#f9fafb',
    borderRadius: '8px',
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
  },
  header: {
    marginBottom: '20px',
    borderBottom: '1px solid #e5e7eb',
    paddingBottom: '16px'
  },
  formContainer: {
    display: 'flex',
    gap: '12px',
    marginBottom: '24px',
    alignItems: 'stretch'
  },
  input: {
    flexGrow: 1,
    padding: '10px 12px',
    fontSize: '1rem',
    border: '1px solid #d1d5db',
    borderRadius: '6px'
  },
  button: {
    padding: '10px 20px',
    fontSize: '1rem',
    color: 'white',
    backgroundColor: '#2563eb',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontWeight: '500'
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
    fontSize: '14px'
  },
  th: {
    padding: '12px 16px',
    textAlign: 'left',
    backgroundColor: '#f3f4f6',
    borderBottom: '2px solid #ddd',
    fontWeight: '600'
  },
  td: {
    padding: '12px 16px',
    borderBottom: '1px solid #eee'
  },
  arrowUp: { color: '#16a34a', fontWeight: 'bold' },
  arrowDown: { color: '#dc2626', fontWeight: 'bold' },
  arrowSame: { color: '#6b7280' }
};

const getArrow = (direction) => {
  if (direction === 'up') return <span style={styles.arrowUp}>▲</span>;
  if (direction === 'down') return <span style={styles.arrowDown}>▼</span>;
  return <span style={styles.arrowSame}>–</span>;
};


const MarketDataPage = () => {
  const clientRef = useRef(null);
  const [trackedAssets, setTrackedAssets] = useState({});
  const [inputAsset, setInputAsset] = useState('');
  const [subscriptions, setSubscriptions] = useState({});
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isInitialLoading, setIsInitialLoading] = useState(true); // Sayfa ilk yüklenirken

  const updateMarketData = useCallback((priceUpdate) => {
    setTrackedAssets(prevData => {
      const { assetCode, price: newPriceStr } = priceUpdate;
      const oldData = prevData[assetCode];
      if (!oldData) return prevData;

      const newPrice = Number(newPriceStr);
      const oldPrice = Number(oldData.price);
      
      let direction = 'same';
      if (newPrice > oldPrice) direction = 'up';
      else if (newPrice < oldPrice) direction = 'down';

      const className = direction === 'up' ? 'price-up' : (direction === 'down' ? 'price-down' : 'price-cell');
      const updatedAsset = { ...oldData, price: newPrice, direction, className };

      setTimeout(() => {
        setTrackedAssets(currentData => {
          if (currentData[assetCode] && currentData[assetCode].className !== 'price-cell') {
            return { ...currentData, [assetCode]: { ...currentData[assetCode], className: 'price-cell' } };
          }
          return currentData;
        });
      }, 500);

      return { ...prevData, [assetCode]: updatedAsset };
    });
  }, []);

  const trackAsset = useCallback(async (assetCode) => {
    const code = assetCode.trim().toUpperCase();
    if (!code || !clientRef.current?.connected) {
        throw new Error("Bağlantı aktif değil veya hisse kodu geçersiz.");
    }

    const initialData = await marketDataService.getAssetDetails(code);
    if (!initialData || !initialData.bistCode) {
      throw new Error(`'${code}' için veri bulunamadı.`);
    }
    
    const subscription = clientRef.current.subscribe(`/topic/prices/${code}`, (message) => {
      updateMarketData(JSON.parse(message.body));
    });

    setSubscriptions(prev => ({ ...prev, [code]: subscription }));
    setTrackedAssets(prev => ({ 
      ...prev, 
      [code]: { ...initialData, price: initialData.livePrice || initialData.openPrice, direction: 'same', className: 'price-cell' } 
    }));

    const savedWatchlist = JSON.parse(localStorage.getItem(WATCHLIST_STORAGE_KEY) || '[]');
    if (!savedWatchlist.includes(code)) {
      savedWatchlist.push(code);
      localStorage.setItem(WATCHLIST_STORAGE_KEY, JSON.stringify(savedWatchlist));
    }
  }, [updateMarketData]);

  useEffect(() => {
    if (clientRef.current) return;

    const token = localStorage.getItem('accessToken');
    if (!token) {
        setError("Canlı veri alabilmek için lütfen giriş yapın.");
        setIsInitialLoading(false);
        return; 
    }
    
    const stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      debug: (str) => console.log(new Date(), str),
    });
    
    stompClient.onConnect = async () => {
        console.log('WebSocket Bağlantısı Başarılı! Kayıtlı liste yükleniyor...');
        setError(null);
        
        const savedWatchlist = JSON.parse(localStorage.getItem(WATCHLIST_STORAGE_KEY) || '[]');
        if (savedWatchlist.length > 0) {
            // Promise.allSettled, hisselerden biri yüklenemese bile diğerlerinin yüklenmesine devam eder.
            const results = await Promise.allSettled(savedWatchlist.map(code => trackAsset(code)));
            results.forEach((result, index) => {
                if (result.status === 'rejected') {
                    console.error(`Kaydedilmiş hisse '${savedWatchlist[index]}' yüklenemedi:`, result.reason);
                }
            });
        }
        setIsInitialLoading(false);
    };

    stompClient.onStompError = (frame) => {
        console.error('STOMP Broker Hatası:', frame.headers['message']);
        setError(`WebSocket bağlantı hatası: ${frame.headers['message']}`);
        setIsInitialLoading(false);
    };
    
    stompClient.activate();
    clientRef.current = stompClient;

    return () => {
      if (clientRef.current && clientRef.current.active) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
  }, [trackAsset]); // trackAsset'i dependency olarak ekliyoruz.

  const handleTrackAssetClick = async () => {
    const code = inputAsset.trim().toUpperCase();
    if (!code) return;
    if (trackedAssets[code]) {
        alert(`'${code}' zaten izleme listenizde.`);
        return;
    }

    setIsLoading(true);
    try {
        await trackAsset(code);
        setInputAsset('');
    } catch(err) {
        alert(`Hata: ${err.message}`);
    } finally {
        setIsLoading(false);
    }
  };

  const handleUntrackAsset = (assetCode) => {
    const subscription = subscriptions[assetCode];
    if (subscription) {
      subscription.unsubscribe();
      
      const { [assetCode]: _, ...newSubs } = subscriptions;
      setSubscriptions(newSubs);
      
      const { [assetCode]: __, ...newAssets } = trackedAssets;
      setTrackedAssets(newAssets);

      const savedWatchlist = JSON.parse(localStorage.getItem(WATCHLIST_STORAGE_KEY) || '[]');
      const updatedWatchlist = savedWatchlist.filter(code => code !== assetCode);
      localStorage.setItem(WATCHLIST_STORAGE_KEY, JSON.stringify(updatedWatchlist));
    }
  };

  if (isInitialLoading) {
    return <div style={styles.container}><h1>İzleme Listeniz Yükleniyor...</h1></div>;
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1>Kişisel Piyasa İzleme Ekranı</h1>
      </div>
      {error && <p style={{color: '#dc2626', fontWeight: 'bold', padding: '10px', backgroundColor: '#fee2e2', borderRadius: '6px'}}>{error}</p>}
      <p style={{color: '#4b5563', marginTop: '-10px'}}>İzlemek istediğiniz hisse senedinin BIST kodunu girip "İzle" butonuna tıklayın.</p>
      
      <div style={styles.formContainer}>
        <input
          type="text"
          value={inputAsset}
          onChange={(e) => setInputAsset(e.target.value)}
          placeholder="Hisse Kodu"
          style={styles.input}
          onKeyPress={(e) => e.key === 'Enter' && handleTrackAssetClick()}
          disabled={isLoading}
        />
        <button onClick={handleTrackAssetClick} style={styles.button} disabled={isLoading}>
          {isLoading ? 'Ekleniyor...' : 'İzle'}
        </button>
      </div>

      <h2>İzlenen Hisseler</h2>
      {Object.keys(trackedAssets).length === 0 ? (
        <div style={{ padding: '20px', color: '#6b7280', border: '2px dashed #e5e7eb', borderRadius: '8px', textAlign: 'center' }}>
          Henüz izlenen bir hisse bulunmuyor.
        </div>
      ) : (
        <table style={styles.table}>
          <thead>
            <tr>
              <th style={styles.th}>Hisse</th>
              <th style={{...styles.th, textAlign: 'right'}}>Anlık Fiyat</th>
              <th style={{...styles.th, textAlign: 'center', width: '60px'}}>Yön</th>
              <th style={{...styles.th, textAlign: 'right'}}>Açılış</th>
              <th style={{...styles.th, textAlign: 'right'}}>Yüksek</th>
              <th style={{...styles.th, textAlign: 'right'}}>Düşük</th>
              <th style={{...styles.th, textAlign: 'center', width: '100px'}}>İşlem</th>
            </tr>
          </thead>
          <tbody>
            {Object.values(trackedAssets).map(asset => (
              <tr key={asset.bistCode} className={asset.className}>
                <td style={{...styles.td, fontWeight: 'bold'}}>{asset.companyName} ({asset.bistCode})</td>
                <td style={{...styles.td, textAlign: 'right', fontWeight: 'bold', fontSize: '1.1em'}}>{Number(asset.price).toFixed(2)} {asset.currency}</td>
                <td style={{...styles.td, textAlign: 'center'}}>{getArrow(asset.direction)}</td>
                <td style={{...styles.td, textAlign: 'right', color: '#4b5563'}}>{Number(asset.openPrice).toFixed(2)}</td>
                <td style={{...styles.td, textAlign: 'right', color: '#4b5563'}}>{Number(asset.dailyHigh).toFixed(2)}</td>
                <td style={{...styles.td, textAlign: 'right', color: '#4b5563'}}>{Number(asset.dailyLow).toFixed(2)}</td>
                <td style={{...styles.td, textAlign: 'center'}}>
                  <button 
                    onClick={() => handleUntrackAsset(asset.bistCode)} 
                    style={{ background: '#ef4444', color: 'white', border: 'none', padding: '5px 12px', cursor: 'pointer', borderRadius: '4px', fontWeight: '500' }}>
                      Bırak
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      <style>{`
        .price-up td { background-color: rgba(22, 163, 74, 0.1); transition: background-color 0.2s ease-in-out; }
        .price-down td { background-color: rgba(220, 38, 38, 0.1); transition: background-color 0.2s ease-in-out; }
        .price-cell td { background-color: transparent; transition: background-color 0.5s ease-in-out; }
      `}</style>
    </div>
  );
};

export default MarketDataPage;