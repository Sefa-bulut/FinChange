import api from './api'; 

const API_URL = '/portfolio-groups';

// Mevcut kullanıcının gruplarını getirir
export const getMyGroups = async () => {
    try {
        const response = await api.get(API_URL);
        return response.result; // SuccessResponse içindeki 'result' alanını döndür
    } catch (error) {
        console.error("Gruplar alınırken hata oluştu:", error);
        throw error;
    }
};

// Yeni bir grup oluşturur
export const createGroup = async (groupName) => {
    try {
        const response = await api.post(API_URL, { groupName });
        // SuccessResponse { isSuccess, message, result }
        return response.result ?? null;
    } catch (error) {
        console.error("Grup oluşturulurken hata oluştu:", error);
        throw error;
    }
};

// Bir grubun aktif üyelerini getirir
export const getActiveMembers = async (groupId) => {
    try {
        const response = await api.get(`${API_URL}/${groupId}/members`);
        return response.result;
    } catch (error) {
        console.error(`Grup (ID: ${groupId}) üyeleri alınırken hata oluştu:`, error);
        throw error;
    }
};

// Bir gruba yeni üyeler ekler
export const addMembersToGroup = async (groupId, customerIds) => {
    try {
        const response = await api.post(`${API_URL}/${groupId}/members`, { customerIds });
        return response.result ?? null;
    } catch (error) {
        console.error(`Gruba (ID: ${groupId}) üye eklenirken hata oluştu:`, error);
        throw error;
    }
};

// Bir gruptan bir üyeyi çıkarır
export const removeMemberFromGroup = async (groupId, customerId) => {
    try {
        const response = await api.delete(`${API_URL}/${groupId}/members/${customerId}`);
        return response.result ?? null;
    } catch (error) {
        console.error(`Gruptan (ID: ${groupId}) üye (ID: ${customerId}) çıkarılırken hata oluştu:`, error);
        throw error;
    }
};