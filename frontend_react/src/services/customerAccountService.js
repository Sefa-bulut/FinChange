import api from './api';

const API_URL_PREFIX = '/v1/clients';

const handleError = (error, context) => {
    console.error(`${context} sırasında hata oluştu:`, error.message || 'Bilinmeyen bir hata.');
    throw error;
};

const processResponse = (response, defaultValue = null) => {
    if (response && response.isSuccess && response.result !== undefined) {
        return response.result;
    }
    console.warn("API'den beklenen SuccessResponse formatında veri gelmedi. Dönen:", response);
    return defaultValue;
};

// --- GÜNCELLENMİŞ SERVİS FONKSİYONLARI ---

/**
 * Müşteri koduna göre ve isteğe bağlı olarak aktif/pasif durumuna göre filtrelenmiş hesapları getirir.
 * Bu fonksiyon artık hem ilk aramayı hem de filtrelemeyi yapar.
 * @param {string} customerCode Müşterinin kodu.
 * @param {boolean|null} active Filtre durumu (true: aktif, false: pasif, null/undefined: tümü).
 * @returns {Promise<Array<object>>} Bir hesap nesneleri dizisi döndürür.
 */
export const getAccountsByCustomerCodeAndStatus = async (customerCode, active) => {
    try {
        const params = {};
        // 'active' parametresi null veya undefined değilse, params nesnesine ekle.
        if (active !== null && active !== undefined) {
            params.active = active;
        }

        // Tek ve doğru endpoint'i kullanıyoruz.
        const response = await api.get(`${API_URL_PREFIX}/code/${customerCode}/accounts`, { params });
        return processResponse(response, []);
    } catch (error) {
        handleError(error, `Müşteri (${customerCode}) için hesaplar alınırken`);
    }
};

/**
 * Belirli bir müşteri için yeni bir banka hesabı oluşturur.
 * @param {number|string} clientId Müşterinin ID'si.
 * @param {object} accountData Yeni hesap bilgileri.
 * @returns {Promise<object|null>} Başarıyla oluşturulan hesap detaylarını veya null döner.
 */
export const createAccountForClient = async (clientId, accountData) => {
    try {
        const response = await api.post(`${API_URL_PREFIX}/${clientId}/accounts`, accountData);
        return processResponse(response, null);
    } catch (error) {
        handleError(error, `Müşteri (ID: ${clientId}) için hesap oluşturulurken`);
    }
};

/**
 * ID'si verilen tek bir hesabın detaylarını getirir.
 * @param {number|string} accountId Hesap ID'si.
 * @returns {Promise<object|null>} Hesap detay nesnesini veya null döner.
 */
export const getAccountById = async (accountId) => {
    try {
        const response = await api.get(`${API_URL_PREFIX}/accounts/${accountId}`);
        return processResponse(response, null);
    } catch (error) {
        handleError(error, `Hesap (ID: ${accountId}) detayları alınırken`);
    }
};

/**
 * Belirtilen hesaba para yatırır.
 * @param {number|string} accountId Hesap ID'si.
 * @param {number} amount Yatırılacak miktar.
 * @returns {Promise<object|null>} Güncellenmiş hesap durumunu içeren nesne veya null döner.
 */
export const depositToAccount = async (accountId, amount) => {
    try {
        const response = await api.post(`${API_URL_PREFIX}/accounts/${accountId}/deposit`, { amount });
        return processResponse(response, null);
    } catch (error) {
        handleError(error, `Hesaba (ID: ${accountId}) para yatırılırken`);
    }
};

/**
 * Belirtilen hesaptan para çeker.
 * @param {number|string} accountId Hesap ID'si.
 * @param {number} amount Çekilecek miktar.
 * @returns {Promise<object|null>} Güncellenmiş hesap durumunu içeren nesne veya null döner.
 */
export const withdrawFromAccount = async (accountId, amount) => {
    try {
        const response = await api.post(`${API_URL_PREFIX}/accounts/${accountId}/withdraw`, { amount });
        return processResponse(response, null);
    } catch (error) {
        handleError(error, `Hesaptan (ID: ${accountId}) para çekilirken`);
    }
};

/**
 * Belirtilen hesabın aktif/pasif durumunu değiştirir.
 * @param {number|string} accountId Hesabın ID'si.
 * @param {boolean} active Hesabın yeni durumu (true: aktif, false: pasif).
 * @returns {Promise<object|null>} Güncellenmiş hesap durumunu içeren nesne veya null döner.
 */
export const changeAccountStatus = async (accountId, active) => {
    try {
        const response = await api.patch(`${API_URL_PREFIX}/accounts/${accountId}/status`, null, {
            params: { active }
        });
        return processResponse(response, null);
    } catch (error) {
        const statusText = active ? "aktif" : "pasif";
        handleError(error, `Hesap (ID: ${accountId}) ${statusText} duruma getirilirken`);
    }
};