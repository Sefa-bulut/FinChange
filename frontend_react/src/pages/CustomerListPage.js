import React, { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getAllClients } from '../services/customerService';
import CustomerTable from '../components/CustomerTable';
import { useAuth } from '../context/AuthContext';

/* ==== İsim/Şirket akıllı filtre yardımcıları ==== */
const normalizeTR = (s = '') =>
    s
        .toLocaleLowerCase('tr')
        .replace(/ç/g, 'c')
        .replace(/ğ/g, 'g')
        .replace(/ı/g, 'i')
        .replace(/ö/g, 'o')
        .replace(/ş/g, 's')
        .replace(/ü/g, 'u')
        .replace(/\s+/g, ' ')
        .trim();

const pickDisplayName = (c) => {
  const typeRaw = c?.customerType ?? c?.musteriTipi ?? c?.type ?? c?.tip ?? '';
  const type = String(typeRaw).toUpperCase();
  if (type === 'GERCEK') {
    const ad = (c?.ad ?? c?.firstName ?? '').trim();
    const soyad = (c?.soyad ?? c?.lastName ?? '').trim();
    const full = `${ad} ${soyad}`.trim();
    return full || '-';
  }
  return (
      c?.sirketUnvani ??
      c?.unvan ??
      c?.companyName ??
      c?.name ??
      c?.adSoyad ??
      '-'
  );
};

const matchesNameSmart = (record, input) => {
  if (!input) return true;
  const tokens = normalizeTR(input).split(' ').filter(Boolean);
  if (tokens.length === 0) return true;

  const haystack = normalizeTR(
      `${pickDisplayName(record)} ${record?.sirketUnvani ?? record?.companyName ?? ''}`
  );

  return tokens.every((t) => haystack.includes(t));
};
/* ==== /yardımcılar ==== */

const pageStyles = {
  page: { padding: '30px' },
  headerContainer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
  header: { fontSize: '28px', fontWeight: 'bold', color: '#333', margin: 0 },
  buttonLink: { textDecoration: 'none', padding: '10px 20px', backgroundColor: '#c8102e', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px' },
  contentCard: { backgroundColor: 'white', padding: '24px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' },
  statsText: { paddingTop: '16px', fontSize: '14px', color: '#6c757d', fontWeight: '500' }
};

const filterStyles = {
  container: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px 16px', paddingBottom: '24px', borderBottom: '1px solid #e9ecef' },
  inputGroup: { display: 'flex', flexDirection: 'column', gap: '8px' },
  label: { fontSize: '14px', fontWeight: '500', color: '#333' },
  input: { height: '38px', padding: '0 12px', borderRadius: '5px', border: '1px solid #ced4da', fontSize: '14px', boxSizing: 'border-box' },
  buttonContainer: { gridColumn: '3 / 4', display: 'flex', justifyContent: 'flex-end', alignItems: 'flex-end' },
  button: { height: '38px', padding: '0 24px', borderRadius: '5px', border: 'none', cursor: 'pointer', backgroundColor: '#6c757d', color: 'white', fontSize: '14px', fontWeight: '500', whiteSpace: 'nowrap' }
};

export default function CustomerListPage() {
  const { permissions } = useAuth();
  const [clients, setClients] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const [filters, setFilters] = useState({
    name: '',
    email: '',
    clientCode: '',
    customerType: '',
    status: ''
  });

  const fetchClients = useCallback(async (currentFilters) => {
    setIsLoading(true);
    setError('');
    try {
      // İsim/şirket filtresini localde yapacağımız için API'ye göndermeyelim
      const { name, ...serverFilters } = currentFilters;

      const data = await getAllClients(serverFilters);
      const list = Array.isArray(data) ? data : [];

      const locallyFiltered = name
          ? list.filter((row) => matchesNameSmart(row, name))
          : list;

      setClients(locallyFiltered);
    } catch (err) {
      setError('Müşteri listesi yüklenirken hata oluştu: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const handler = setTimeout(() => { fetchClients(filters); }, 400);
    return () => clearTimeout(handler);
  }, [filters, fetchClients]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const clearFilters = () =>
      setFilters({ name: '', email: '', clientCode: '', customerType: '', status: '' });

  const handleDetail = (id) => navigate(`/dashboard/clients/${id}`);

  const canCreateClient = permissions.includes('client:create');

  return (
      <div style={pageStyles.page}>
        <div style={pageStyles.headerContainer}>
          <h1 style={pageStyles.header}>Müşteri Yönetimi</h1>
          {canCreateClient && (
              <Link to="/dashboard/clients/new" style={pageStyles.buttonLink}>
                + Yeni Müşteri Ekle
              </Link>
          )}
        </div>

        <div style={pageStyles.contentCard}>
          {/* Filtreler */}
          <div style={filterStyles.container}>
            <div style={filterStyles.inputGroup}>
              <label style={filterStyles.label} htmlFor="name">Ad / Şirket</label>
              <input style={filterStyles.input} type="text" id="name" name="name" value={filters.name} onChange={handleFilterChange} />
            </div>
            <div style={filterStyles.inputGroup}>
              <label style={filterStyles.label} htmlFor="email">E-posta</label>
              <input style={filterStyles.input} type="email" id="email" name="email" value={filters.email} onChange={handleFilterChange} />
            </div>
            <div style={filterStyles.inputGroup}>
              <label style={filterStyles.label} htmlFor="clientCode">Müşteri Kodu</label>
              <input style={filterStyles.input} type="text" id="clientCode" name="clientCode" value={filters.clientCode} onChange={handleFilterChange} />
            </div>
            <div style={filterStyles.inputGroup}>
              <label style={filterStyles.label} htmlFor="customerType">Müşteri Tipi</label>
              <select style={filterStyles.input} id="customerType" name="customerType" value={filters.customerType} onChange={handleFilterChange}>
                <option value="">Tüm Tipler</option>
                <option value="GERCEK">Bireysel</option>
                <option value="TUZEL">Kurumsal</option>
              </select>
            </div>
            <div style={filterStyles.inputGroup}>
              <label style={filterStyles.label} htmlFor="status">Durum</label>
              <select style={filterStyles.input} id="status" name="status" value={filters.status} onChange={handleFilterChange}>
                <option value="">Tüm Durumlar</option>
                <option value="Aktif">Aktif</option>
                <option value="Pasif">Pasif</option>
              </select>
            </div>
            <div style={filterStyles.buttonContainer}>
              <button style={filterStyles.button} onClick={clearFilters}>Filtreleri Temizle</button>
            </div>
          </div>

          <div style={pageStyles.statsText}>
            {isLoading ? '...' : `${clients.length} müşteri bulundu`}
          </div>

          {isLoading ? (
              <div style={{ textAlign: 'center', padding: '40px', color: '#6c757d' }}>
                <p>Müşteri listesi yükleniyor...</p>
              </div>
          ) : error ? (
              <div style={{ textAlign: 'center', padding: '40px', color: '#dc3545' }}>
                <p>{error}</p>
              </div>
          ) : (
              <div style={{ overflowX: 'auto', width: '100%' }}>
                <CustomerTable clients={clients} onDetail={handleDetail} />
              </div>
          )}
        </div>
      </div>
  );
}
