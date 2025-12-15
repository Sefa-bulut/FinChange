import api from './api'; // Ana axios instance'ımızı import ediyoruz

/**
 * Yeni bir tatil kaydı oluşturur.
 * @param {object} holidayData - Tatil verilerini içeren nesne.
 * Örnek: { holidayDate: '2025-10-29', type: 'RESMI_TATIL', description: 'Yeni Tatil' }
 * @returns {Promise<object>} Oluşturulan tatil nesnesini döndürür.
 */
export const createHoliday = async (holidayData) => {
    // api.js'deki interceptor sayesinde yanıt zaten { isSuccess, result, ... } formatında geliyor.
    // Biz doğrudan sonucu (result) döndürüyoruz.
    const response = await api.post('/v1/holidays', holidayData);
    return response.result;
};

/**
 * Mevcut bir tatil kaydını günceller.
 * @param {number} id - Güncellenecek tatilin ID'si.
 * @param {object} holidayData - Yeni tatil verileri.
 * @returns {Promise<object>} Güncellenen tatil nesnesini döndürür.
 */
export const updateHoliday = async (id, holidayData) => {
    const response = await api.put(`/v1/holidays/${id}`, holidayData);
    return response.result;
};

/**
 * Belirtilen ID'ye sahip tatil kaydını siler.
 * @param {number} id - Silinecek tatilin ID'si.
 * @returns {Promise<object>} Backend'den dönen başarı yanıtını döndürür.
 */
export const deleteHoliday = async (id) => {
    const response = await api.delete(`/v1/holidays/${id}`);
    return response; // Genellikle result null olacağı için tüm yanıtı dönebiliriz.
};

/**
 * Tüm tatil kayıtlarını listeler.
 * @returns {Promise<Array<object>>} Tatil nesnelerinden oluşan bir dizi döndürür.
 */
export const getAllHolidays = async () => {
    const response = await api.get('/v1/holidays');
    return response.result;
};

/**
 * Belirtilen ID'ye sahip tek bir tatil kaydını getirir.
 * @param {number} id - Getirilecek tatilin ID'si.
 * @returns {Promise<object>} Bulunan tatil nesnesini döndürür.
 */
export const getHolidayById = async (id) => {
    const response = await api.get(`/v1/holidays/${id}`);
    return response.result;
};

/**
 * Belirtilen tarihe göre tatil kayıtlarını getirir.
 * @param {string} date - 'YYYY-MM-DD' formatında tarih.
 * @returns {Promise<Array<object>>} O tarihe ait tatil kayıtlarını döndürür.
 */
export const getHolidaysByDate = async (date) => {
    const response = await api.get('/v1/holidays/by-date', {
        params: {
            date: date, // axios, bunu /by-date?date=YYYY-MM-DD şeklinde bir URL'e çevirecektir.
        }
    });
    return response.result;
};

// Tüm fonksiyonları tek bir nesne olarak da export edebiliriz.
const holidayService = {
    create: createHoliday,
    update: updateHoliday,
    delete: deleteHoliday,
    getAll: getAllHolidays,
    getById: getHolidayById,
    getByDate: getHolidaysByDate,
};

export default holidayService;