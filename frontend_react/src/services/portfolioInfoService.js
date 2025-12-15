import api from './api';

export async function getSettlementInfoByBist(customerId, bistCode) {
  const url = `/portfolio/customers/${customerId}/assets/by-bist/${encodeURIComponent(bistCode)}/settlement-info`;
  try {
    const { data } = await api.get(url);
    return data;
  } catch (e) {
    const resp = e?.response?.data;
    if (resp && typeof resp === 'object') return resp;
    throw e;
  }
}
