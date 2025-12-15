import React, { useState, useEffect, useCallback } from 'react';
import { getAllAssets } from '../services/AssetService';
import AssetTable from '../components/asset/AssetTable';
import { toast } from 'react-toastify';

const pageContainerStyle = {
    maxWidth: '1200px',
    margin: '40px auto',
    padding: '20px',
};

const listContainerStyle = {
    backgroundColor: 'white',
    padding: '32px',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
    overflowX: 'auto',
};

const headerTextStyle = {
    fontSize: '22px',
    fontWeight: '600',
    color: '#333',
    margin: 0,
    marginBottom: '24px',
};

const loadingStyle = {
    textAlign: 'center',
    padding: '40px',
    fontSize: '16px',
    color: '#555',
};

export default function AssetListPage() {
    const [assets, setAssets] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    const fetchAssets = useCallback(async () => {
        setIsLoading(true);
        try {
            const responseData = await getAllAssets();
            setAssets(responseData || []);
        } catch (err) {
            toast.error('Varlık listesi yüklenirken bir hata oluştu.');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchAssets();
    }, [fetchAssets]);

    return (
        <div style={pageContainerStyle}>
            <div style={listContainerStyle}>
                <h2 style={headerTextStyle}>Mevcut Varlıklar</h2>
                {isLoading ? (
                    <div style={loadingStyle}>Yükleniyor...</div>
                ) : (
                    <AssetTable assets={assets} />
                )}
            </div>
        </div>
    );
}