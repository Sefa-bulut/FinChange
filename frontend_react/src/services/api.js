import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => {
    if (response.data && response.data.isSuccess !== undefined) {
      return response.data; // SuccessResponse
    }
    return response;
  },
  (error) => {
    const status = error.response?.status;
    const data = error.response?.data;

    if (status === 401 || status === 403) {
      localStorage.removeItem('accessToken');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    // Backend ErrorResponse.header bazlı kullanıcı dostu mesajlar
    const header = data?.header;
    let friendly = data?.message || 'Bir hata oluştu.';
    switch (header) {
      case 'MARKET_CLOSED':
        friendly = 'Piyasa kapalıyken piyasa emri verilemez.';
        break;
      case 'INSUFFICIENT_FUNDS':
        friendly = 'Yetersiz bakiye.';
        break;
      case 'INSUFFICIENT_ASSET':
        friendly = 'Yetersiz lot.';
        break;
      case 'LIVE_PRICE_UNAVAILABLE':
        friendly = 'Canlı fiyat bulunamadı. Piyasa kapalı olabilir.';
        break;
      case 'VALIDATION ERROR':
        friendly = data?.message || data?.subErrors?.[0]?.message || 'Doğrulama hatası.';
        break;
      default:
        break;
    }

    return Promise.reject({ ...data, message: friendly });
  }
);

export default api;