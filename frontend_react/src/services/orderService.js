import api from './api';
const API_URL = '/orders';

export const createBulkOrder = async (payload) => {
    try {
        const response = await api.post(`${API_URL}/bulk`, payload);
        return response.result;
    } catch (error) {
        console.error("Toplu emir gönderilirken hata oluştu:", error);
        throw error;
    }
};

export const validateLot = async (payload) => {
    try {
        const response = await api.post(`${API_URL}/validate-lot`, payload);
        return response.result;
    } catch (error) {
        console.error("Lot validasyonu sırasında hata oluştu:", error);
        throw error;
    }
};

export const getOrders = async (filters) => {
    try {
        const response = await api.get(API_URL, {
            params: filters
        });
        return response.result;
    } catch (error) {
        console.error("Emirler getirilirken hata oluştu:", error);
        throw error;
    }
};

export const cancelOrder = async (orderId) => {
    try {
        const response = await api.delete(`${API_URL}/${orderId}`);
        return response.result;
    } catch (error) {
        console.error(`Emir ${orderId} iptal edilirken hata oluştu:`, error);
        throw error;
    }
};

export const updateOrder = async (orderId, updateData) => {
    try {
        const response = await api.put(`${API_URL}/${orderId}`, updateData);
        return response.result;
    } catch (error) {
        console.error(`Emir ${orderId} güncellenirken hata oluştu:`, error);
        throw error;
    }
};

