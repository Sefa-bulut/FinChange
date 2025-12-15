import React, { useState, useEffect } from 'react';
import { useMarketData } from '../../hooks/useMarketData';
import { getAssetDetails } from '../../services/AssetService';
import { toast } from 'react-toastify';

// Stil nesnesini güncelleyelim
const styles = {
    container: { maxWidth: '600px', margin: '40px auto', backgroundColor: 'white', padding: '30px', borderRadius: '12px', boxShadow: '0 6px 20px rgba(0,0,0,0.07)' },
    header: { fontSize: '24px', fontWeight: 'bold', color: '#111827', marginBottom: '25px', textAlign: 'center' },
    searchContainer: { display: 'flex', gap: '10px', marginBottom: '30px' },
    input: { flexGrow: 1, padding: '12px', fontSize: '16px', border: '1px solid #d1d5db', borderRadius: '6px', outline: 'none' },
    button: { padding: '12px 20px', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', fontSize: '16px', transition: 'background-color 0.2s' },
    searchButton: { backgroundColor: '#2563eb', color: 'white' },
    infoContainer: { border: '1px solid #e5e7eb', borderRadius: '8px', padding: '20px', backgroundColor: '#f9fafb' },
    infoGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' },
    infoItem: { fontSize: '16px', display: 'flex', flexDirection: 'column' },
    infoLabel: { color: '#6b7280', fontSize: '14px', marginBottom: '4px' },
    infoValue: { display: 'flex', alignItems: 'center', color: '#1f2937', fontWeight: '600' }, // Flexbox için güncellendi
    continueButton: { width: '100%', marginTop: '30px', backgroundColor: '#16a34a', color: 'white', padding: '15px' },
    // === YENİ EKLENEN STİLLER ===
    arrowUp: { color: '#16a34a', marginLeft: '8px', fontSize: '14px' },
    arrowDown: { color: '#dc2626', marginLeft: '8px', fontSize: '14px' },
};

// Okları render eden küçük bir yardımcı component
const PriceArrow = ({ direction }) => {
    if (direction === 'up') return <span style={styles.arrowUp}>▲</span>;
    if (direction === 'down') return <span style={styles.arrowDown}>▼</span>;
    return null; // Yön değişmediyse hiçbir şey gösterme
};

export default function AssetSelection({ onAssetSelect }) {
    const [bistCode, setBistCode] = useState('');
    const [assetDetails, setAssetDetails] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    
    // === YENİ EKLENEN STATE'LER ===
    const [previousPrice, setPreviousPrice] = useState(null);
    const [priceDirection, setPriceDirection] = useState('same'); // 'up', 'down', 'same'

    const livePriceData = useMarketData(assetDetails?.bistCode);

    // Canlı fiyat değiştiğinde yönü belirlemek için useEffect
    useEffect(() => {
        // Sadece canlı veri varsa ve bir önceki fiyattan farklıysa çalış
        if (livePriceData?.price && livePriceData.price !== previousPrice) {
            if (previousPrice) { // İlk yükleme değilse karşılaştır
                if (livePriceData.price > previousPrice) {
                    setPriceDirection('up');
                } else if (livePriceData.price < previousPrice) {
                    setPriceDirection('down');
                }
            }
            // Bir sonraki karşılaştırma için mevcut fiyatı "önceki fiyat" olarak kaydet
            setPreviousPrice(livePriceData.price);
        }
    }, [livePriceData, previousPrice]);

    // Yeni bir hisse arandığında yönü ve önceki fiyatı sıfırla
    useEffect(() => {
        if (assetDetails) {
            setPreviousPrice(assetDetails.livePrice || assetDetails.previousClose);
            setPriceDirection('same');
        }
    }, [assetDetails]);


    const handleSearch = async () => {
        if (!bistCode.trim()) return;
        setIsLoading(true);
        setAssetDetails(null);
        try {
            const details = await getAssetDetails(bistCode.trim().toUpperCase());
            if(details) {
                setAssetDetails(details);
            } else {
                throw new Error("Varlık bulunamadı.");
            }
        } catch (error) {
            toast.error(error.message || "Bir hata oluştu.");
            setAssetDetails(null);
        } finally {
            setIsLoading(false);
        }
    };

    const displayPrice = livePriceData?.price || assetDetails?.livePrice || assetDetails?.previousClose;

    // Günlük değişimi ve yönünü hesaplayan fonksiyon
    const getDailyChangeInfo = () => {
        if (!assetDetails || !displayPrice || !assetDetails.openPrice || assetDetails.openPrice === 0) {
            return { text: 'N/A', direction: 'same' };
        }
        const change = ((displayPrice / assetDetails.openPrice - 1) * 100);
        const direction = change > 0 ? 'up' : change < 0 ? 'down' : 'same';
        return {
            text: `${change.toFixed(2)}%`,
            direction: direction
        };
    };

    const dailyChangeInfo = getDailyChangeInfo();

    return (
        <div style={styles.container}>
            <h1 style={styles.header}>Varlık Seçimi</h1>

            <div style={styles.searchContainer}>
                <input 
                    type="text" 
                    placeholder="Hisse Kodu Giriniz (örn: TCELL)"
                    value={bistCode}
                    onChange={(e) => setBistCode(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                    style={styles.input}
                    disabled={isLoading}
                />
                <button onClick={handleSearch} disabled={isLoading} style={{...styles.button, ...styles.searchButton}}>
                    {isLoading ? "Aranıyor..." : "Hisse Bul"}
                </button>
            </div>

            {assetDetails && (
                <div style={styles.infoContainer}>
                    <h2 style={{ fontSize: '20px', marginBottom: '20px' }}>
                        {assetDetails.companyName} ({assetDetails.bistCode})
                    </h2>
                    <div style={styles.infoGrid}>
                        <div style={styles.infoItem}>
                            <span style={styles.infoLabel}>Fiyat:</span> 
                            {/* === FİYAT KISMI GÜNCELLENDİ === */}
                            <span style={styles.infoValue}>
                                {displayPrice ? `${Number(displayPrice).toFixed(2)} ${assetDetails.currency}` : '...'}
                                <PriceArrow direction={priceDirection} />
                            </span>
                        </div>
                        <div style={styles.infoItem}>
                            <span style={styles.infoLabel}>Günlük Değişim:</span> 
                            {/* === GÜNLÜK DEĞİŞİM KISMI GÜNCELLENDİ === */}
                            <span style={styles.infoValue}>
                                <span style={{ color: dailyChangeInfo.direction === 'up' ? '#16a34a' : dailyChangeInfo.direction === 'down' ? '#dc2626' : 'inherit' }}>
                                    {dailyChangeInfo.text}
                                </span>
                                <PriceArrow direction={dailyChangeInfo.direction} />
                            </span>
                        </div>
                        <div style={styles.infoItem}>
                            <span style={styles.infoLabel}>Gün İçi En Yüksek</span> 
                            <span style={styles.infoValue}>{assetDetails.dailyHigh?.toFixed(2) || 'N/A'}</span>
                        </div>
                        <div style={styles.infoItem}>
                            <span style={styles.infoLabel}>Gün İçi En Düşük</span> 
                            <span style={styles.infoValue}>{assetDetails.dailyLow?.toFixed(2) || 'N/A'}</span>
                        </div>
                    </div>
                    <button 
                        onClick={() => onAssetSelect({ ...assetDetails, price: displayPrice })} 
                        style={{...styles.button, ...styles.continueButton}}
                        disabled={!displayPrice}
                    >
                        Bu Hisse ile Devam Et
                    </button>
                </div>
            )}
        </div>
    );
}