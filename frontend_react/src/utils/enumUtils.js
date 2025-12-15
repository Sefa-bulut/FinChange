// Enum değerlerini frontend'te handle etmek için utility fonksiyonlar

export const getTransactionTypeDisplay = (transactionType) => {
  if (typeof transactionType === 'object' && transactionType !== null) {
    // Backend'den enum object gelirse
    return transactionType === 'BUY' ? 'Alım' : 'Satım';
  }
  // String olarak gelirse
  return transactionType === 'BUY' ? 'Alım' : 'Satım';
};

export const getOrderTypeDisplay = (orderType) => {
  if (typeof orderType === 'object' && orderType !== null) {
    // Backend'den enum object gelirse
    return orderType === 'LIMIT' ? 'Limit' : 'Piyasa';
  }
  // String olarak gelirse
  return orderType === 'LIMIT' ? 'Limit' : 'Piyasa';
};

export const getOrderStatusDisplay = (status) => {
  const statusMap = {
    'QUEUED': 'Sırada',
    'ACTIVE': 'Aktif',
    'PARTIALLY_FILLED': 'Kısmen Gerçekleşti',
    'FILLED': 'Gerçekleşti',
    'CANCELLED': 'İptal',
    'REJECTED': 'Reddedildi',
    'FAILED': 'Başarısız'
  };
  
  if (typeof status === 'object' && status !== null) {
    return statusMap[status] || status;
  }
  
  return statusMap[status] || status;
};