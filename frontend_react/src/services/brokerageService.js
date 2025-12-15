import api from './api';

const API_URL = '/brokerage-firms';

export const getAllFirms = async () => {
    const response = await api.get(API_URL);
    return response.result;
};

export const getFirmById = async (id) => {
    const response = await api.get(`${API_URL}/${id}`);
    return response.result;
};

export const createFirm = async (firmData) => {
    const response = await api.post(API_URL, firmData);
    return response.result;
};

export const updateFirm = async (id, firmData) => {
    const response = await api.put(`${API_URL}/${id}`, firmData);
    return response.result;
};

export const deleteFirm = async (id) => {
    await api.delete(`${API_URL}/${id}`);
};

export const activateFirm = async (id) => {
    const response = await api.post(`${API_URL}/${id}/activate`);
    return response.result;
};
