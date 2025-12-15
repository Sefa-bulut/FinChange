import React, { memo, useMemo } from 'react';
import { FaLock, FaInfoCircle } from 'react-icons/fa';

function getEffectivePrice(orderType, limitPriceInput, currentPrice) {
  if (orderType === 'LIMIT') {
    const lp = Number(limitPriceInput);
    return isNaN(lp) || lp <= 0 ? 0 : lp;
  }
  return Number(currentPrice) || 0;
}

function CustomerRow({
  member,
  account,
  order,
  selectedAsset,
  transactionType,
  orderType,
  limitPrice,
  currentPrice,
  onSelectCustomer,
  onLotChange,
  onUnlockLot,
  onShowSettlementInfo,
  styles
}) {
  const assetCurrency = selectedAsset.currency;

  const isAccountEligible = useMemo(() => {
    if (transactionType === 'BUY') {
      return account.currency === assetCurrency;
    } else {
      return !!(account.assets && account.assets.some(asset => asset.bistCode === selectedAsset.bistCode));
    }
  }, [transactionType, account.currency, account.assets, assetCurrency, selectedAsset.bistCode]);

  const assetInfoForSell = useMemo(() => {
    if (transactionType !== 'SELL') return null;
    return (account.assets || []).find(a => a.bistCode === selectedAsset.bistCode) || null;
  }, [transactionType, account.assets, selectedAsset.bistCode]);

  const availableLotsForSell = assetInfoForSell ? (assetInfoForSell.availableLots || 0) : 0;

  const { availableBalanceForBuy, maxLotsForBuy } = useMemo(() => {
    const effectivePriceRow = getEffectivePrice(orderType, limitPrice, currentPrice);
    const availableBalance = Math.max(0, (Number(account.balance) || 0) - (Number(account.blockedBalance) || 0));
    const maxLots = effectivePriceRow > 0 ? Math.floor(availableBalance / effectivePriceRow) : 0;
    return { availableBalanceForBuy: availableBalance, maxLotsForBuy: maxLots };
  }, [orderType, limitPrice, currentPrice, account.balance, account.blockedBalance]);

  return (
    <tr key={account.id} style={!isAccountEligible ? { backgroundColor: '#f8f9fa', color: '#6c757d' } : {}}>
      <td style={styles.td}>
        <input
          type="checkbox"
          checked={isAccountEligible && (order?.isSelected || false)}
          onChange={(e) => onSelectCustomer(account.id, e.target.checked)}
          disabled={!isAccountEligible}
        />
      </td>
      <td style={styles.td}>{member.customerCode}</td>
      <td style={styles.td}>{member.fullName}</td>
      <td style={styles.td}>{account.accountNumber}</td>
      <td style={styles.td}>{account.accountName}</td>
      <td style={styles.td}>
        {transactionType === 'BUY'
          ? `${availableBalanceForBuy.toFixed(2)} ${account.currency}`
          : (
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              <span>{Math.max(0, availableLotsForSell)} Lot</span>
              {assetInfoForSell?.blockedLot > 0 && (
                <FaInfoCircle
                  title="T+2 takas bilgisi"
                  style={{ cursor: 'pointer', color: '#6c757d' }}
                  onClick={() => onShowSettlementInfo(member.customerId)}
                />
              )}
            </span>
          )}
      </td>
      <td style={styles.td}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <input
            type="number"
            min="0"
            max={transactionType === 'BUY' ? maxLotsForBuy : availableLotsForSell}
            value={order?.lot || 0}
            onChange={(e) => onLotChange(account.id, e.target.value)}
            style={styles.lotInput}
            disabled={!isAccountEligible}
          />
          {order?.isLocked && (
            <FaLock
              style={{ cursor: 'pointer', color: '#c8102e' }}
              title="Lot miktarı kilitli. Otomatik hesaplamayı açmak için tıklayın."
              onClick={() => onUnlockLot(account.id)}
            />
          )}
        </div>
      </td>
      <td style={styles.td}>
        {!isAccountEligible
          ? (transactionType === 'BUY' ? `Sadece ${assetCurrency}` : 'Lot Yok')
          : (order?.error ? (
            <div style={styles.validationError}>{order.error}</div>
          ) : order?.isValid && order?.lot > 0 ? (
            <div style={styles.validationSuccess}>✓ Geçerli</div>
          ) : (
            <div style={{ color: '#6c757d', fontSize: '12px' }}>-</div>
          ))}
      </td>
    </tr>
  );
}

function areEqual(prev, next) {
  return (
    prev.order?.lot === next.order?.lot &&
    prev.order?.isLocked === next.order?.isLocked &&
    prev.order?.isSelected === next.order?.isSelected &&
    prev.order?.isValid === next.order?.isValid &&
    prev.order?.error === next.order?.error &&
    prev.transactionType === next.transactionType &&
    prev.orderType === next.orderType &&
    prev.limitPrice === next.limitPrice &&
    prev.currentPrice === next.currentPrice &&
    prev.selectedAsset.bistCode === next.selectedAsset.bistCode &&
    prev.selectedAsset.currency === next.selectedAsset.currency &&
    prev.account.id === next.account.id &&
    prev.account.balance === next.account.balance &&
    prev.account.blockedBalance === next.account.blockedBalance &&
    JSON.stringify(prev.account.assets || []) === JSON.stringify(next.account.assets || [])
  );
}

export default memo(CustomerRow, areEqual);
