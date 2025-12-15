import api from './api';

const ASSETS_API_URL = '/assets';

export const getAllAssets = async (filters = {}) => {
    try {
        const response = await api.get(ASSETS_API_URL, { params: filters });
        if (response && response.data && Array.isArray(response.data.result)) {
            return response.data.result;
        } else {
            if (Array.isArray(response.data)) {
                return response.data;
            }
        }
        return [];

    } catch (error) {
        console.error("Varlıklar getirilirken hata oluştu:", error);
        return [];
    }
};

export const createAsset = async (assetData) => {
    const response = await api.post(ASSETS_API_URL, assetData);
    return response.data;
};

export const updateAsset = async (id, assetData) => {
    const response = await api.put(`${ASSETS_API_URL}/${id}`, assetData);
    return response.data;
};

export const getAssetDetails = async (bistCode) => {
    try {
        const response = await api.get(`/v1/market-data/asset-details/${bistCode}`);
        return response.result;
    } catch (error) {
        console.error(`${bistCode} için varlık detayı alınırken hata oluştu:`, error);
        throw error;
    }
};