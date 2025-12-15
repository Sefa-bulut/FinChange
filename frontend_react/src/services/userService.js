import api from './api';

const BASE_URL = '/users';


export const getUsers = async (filters = {}) => {
    try {
        const cleanFilters = {};
        for (const key in filters) {
            const value = filters[key];
            if (value != null && value !== '') {
                cleanFilters[key] = value;
            }
        }

        const params = new URLSearchParams(cleanFilters).toString();
        const response = await api.get(`/users?${params}`);
        return response;
    } catch (error) {
        console.error("Personel listesi alınırken hata oluştu:", error);
        throw error;
    }
};

export const inviteUser = async (userData) => {
    try {
        const response = await api.post(`${BASE_URL}/invite`, userData);
        return response;
    } catch (error) {
        console.error("Personel davet edilirken hata oluştu:", error);
        throw error;
    }
};


export const updateUserRoles = async (userId, roleNames) => {
    try {
        const response = await api.put(`${BASE_URL}/${userId}/roles`, { roleNames });
        return response;
    } catch (error) {
        console.error("Personel rolleri güncellenirken hata oluştu:", error);
        throw error;
    }
};


export const updateUserStatus = async (userId, isActive) => {
    try {
        const response = await api.put(`${BASE_URL}/${userId}/status`, { isActive });
        return response;
    } catch (error) {
        console.error("Personel durumu güncellenirken hata oluştu:", error);
        throw error;
    }
};

// YENİ: Tek bir kullanıcıyı ID ile getiren servis
export const getUserById = async (userId) => {
    try {
        const response = await api.get(`${BASE_URL}/${userId}`);
        return response;
    } catch (error) {
        console.error(`Personel (ID: ${userId}) bilgileri alınırken hata oluştu:`, error);
        throw error;
    }
};


export const updateUserInfo = async (userId, userInfo) => {
    try {
        const response = await api.put(`${BASE_URL}/${userId}/info`, userInfo);
        return response;
    } catch (error) {
        console.error("Frontend - Kullanıcı bilgileri güncelleme hatası:", error);
        throw error;
    }
};

export const updateUser = async (userId, userData) => {
    try {
        const response = await api.put(`${BASE_URL}/${userId}`, userData);
        return response;
    } catch (error) {
        console.error(`Personel (ID: ${userId}) güncellenirken hata oluştu:`, error);
        throw error;
    }
};