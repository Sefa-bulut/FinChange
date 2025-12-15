// src/services/systemDateService.js
import api from './api'; // Ana axios instance'ımızı import ediyoruz

const API_URL_PREFIX = '/v1/system-date';

/**
 * Genel Hata Yakalayıcı.
 * @param {object} error Interceptor'dan gelen hata nesnesi.
 * @param {string} context Hatanın oluştuğu işlemle ilgili bilgi.
 */
const handleError = (error, context) => {
    console.error(`${context} sırasında hata oluştu:`, error.message || 'Bilinmeyen bir hata.');
    throw error;
};

/**
 * Gelen SuccessResponse'i işleyen ve içindeki 'result' alanını döndüren yardımcı fonksiyon.
 * @param {object} response Axios interceptor'ından gelen { isSuccess, result, message } nesnesi.
 * @param {*} defaultValue Hata durumunda veya result boşsa döndürülecek varsayılan değer.
 * @returns {*} Temizlenmiş veri ('result' alanı) veya varsayılan değer.
 */
const processResponse = (response, defaultValue = null) => {
    if (response && response.isSuccess && response.result !== undefined) {
        return response.result;
    }
    // `update` gibi işlemler `result: null` dönebilir, bu bir uyarı değil.
    // Sadece `isSuccess` false ise veya `result` alanı hiç yoksa uyarı ver.
    if (!response || response.isSuccess === undefined) {
        console.warn("API'den beklenen SuccessResponse formatında veri gelmedi. Dönen:", response);
    }
    return defaultValue;
};


// --- GÜNCELLENMİŞ SERVİS FONKSİYONLARI ---

/**
 * Mevcut sistem tarihini ve saatini (LocalDateTime) backend'den alır.
 * GET /api/v1/system-date
 * @returns {Promise<string|null>} Tarih ve saati içeren ISO formatında bir string veya null.
 */
export const getCurrentSystemTime = async () => {
    try {
        const response = await api.get(API_URL_PREFIX);
        return processResponse(response, null);
    } catch (error) {
        handleError(error, 'Sistem tarihi ve saati alınırken');
    }
};

/**
 * Mevcut sistem tarihini (LocalDate) backend'den alır.
 * GET /api/v1/system-date/date
 * @returns {Promise<string|null>} Tarihi içeren 'YYYY-MM-DD' formatında bir string veya null.
 */
export const getCurrentSystemDate = async () => {
    try {
        const response = await api.get(`${API_URL_PREFIX}/date`);
        return processResponse(response, null);
    } catch (error) {
        handleError(error, 'Sistem tarihi alınırken');
    }
};

/**
 * Sistem tarihini ve saatini günceller.
 * PUT /api/v1/system-date
 * @param {object} updateData - Güncelleme verilerini içeren nesne.
 * @returns {Promise<boolean>} İşlemin başarılı olup olmadığını belirten bir boolean döner.
 */
export const updateSystemTime = async (updateData) => {
    try {
        const response = await api.put(API_URL_PREFIX, updateData);
        // Güncelleme işlemi genellikle 'result' döndürmez, sadece başarı durumunu kontrol ederiz.
        return response && response.isSuccess;
    } catch (error) {
        handleError(error, 'Sistem tarihi güncellenirken');
    }
};

// Tüm fonksiyonları tek bir nesne olarak da export edebiliriz.
const systemDateService = {
    getTime: getCurrentSystemTime,
    getDate: getCurrentSystemDate,
    update: updateSystemTime,
};

export default systemDateService;