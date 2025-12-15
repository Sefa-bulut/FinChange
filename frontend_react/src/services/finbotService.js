import api from './api';

const API_URL = '/finbot';

export const ask = async ({ question, language }) => {
  try {
    const payload = { question, language };
    const response = await api.post(`${API_URL}/ask`, payload);
    return response.result; 
  } catch (error) {
    console.error('Finbot ask çağrısı hatası:', error);
    throw error;
  }
};

export const getNews = async () => {
  try {
    const response = await api.get(`${API_URL}/turkish-news`);
    return response.result; 
  } catch (error) {
    console.error('Finbot news çağrısı hatası:', error);
    throw error;
  }
};
