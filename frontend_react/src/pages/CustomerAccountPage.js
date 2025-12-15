import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
// Servisten artık sadece tek bir birleşik listeleme fonksiyonunu import ediyoruz
import { getAccountsByCustomerCodeAndStatus } from '../services/customerAccountService';
import { toast } from 'react-toastify';

const CustomerAccountsPage = () => {
    const navigate = useNavigate();
    const location = useLocation();

    const initialCustomerCode = location.state?.customerCode || '';

    const [customerCodeInput, setCustomerCodeInput] = useState(initialCustomerCode);
    const [accounts, setAccounts] = useState([]);
    const [foundCustomer, setFoundCustomer] = useState(null); 
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    const [statusFilter, setStatusFilter] = useState(null); // null (tümü), true (aktif), false (pasif)

    const fetchAccounts = useCallback(async () => {
        if (!foundCustomer) return;

        setIsLoading(true);
        setError(null);
        try {
            const accountsData = await getAccountsByCustomerCodeAndStatus(foundCustomer.code, statusFilter);
            
            if (Array.isArray(accountsData)) {
                // API'den dönen {id: null} içeren placeholder nesneleri ayıklıyoruz.
                const realAccounts = accountsData.filter(acc => acc.id !== null);
                setAccounts(realAccounts);
            } else {
                 toast.error(`'${foundCustomer.code}' kodlu müşteri için hesaplar alınamadı.`);
                 setAccounts([]);
            }
        } catch (err) {
            setError(err.message || 'Beklenmedik bir hata oluştu.');
            setAccounts([]);
        } finally {
            setIsLoading(false);
        }
    }, [foundCustomer, statusFilter]);


    const handleSearchClick = async () => {
        const codeToSearch = customerCodeInput.trim();
        if (!codeToSearch) {
            toast.warn('Lütfen bir müşteri kodu giriniz.');
            return;
        }
        
        setIsLoading(true);
        setError(null);
        setAccounts([]);
        setFoundCustomer(null);
        setStatusFilter(null);

        try {
            const initialAccountsData = await getAccountsByCustomerCodeAndStatus(codeToSearch, null);
            
            if (Array.isArray(initialAccountsData) && initialAccountsData.length > 0 && initialAccountsData[0].customerId) {
                const customerId = initialAccountsData[0].customerId;
                setFoundCustomer({ id: customerId, code: codeToSearch });
                
                const realAccounts = initialAccountsData.filter(acc => acc.id !== null);
                setAccounts(realAccounts);
            } else {
                toast.error(`'${codeToSearch}' kodlu müşteri bulunamadı veya hiç hesabı yok.`);
                setFoundCustomer(null);
            }
        } catch (err) {
            setError(err.message || 'Beklenmedik bir hata oluştu.');
            setFoundCustomer(null);
        } finally {
            setIsLoading(false);
        }
    };
    
    useEffect(() => {
        if (initialCustomerCode && !foundCustomer) {
            // Yönlendirme ile gelindiyse, input'u doldur ve aramayı tetikle.
            setCustomerCodeInput(initialCustomerCode);
            handleSearchClick(initialCustomerCode);
        }
    }, [initialCustomerCode, foundCustomer]);
    
    useEffect(() => {
        if (foundCustomer) {
            fetchAccounts();
        }
    }, [statusFilter, foundCustomer, fetchAccounts]);


    const handleAddAccount = () => {
        if (!foundCustomer || !foundCustomer.id) {
            toast.error("Yeni hesap eklemek için önce geçerli bir müşteri bulunmalıdır.");
            return;
        }
        navigate('/dashboard/account-records/create', { state: { clientId: foundCustomer.id, customerCode: foundCustomer.code } });
    };

    const handleKeyPress = (e) => { if (e.key === 'Enter') handleSearchClick(); };

    const styles = `
        .page-container { max-width: 1200px; margin: 2rem auto; padding: 0 1rem; font-family: 'Segoe UI', system-ui, sans-serif; }
        .page-header { margin-bottom: 2rem; }
        .page-header h1 { font-size: 2.25rem; color: #111827; margin: 0 0 0.5rem 0; }
        .page-header p { font-size: 1rem; color: #4b5563; }
        .content-card { background-color: #ffffff; border-radius: 0.75rem; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1); padding: 1.5rem; }
        .search-container { display: flex; gap: 1rem; margin-bottom: 2rem; }
        .search-input { flex-grow: 1; padding: 0.75rem 1rem; font-size: 1rem; border: 1px solid #d1d5db; border-radius: 0.5rem; }
        .primary-button { padding: 0.75rem 1.5rem; font-size: 1rem; font-weight: 500; border: none; background-color: #3b82f6; color: white; border-radius: 0.5rem; cursor: pointer; }
        .primary-button:disabled { background-color: #9ca3af; cursor: not-allowed; }
        .results-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
        .results-header h2 { font-size: 1.5rem; margin: 0; }
        .add-account-btn { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.6rem 1.2rem; background-color: #10b981; color: white; border: none; border-radius: 0.5rem; cursor: pointer; font-weight: 500; }
        .data-table { width: 100%; border-collapse: collapse; }
        .data-table th, .data-table td { padding: 1rem; text-align: left; border-bottom: 1px solid #e5e7eb; }
        .data-table th { background-color: #f9fafb; color: #4b5563; font-size: 0.875rem; text-transform: uppercase; }
        .message-box { padding: 1rem; margin-top: 1.5rem; border-radius: 0.5rem; border: 1px solid; }
        .error-box { background-color: #fef2f2; color: #991b1b; border-color: #fecaca; }
        .info-box { background-color: #f0f9ff; color: #0c4a6e; border-color: #bae6fd; }
        .filter-tabs { display: flex; gap: 0.5rem; margin-bottom: 1.5rem; border-bottom: 1px solid #e5e7eb; }
        .filter-tab { padding: 0.5rem 1rem; border: none; background-color: transparent; color: #6b7280; cursor: pointer; font-weight: 500; border-bottom: 2px solid transparent; transition: color 0.2s, border-color 0.2s; }
        .filter-tab:hover { color: #3b82f6; }
        .filter-tab-active { color: #3b82f6; border-bottom-color: #3b82f6; }
        .status-badge { padding: 2px 8px; border-radius: 9999px; font-size: 12px; font-weight: 500; }
        .status-active { background-color: #dcfce7; color: #166534; }
        .status-inactive { background-color: #fee2e2; color: #991b1b; }
    `;

    return (
        <div className="page-container">
            <style>{styles}</style>
            <header className="page-header"><h1>Müşteri Hesap Yönetimi</h1><p>Müşteri kodunu girerek ilgili müşterinin hesaplarını listeleyebilir ve yönetebilirsiniz.</p></header>
            <div className="content-card">
                <div className="search-container">
                    <input type="text" value={customerCodeInput} onChange={(e) => setCustomerCodeInput(e.target.value)} onKeyPress={handleKeyPress} placeholder="Müşteri Kodunu Giriniz..." className="search-input" disabled={isLoading}/>
                    <button onClick={() => handleSearchClick(customerCodeInput)} disabled={isLoading || !customerCodeInput.trim()} className="primary-button">{isLoading ? 'Aranıyor...' : 'Müşteri Bul'}</button>
                </div>
                {foundCustomer && (
                    <div className="results-area">
                        {isLoading && <p>Yükleniyor...</p>}
                        {error && !isLoading && <div className="message-box error-box">{error}</div>}
                        {!error && !isLoading && (
                            <>
                                <div className="results-header"><h2>{foundCustomer.code} Müşteri Detayları</h2><button className="add-account-btn" onClick={handleAddAccount}>Yeni Hesap Ekle</button></div>
                                <div className="filter-tabs">
                                    <button className={`filter-tab ${statusFilter === null ? 'filter-tab-active' : ''}`} onClick={() => setStatusFilter(null)}>Tümü</button>
                                    <button className={`filter-tab ${statusFilter === true ? 'filter-tab-active' : ''}`} onClick={() => setStatusFilter(true)}>Aktif</button>
                                    <button className={`filter-tab ${statusFilter === false ? 'filter-tab-active' : ''}`} onClick={() => setStatusFilter(false)}>Pasif</button>
                                </div>
                                {accounts.length > 0 ? (
                                    <table className="data-table">
                                        <thead><tr><th>Durum</th><th>Hesap Numarası</th><th>Hesap Adı</th><th>Kullanılabilir Bakiye</th></tr></thead>
                                        <tbody>
                                            {accounts.map(account => (
                                                <tr key={account.id} style={{ opacity: account.isActive ? 1 : 0.6, cursor: 'pointer' }} onClick={() => navigate(`/dashboard/account-records/${account.id}`, { state: { customerCode: foundCustomer.code } })}>
                                                    {/* DEĞİŞİKLİK BURADA: 'active' yerine 'isActive' kullanıldı */}
                                                    <td><span className={`status-badge ${account.isActive ? 'status-active' : 'status-inactive'}`}>{account.isActive ? 'Aktif' : 'Pasif'}</span></td>
                                                    <td>{account.accountNumber}</td>
                                                    <td>{account.accountName}</td>
                                                    <td>{account.availableBalance.toFixed(2)} {account.currency}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                ) : (<div className="message-box info-box">Bu filtreye uygun hesap bulunamadı.</div>)}
                            </>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default CustomerAccountsPage;