// src/services/authService.js
import api from './api';

const API_URL_PREFIX = '/v1/auth';

const handleError = (error, context) => {
    console.error(`${context} sırasında hata oluştu:`, error.message || 'Bilinmeyen bir hata.');
    throw error;
};

const processResponse = (response, defaultValue = null) => {
    if (response && response.isSuccess && response.result !== undefined) {
        return response.result;
    }
    if (!response || response.isSuccess === undefined) {
        console.warn("API'den beklenen SuccessResponse formatında veri gelmedi. Dönen:", response);
    }
    return defaultValue;
};


/**
 * Kullanıcı giriş işlemini gerçekleştirir.
 * @param {string} email - Kullanıcının e-posta adresi.
 * @param {string} password - Şifre.
 * @returns {Promise<object|null>} Başarılı giriş sonrası token ve kullanıcı bilgilerini içeren nesne.
 */
export const login = async (email, password) => { // <-- DEĞİŞİKLİK: 'username' -> 'email'
    try {
        // DEĞİŞİKLİK: Gönderilen JSON nesnesindeki anahtar 'email' olarak düzeltildi.
        const response = await api.post(`${API_URL_PREFIX}/login`, { email, password });
        return processResponse(response, null);
    } catch (error) {
        handleError(error, 'Giriş yapılırken');
    }
};


/**
 * Zorunlu şifre değişikliği işlemini gerçekleştirir.
 * @param {string} newPassword - Yeni şifre.
 * @param {string} confirmPassword - Yeni şifre tekrarı.
 * @returns {Promise<boolean>} İşlemin başarılı olup olmadığını belirten boolean.
 */
export const forceChangePassword = async (newPassword, confirmPassword) => {
    try {
        const response = await api.post(`${API_URL_PREFIX}/force-change-password`, { newPassword, confirmPassword });
        return response && response.isSuccess;
    } catch (error) {
        handleError(error, 'Zorunlu şifre değişikliği sırasında');
    }
};

/**
 * Şifre sıfırlama kodu gönderilmesini talep eder.
 * @param {string} email - Kullanıcının e-posta adresi.
 * @returns {Promise<string|null>} Başarı durumunda sunucudan gelen mesajı döndürür.
 */
export const forgotPassword = async (email) => {
    try {
        const response = await api.post(`${API_URL_PREFIX}/forgot-password`, { email });
        return processResponse(response, null);
    } catch (error) {
        handleError(error, 'Şifre sıfırlama talebi gönderilirken');
    }
};

/**
 * Sıfırlama kodu ve yeni şifre ile şifreyi günceller.
 * @param {string} token - Sıfırlama kodu.
 * @param {string} newPassword - Yeni şifre.
 * @returns {Promise<string|null>} Başarı durumunda sunucudan gelen mesajı döndürür.
 */
export const resetPassword = async (token, newPassword) => {
    try {
        const response = await api.post(`${API_URL_PREFIX}/reset-password`, { token, newPassword });
        return processResponse(response, null);
    } catch (error) {
        handleError(error, 'Şifre sıfırlanırken');
    }
};

/**
 * Refresh token kullanarak yeni access token alır.
 * @param {string} refreshToken - Mevcut refresh token.
 * @returns {Promise<object|null>} Yeni token'ları içeren nesne.
 */
export const refreshToken = async (refreshToken) => {
    try {
        const response = await api.post(`${API_URL_PREFIX}/refresh`, { refreshToken });
        return processResponse(response, null);
    } catch (error) {
        handleError(error, 'Token yenilenirken');
    }
};

/**
 * Kullanıcı çıkış işlemini gerçekleştirir.
 * @param {string} accessToken
 * @param {string} refreshToken
 * @returns {Promise<void>}
 */
export const logout = async (accessToken, refreshToken) => {
    try {
        await api.post(`${API_URL_PREFIX}/logout`, { accessToken, refreshToken });
    } catch (error) {
        console.error("Sunucu tarafında logout başarısız oldu, yine de client temizleniyor.", error);
    }
};