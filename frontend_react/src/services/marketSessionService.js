import api from './api';

const API_URL = '/v1/market-session';

export const getMarketStatus = async () => {
  const res = await api.get(`${API_URL}/status`);
  return res.result || res.data?.result || res.data; 
};

export const openSettlementOverride = async () => {
  const res = await api.post(`${API_URL}/override/settlement/open`);
  return res.result || res.data?.result || res.data;
};

export const closeSettlementOverride = async () => {
  const res = await api.post(`${API_URL}/override/settlement/close`);
  return res.result || res.data?.result || res.data;
};


