import React, { useState } from 'react';
import AssetSelection from '../components/trading/AssetSelection';
import OrderPlacement from '../components/trading/OrderPlacement';

// Stil
const styles = {
    page: {
        padding: '30px',
        backgroundColor: '#f4f7f9',
        minHeight: 'calc(100vh - 60px)' 
    }
};

export default function TradingPage() {
    const [selectedAsset, setSelectedAsset] = useState(null);
    const [initialPriceData, setInitialPriceData] = useState(null);

    const handleAssetSelect = (asset, priceData) => {
        setSelectedAsset(asset);
        setInitialPriceData(priceData);
    };

    const handleGoBack = () => {
        setSelectedAsset(null);
        setInitialPriceData(null);
    };

    return (
        <div style={styles.page}>
            {!selectedAsset ? (
                <AssetSelection onAssetSelect={handleAssetSelect} />
            ) : (
                <OrderPlacement 
                    selectedAsset={selectedAsset} 
                    initialPriceData={initialPriceData}
                    onGoBack={handleGoBack} 
                />
            )}
        </div>
    );
}