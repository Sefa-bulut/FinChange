import React, { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import marketDataService from '../services/marketDataService';

// Stil nesnelerini component dışında tanımlamak, her render'da yeniden oluşturulmalarını engeller.
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
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
    borderBottom: '1px solid #e5e7eb',
    paddingBottom: '16px'
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

// Yön oklarını döndüren yardımcı fonksiyon
const getArrow = (direction) => {
  if (direction === 'up') return <span style={styles.arrowUp}>▲</span>;
  if (direction === 'down') return <span style={styles.arrowDown}>▼</span>;
  return <span style={styles.arrowSame}>–</span>;
};


const MarketWatchPage = () => {
  // useRef, component render'ları arasında client nesnesini korur.
  const clientRef = useRef(null);
  
  // State'leri gruplamak yerine ayrı ayrı yönetmek daha temiz olabilir.
  const [marketData, setMarketData] = useState({});
  const [marketDate, setMarketDate] = useState('');
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(true); // Yüklenme durumu için state

  useEffect(() => {
    // Component yeniden render olduğunda tekrar tekrar bağlantı kurmasını engellemek için.
    if (clientRef.current) {
        return;
    }

    const token = localStorage.getItem('accessToken');
    if (!token) {
        setError("Giriş yapılmamış. Piyasa verileri ve anlık fiyatlar için lütfen giriş yapın.");
        setIsLoading(false);
        return;
    }

    // STOMP client'ını yapılandır
    const stompClient = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        connectHeaders: { Authorization: `Bearer ${token}` },
        reconnectDelay: 5000,
        debug: (str) => console.log(new Date(), str),
        onStompError: (frame) => {
            console.error('STOMP Broker Hatası:', frame.headers['message'], frame.body);
            setError(`WebSocket bağlantı hatası: ${frame.headers['message']}`);
            setIsLoading(false);
        },
    });

    const fetchInitialDataAndSubscribe = async () => {
      try {
        setError(null);
        setIsLoading(true);

        // Servis çağrısı, `SuccessResponse`'i işleyip bize temiz veriyi ({dates, assets}) döndürür.
        const initialData = await marketDataService.getActiveAssets();
        
        if (!initialData || !initialData.assets || initialData.assets.length === 0) {
          setError("İzlenecek aktif hisse bulunamadı. Piyasa kapalı olabilir.");
          setIsLoading(false);
          return;
        }

        // Tarihi formatlayıp state'e kaydedelim.
        if (initialData.dates) {
          const formattedDate = new Date(initialData.dates).toLocaleString('tr-TR', {
            dateStyle: 'long',
            timeStyle: 'short'
          });
          setMarketDate(formattedDate);
        }

        // Gelen veriyi, UI'da kullanacağımız formata dönüştürelim.
        const initialMarketData = {};
        initialData.assets.forEach(asset => {
          initialMarketData[asset.bistCode] = { 
            ...asset, 
            price: asset.closePrice, // Anlık fiyat için başlangıç değeri
            direction: 'same',       // Fiyat yönü
            className: 'price-cell', // Animasyon için class
          };
        });
        setMarketData(initialMarketData);
        setIsLoading(false);

        // onConnect callback'ini burada tanımlayarak `initialData.assets`'e erişmesini sağlıyoruz.
        stompClient.onConnect = () => {
          console.log('WebSocket Bağlantısı Başarılı! Abonelikler başlatılıyor...');
          initialData.assets.forEach(asset => {
            stompClient.subscribe(`/topic/prices/${asset.bistCode}`, (message) => {
              updateMarketData(JSON.parse(message.body));
            });
          });
        };
        
        // Her şey hazır, bağlantıyı aktive et
        stompClient.activate();
        clientRef.current = stompClient;

      } catch (err) {
        setError(`Piyasa verileri yüklenemedi: ${err.message || "Bilinmeyen sunucu hatası"}`);
        setIsLoading(false);
        console.error(err);
      }
    };

    fetchInitialDataAndSubscribe();

    // Cleanup fonksiyonu: Component DOM'dan kaldırıldığında çalışır.
    return () => {
      if (clientRef.current && clientRef.current.active) {
        console.log('MarketWatchPage sonlandırılıyor, WebSocket bağlantısı kapatılıyor...');
        clientRef.current.deactivate();
        clientRef.current = null;
      }
    };
  }, []); // Boş dependency array, bu useEffect'in sadece bir kez çalışmasını sağlar.

  const updateMarketData = (priceUpdate) => {
    setMarketData(prevData => {
      const { assetCode, price: newPriceStr } = priceUpdate;
      const oldData = prevData[assetCode];
      
      if (!oldData) return prevData; // Eğer hisse listede yoksa bir şey yapma

      const newPrice = Number(newPriceStr);
      const oldPrice = Number(oldData.price);
      
      let direction = oldData.direction;
      if (newPrice > oldPrice) direction = 'up';
      else if (newPrice < oldPrice) direction = 'down';

      // Animasyon için class ismini ayarla
      const className = direction === 'up' ? 'price-up' : (direction === 'down' ? 'price-down' : 'price-cell');
      const updatedAsset = { ...oldData, price: newPrice, direction, className };

      // Rengi kısa bir süre sonra sıfırlamak için zamanlayıcı
      setTimeout(() => {
        setMarketData(currentData => {
          if (currentData[assetCode] && currentData[assetCode].className !== 'price-cell') {
            return { ...currentData, [assetCode]: { ...currentData[assetCode], className: 'price-cell' } };
          }
          return currentData;
        });
      }, 500); // Animasyon süresi

      return { ...prevData, [assetCode]: updatedAsset };
    });
  };

  if (isLoading) {
    return <div style={styles.container}><h1>Piyasa Verileri Yükleniyor...</h1></div>;
  }
  
  if (error) {
    return <div style={{...styles.container, color: '#dc2626' }}><h1>Hata</h1><p>{error}</p></div>;
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1>Piyasa İzleme Ekranı</h1>
        <strong>Piyasa Tarihi: {marketDate || 'Bilinmiyor'}</strong>
      </div>
      
      <table style={styles.table}>
        <thead>
          <tr>
            <th style={styles.th}>Hisse</th>
            <th style={{...styles.th, textAlign: 'right'}}>Anlık Fiyat</th>
            <th style={{...styles.th, textAlign: 'center', width: '60px'}}>Yön</th>
            <th style={{...styles.th, textAlign: 'right'}}>Açılış</th>
            <th style={{...styles.th, textAlign: 'right'}}>Gün İçi Yüksek</th>
            <th style={{...styles.th, textAlign: 'right'}}>Gün İçi Düşük</th>
            <th style={{...styles.th, textAlign: 'right'}}>Kapanış</th>
          </tr>
        </thead>
        <tbody>
          {Object.values(marketData).map(asset => (
            <tr key={asset.bistCode} className={asset.className}>
              <td style={{...styles.td, fontWeight: 'bold'}}>{asset.bistCode}</td>
              <td style={{...styles.td, textAlign: 'right', fontWeight: 'bold', fontSize: '1.1em'}}>{Number(asset.price).toFixed(2)} TL</td>
              <td style={{...styles.td, textAlign: 'center'}}>{getArrow(asset.direction)}</td>
              <td style={{...styles.td, textAlign: 'right', color: '#4b5563'}}>{Number(asset.openPrice).toFixed(2)}</td>
              <td style={{...styles.td, textAlign: 'right', color: '#4b5563'}}>{Number(asset.highPrice).toFixed(2)}</td>
              <td style={{...styles.td, textAlign: 'right', color: '#4b5563'}}>{Number(asset.lowPrice).toFixed(2)}</td>
              <td style={{...styles.td, textAlign: 'right', color: '#4b5563'}}>{Number(asset.closePrice).toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <style>{`
        .price-up td { background-color: rgba(22, 163, 74, 0.1); transition: background-color 0.2s ease-in-out; }
        .price-down td { background-color: rgba(220, 38, 38, 0.1); transition: background-color 0.2s ease-in-out; }
        .price-cell td { background-color: transparent; transition: background-color 0.5s ease-in-out; }
      `}</style>
    </div>
  );
};

export default MarketWatchPage;