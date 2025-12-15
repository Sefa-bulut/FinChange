import api from './api';

const API_URL = '/clients';

export const createClient = async (clientData, files = {}) => {
  try {
    const formData = new FormData();

    formData.append('clientData', JSON.stringify(clientData));

    if (files.vergiLevhasi) formData.append('vergiLevhasi', files.vergiLevhasi);
    if (files.kvkkBelgesi) formData.append('kvkkBelgesi', files.kvkkBelgesi);
    if (files.portfoyYonetimSozlesmesi) formData.append('portfoyYonetimSozlesmesi', files.portfoyYonetimSozlesmesi);
    if (files.elektronikBildirimIzni) formData.append('elektronikBildirimIzni', files.elektronikBildirimIzni);

    if (files.vergiLevhasi) {
      formData.append('vergiLevhasi', files.vergiLevhasi);
    }
    if (files.kvkkBelgesi) {
      formData.append('kvkkBelgesi', files.kvkkBelgesi);
    }
    if (files.portfoyYonetimSozlesmesi) {
      formData.append('portfoyYonetimSozlesmesi', files.portfoyYonetimSozlesmesi);
    }
    if (files.elektronikBildirimIzni) {
      formData.append('elektronikBildirimIzni', files.elektronikBildirimIzni);
    }

    const response = await api.post(`${API_URL}/create`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response;
  } catch (error) {
    console.error('Müşteri oluşturma API hatası:', error);
    throw error;
  }
};

export const getAllClients = async (filters = {}) => {
  try {
    const cleanFilters = Object.fromEntries(
      Object.entries(filters).filter(([_, v]) => v != null && v !== '')
    );
    const response = await api.get(API_URL, { params: cleanFilters });
    return response.result;
  } catch (error) {
    console.error("Müşteriler getirilirken hata oluştu:", error);
    throw error;
  }
};

export const getClientById = async (id) => {
  try {
    const response = await api.get(`${API_URL}/${id}`);
    return response.result;
  } catch (error) {
    console.error(`Müşteri (ID: ${id}) detayı alınırken hata oluştu:`, error);
    throw error;
  }
};

export const updateClient = async (id, clientUpdateData) => {
  try {
    const response = await api.put(`${API_URL}/${id}`, clientUpdateData);
    return response.result;
  } catch (error) {
    console.error(`Müşteri (ID: ${id}) güncellenirken hata oluştu:`, error);
    throw error;
  }
};

export const getEligibleClients = async () => {
  try {
    const response = await api.get(`${API_URL}/eligible-for-bulk-order`);
    return response.result;
  } catch (error) {
    console.error("Uygun müşteriler listesi alınırken hata oluştu:", error);
    throw error;
  }
};