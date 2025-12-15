import api from './api';

const marketDataService = {
  /**
   * Aktif varlıkları ve piyasa tarihini çeker.
   * Backend, veriyi bir SuccessResponse nesnesi içinde döndürür.
   * Axios interceptor'ı bu SuccessResponse nesnesini doğrudan döndürür.
   * @returns {Promise<{dates: string, assets: Array<object>}>}
   */
  getActiveAssets: async () => {
    try {
      // api.get, interceptor sayesinde { isSuccess: true, result: { dates, assets } } döndürecek.
      const successResponse = await api.get('/v1/market-data/active-assets');

      // Yanıtın başarılı olduğunu ve 'result' alanının beklenen veriyi içerdiğini kontrol edelim.
      if (successResponse && successResponse.isSuccess && successResponse.result && Array.isArray(successResponse.result.assets)) {
        // Component'in sadece gerçek veriye (payload) ihtiyacı var. Bu yüzden sadece 'result' kısmını döndürüyoruz.
        return successResponse.result;
      }
      
      console.warn("API'den (active-assets) beklenen SuccessResponse formatında veri gelmedi. Gelen:", successResponse);
      // Beklenmedik bir durumda hata oluşmaması için varsayılan bir yapı döndür.
      return { dates: '', assets: [] };

    } catch (error) {
      // Hata, axios interceptor'ında zaten formatlanmış olabilir.
      console.error("Aktif varlıklar çekilirken bir hata oluştu:", error.message || error);
      throw error;
    }
  },

  /**
   * Belirli bir varlığın detaylarını çeker.
   * Backend, veriyi bir SuccessResponse nesnesi içinde döndürür.
   * @param {string} bistCode - Detayları alınacak hissenin kodu.
   * @returns {Promise<object|null>} Varlık detayları veya null döner.
   */
  getAssetDetails: async (bistCode) => {
    try {
      // Bu çağrı da { isSuccess: true, result: { assetDetails... } } döndürecek.
      const successResponse = await api.get(`/v1/market-data/asset-details/${bistCode}`);

      // Yanıtın başarılı olduğunu ve 'result' alanının dolu olduğunu kontrol et.
      if (successResponse && successResponse.isSuccess && successResponse.result) {
        // Sadece varlık detaylarını içeren 'result' nesnesini döndür.
        return successResponse.result;
      }
      
      console.warn(`${bistCode} için API'den beklenen SuccessResponse formatında veri gelmedi. Gelen:`, successResponse);
      return null;

    } catch (error) {
      console.error(`${bistCode} için detaylar çekilirken hata oluştu:`, error.message || error);
      throw error;
    }
  },
};

export default marketDataService;