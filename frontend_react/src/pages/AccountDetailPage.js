import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { getAccountById, depositToAccount, withdrawFromAccount, changeAccountStatus } from '../services/customerAccountService';

const AccountDetailPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { accountId } = useParams();

    const [account, setAccount] = useState(null);
    const [loading, setLoading] = useState(true);
    const [successMessage, setSuccessMessage] = useState('');
    const [error, setError] = useState('');
    const [depositAmount, setDepositAmount] = useState('');
    const [withdrawAmount, setWithdrawAmount] = useState('');
    const [isDepositing, setIsDepositing] = useState(false);
    const [isWithdrawing, setIsWithdrawing] = useState(false);
    const [isChangingStatus, setIsChangingStatus] = useState(false);
    
    const customerCode = location.state?.customerCode;

    const formatMoney = (val) => {
        const n = typeof val === 'number' ? val : parseFloat(val);
        if (Number.isNaN(n)) return '0.00';
        return n.toFixed(2);
    };
    const formatMoneyN = (val, decimals = 2) => {
        const n = typeof val === 'number' ? val : parseFloat(val);
        if (Number.isNaN(n)) return (0).toFixed(decimals);
        return n.toFixed(decimals);
    };
    const fillWithdraw = (ratio) => {
        if (!account) return;
        const avail = typeof account.availableBalance === 'number' ? account.availableBalance : parseFloat(account.availableBalance);
        if (Number.isNaN(avail)) return;
        const raw = Math.max(0, avail * ratio);
        const floored = Math.floor(raw * 100) / 100;
        setWithdrawAmount(floored.toFixed(2));
    };
    useEffect(() => {
        const fetchAccount = async () => {
            if (!accountId) {
                setError('Hesap ID eksik.');
                setLoading(false);
                return;
            }
            try {
                const accountData = await getAccountById(accountId);
                if (accountData) {
                    setAccount(accountData);
                } else {
                    setError('Hesap bulunamadı.');
                }
            } catch (err) {
                setError('Hesap bilgileri alınamadı: ' + (err.message || 'Bilinmeyen hata'));
            } finally {
                setLoading(false);
            }
        };
        fetchAccount();
    }, [accountId]);

    const handleDeposit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccessMessage('');
        if (!depositAmount || parseFloat(depositAmount) <= 0) {
            setError("Lütfen geçerli bir miktar girin.");
            return;
        }
        setIsDepositing(true);
        try {
            const updatedAccount = await depositToAccount(accountId, parseFloat(depositAmount));
            if (updatedAccount) {
                setAccount(updatedAccount);
                setDepositAmount('');
                setSuccessMessage("Para yatırma işlemi başarılı.");
            }
        } catch (err) {
            setError("Para yatırılamadı: " + (err.message || "Bilinmeyen hata"));
        } finally {
            setIsDepositing(false);
            setTimeout(() => setSuccessMessage(''), 3000);
        }
    };

    const handleWithdraw = async (e) => {
        e.preventDefault();
        setError('');
        setSuccessMessage('');
        if (!withdrawAmount || parseFloat(withdrawAmount) <= 0) {
            setError("Lütfen geçerli bir miktar girin.");
            return;
        }
        const amt = parseFloat(withdrawAmount);
        if (account && typeof account.availableBalance === 'number' && amt > account.availableBalance) {
            setError(`Çekim miktarı kullanılabilir bakiyeden fazla. Maksimum: ${account.availableBalance.toFixed(2)} ${account.currency}`);
            return;
        }
        setIsWithdrawing(true);
        try {
            const updatedAccount = await withdrawFromAccount(accountId, amt);
            if (updatedAccount) {
                setAccount(updatedAccount);
                setWithdrawAmount('');
                setSuccessMessage("Para çekme işlemi başarılı.");
            }
        } catch (err) {
            setError("Para çekilemedi: " + (err.message || "Bilinmeyen hata"));
        } finally {
            setIsWithdrawing(false);
            setTimeout(() => setSuccessMessage(''), 3000);
        }
    };

    const handleStatusChange = async (newStatus) => {
        if (!account) return;
        const statusText = newStatus ? "aktifleştirmek" : "pasifleştirmek";
        const confirmed = window.confirm(`Bu hesabı ${statusText} istediğinizden emin misiniz?`);
        if (!confirmed) return;

        setIsChangingStatus(true);
        setError('');
        setSuccessMessage('');
        try {
            const updatedAccount = await changeAccountStatus(accountId, newStatus);
            if (updatedAccount) {
                setAccount(updatedAccount);
                setSuccessMessage(`Hesap başarıyla ${newStatus ? 'aktif' : 'pasif'} duruma getirildi.`);
            }
        } catch (err) {
            setError(err.message || `Hesap durumu değiştirilemedi.`);
        } finally {
            setIsChangingStatus(false);
            setTimeout(() => setSuccessMessage(''), 3000);
        }
    };
    
    const goBack = () => {
        if (customerCode) {
            navigate('/dashboard/account-records', { state: { customerCode } });
        } else {
            navigate(-1);
        }
    };

    const styles = `
        .page-container { max-width: 900px; margin: 1.5rem auto; padding: 1rem; font-family: 'Segoe UI', system-ui, sans-serif; background-color: #f8f9fa; border-radius: 8px; }
        .page-header { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; padding-bottom: 0.75rem; border-bottom: 1px solid #e0e0e0; }
        .page-header h1 { font-size: 1.75rem; color: #333; margin: 0; font-weight: 600; }
        .content-card { background-color: #ffffff; border-radius: 0.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.06); padding: 1.25rem; margin-top: 1rem; border: 1px solid #e9ecef; }
        .primary-button { padding: 0.5rem 1rem; font-size: 0.875rem; font-weight: 500; border: none; background-color: #3f51b5; color: white; border-radius: 0.375rem; cursor: pointer; transition: background-color 0.2s, transform 0.1s; }
        .primary-button:hover { background-color: #303f9f; transform: translateY(-1px); }
        .primary-button:disabled { background-color: #bdbdbd; cursor: not-allowed; }
        .back-button { background-color: #f0f0f0; color: #555; } 
        .back-button:hover { background-color: #e0e0e0; }
        .section-title { font-size: 1.25rem; color: #333; border-bottom: 1px solid #e0e0e0; padding-bottom: 0.6rem; margin: 0 0 1rem 0; font-weight: 600; } 
        .message-box { padding: 0.75rem; margin-bottom: 1rem; border-radius: 0.375rem; border: 1px solid; font-size: 0.875rem; }
        .error-box { background-color: #ffebee; color: #d32f2f; border-color: #ef9a9a; }
        .success-box { background-color: #e8f5e9; color: #388e3c; border-color: #a5d6a7; }
        .main-layout-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
        .details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; } 
        .detail-item > dt { font-size: 0.8rem; color: #757575; margin-bottom: 0.2rem; font-weight: 500; }
        .detail-item > dd { font-size: 1rem; color: #424242; font-weight: 600; margin: 0; } 
        .balance-prominent { grid-column: 1 / -1; text-align: center; background-color: #f8f9fa; padding: 1rem; border-radius: 0.5rem; border: 1px solid #e9ecef; margin-top: 0.75rem; } 
        .balance-prominent dt { font-size: 0.9rem; color: #6c757d; font-weight: 500; }
        .balance-prominent dd { font-size: 2rem; letter-spacing: -0.05em; font-weight: 700; color: #212121; }
        /* DEĞİŞİKLİK BURADA: Yazı tipi boyutu ve boşluk artırıldı */
        .balance-breakdown { 
            font-size: 0.95rem; /* Boyut artırıldı */
            color: #616161; /* Biraz daha koyu renk */
            margin-top: 0.75rem; /* Üst boşluk artırıldı */
            font-weight: 500;
        }
        .transaction-form-grid { display: flex; flex-direction: column; gap: 1rem; } 
        .transaction-form-vertical { display: flex; flex-direction: column; gap: 0.5rem; } 
        .form-label { font-size: 0.85rem; font-weight: 500; color: #424242; margin-bottom: 0.25rem; } 
        .form-input { padding: 0.6rem; border: 1px solid #bdbdbd; border-radius: 0.375rem; font-size: 0.9rem; width: 100%; box-sizing: border-box; }
        .transaction-button-deposit { background-color: #4caf50; } 
        .transaction-button-deposit:hover { background-color: #43a047; }
        .transaction-button-withdraw { background-color: #ff9800; } 
        .transaction-button-withdraw:hover { background-color: #fb8c00; }
        .status-zone { border: 2px solid #64b5f6; padding: 1.25rem; margin-top: 1.5rem; background-color: #e3f2fd; border-radius: 0.5rem; } 
        .status-zone h3 { font-size: 1.15rem; margin-top: 0; margin-bottom: 0.5rem; color: #1e88e5; }
        .status-zone p { font-size: 0.85rem; margin-bottom: 1rem; color: #424242; }
        .status-button { margin-right: 10px; }
        .activate-button { background-color: #4caf50; }
        .activate-button:hover { background-color: #43a047; }
        .deactivate-button { background-color: #f44336; }
        .deactivate-button:hover { background-color: #d32f2f; }
    `;

    if (loading) return (
        <div className="page-container" style={{ textAlign: 'center' }}>
            <style>{`
                @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
            `}</style>
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '12px', padding: '24px' }}>
                <div style={{ width: 24, height: 24, border: '3px solid #e0e0e0', borderTopColor: '#3f51b5', borderRadius: '50%', animation: 'spin 1s linear infinite' }} />
                <span style={{ fontSize: '0.95rem', color: '#555' }}>Yükleniyor...</span>
            </div>
        </div>
    );
    
    if (!account) {
        return (
            <div className="page-container">
                <style>{styles}</style>
                <header className="page-header">
                    <button className="primary-button back-button" onClick={goBack}>← Geri</button>
                    <h1>Hesap Detayı</h1>
                </header>
                <div className="message-box error-box">{error || 'Hesap bilgileri yüklenemedi veya bulunamadı.'}</div>
            </div>
        );
    }

    return (
        <div className="page-container">
            <style>{styles}</style>
            <header className="page-header">
                <button className="primary-button back-button" onClick={goBack}>← Geri</button>
                <h1>Hesap Detayı</h1>
            </header>

            {(error || successMessage) && (
                <div className={`message-box ${error ? 'error-box' : 'success-box'}`}>{error || successMessage}</div>
            )}

            <div className="main-layout-grid">
                <div>
                    <div className="content-card">
                        <h2 className="section-title">Hesap Bilgileri</h2>
                        <dl className="details-grid">
                            <div className="detail-item"><dt>Hesap Adı</dt><dd>{account.accountName}</dd></div>
                            <div className="detail-item"><dt>Müşteri Kodu</dt><dd>{customerCode || 'Bilinmiyor'}</dd></div>
                            <div className="detail-item"><dt>Hesap Numarası</dt><dd>{account.accountNumber}</dd></div>
                            <div className="detail-item"><dt>Durum</dt><dd style={{ color: account.isActive ? '#28a745' : '#dc3545', fontWeight: 'bold' }}>{account.isActive ? 'Aktif' : 'Pasif'}</dd></div>
                            
                            <div className="balance-prominent">
                                <dt>Kullanılabilir Bakiye</dt>
                                <dd style={{ color: account.availableBalance >= 0 ? '#28a745' : '#dc3545' }}>
                                    {formatMoneyN(account.availableBalance, 4)} {account.currency}
                                </dd>
                                <div className="balance-breakdown">
                                    <span style={{ marginRight: '16px' }}>Toplam: {formatMoney(account.balance)}</span>
                                    <span>Bloke: {formatMoney(account.blockedBalance)}</span>
                                </div>
                            </div>
                        </dl>
                    </div>

                    <div className="status-zone">
                        <h3>Hesap Durumu Yönetimi</h3>
                        <p>Hesabı pasif duruma getirmek, hesaptan para çekme ve alım/satım işlemlerini engeller.</p>
                        {account.isActive ? (
                            <button onClick={() => handleStatusChange(false)} disabled={isChangingStatus} className="primary-button deactivate-button status-button">{isChangingStatus ? 'İşleniyor...' : 'Hesabı Pasif Et'}</button>
                        ) : (
                            <button onClick={() => handleStatusChange(true)} disabled={isChangingStatus} className="primary-button activate-button status-button">{isChangingStatus ? 'İşleniyor...' : 'Hesabı Aktif Et'}</button>
                        )}
                    </div>
                </div>

                <div>
                    <div className="content-card">
                        <h2 className="section-title">Hesap Hareketleri</h2>
                        <div className="transaction-form-grid">
                            <form onSubmit={handleDeposit} className="transaction-form-vertical">
                                <div className="form-group"><label className="form-label" htmlFor="deposit">Yatırılacak Miktar</label><input id="deposit" type="number" step="0.01" value={depositAmount} onChange={e => setDepositAmount(e.target.value)} placeholder="0.00" className="form-input"/></div>
                                <button type="submit" disabled={isDepositing || !account.isActive} className="primary-button transaction-button-deposit">{isDepositing ? 'Yatırılıyor...' : 'Para Yatır'}</button>
                            </form>
                            <form onSubmit={handleWithdraw} className="transaction-form-vertical">
                                <div className="form-group">
                                    <label className="form-label" htmlFor="withdraw">Çekilecek Miktar</label>
                                    <input id="withdraw" type="number" step="0.01" value={withdrawAmount} onChange={e => setWithdrawAmount(e.target.value)} placeholder="0.00" className="form-input"/>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
                                        <small style={{ color: '#6c757d' }}>Maksimum: {formatMoneyN(account?.availableBalance, 4)} {account?.currency}</small>
                                        <div style={{ display: 'inline-flex', gap: 6 }}>
                                            <button type="button" onClick={() => fillWithdraw(0.25)} className="primary-button" style={{ backgroundColor: '#e0e0e0', color: '#333' }}>%25</button>
                                            <button type="button" onClick={() => fillWithdraw(0.5)} className="primary-button" style={{ backgroundColor: '#e0e0e0', color: '#333' }}>%50</button>
                                            <button type="button" onClick={() => fillWithdraw(1)} className="primary-button" style={{ backgroundColor: '#e0e0e0', color: '#333' }}>Tümü</button>
                                        </div>
                                    </div>
                                </div>
                                <button type="submit" disabled={isWithdrawing || !account.isActive} className="primary-button transaction-button-withdraw">{isWithdrawing ? 'Çekiliyor...' : 'Para Çek'}</button>
                            </form>
                        </div>
                        {!account.isActive && (<p style={{color: '#dc3545', textAlign: 'center', marginTop: '1rem', fontWeight: '500'}}>Hesap pasif olduğu için işlem yapılamaz.</p>)}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AccountDetailPage;