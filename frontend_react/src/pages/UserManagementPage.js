import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getUsers } from '../services/userService';
import UserTable from '../components/user/UserTable';
import { toast } from 'react-toastify';
const normalizeTR = (s = '') =>
    String(s)
        .toLocaleLowerCase('tr')
        .normalize('NFKD')
        .replace(/[\u0300-\u036f]/g, '') // combining marks
        .replace(/ı/g, 'i')              // dotless i
        .replace(/\s+/g, ' ')
        .trim();

const matchesNameSmart = (u = {}, input = '') => {
    const q = normalizeTR(input);
    if (!q) return true;

    const ad    = normalizeTR(u.ad ?? u.firstName ?? u.first_name ?? '');
    const soyad = normalizeTR(u.soyad ?? u.lastName  ?? u.last_name  ?? '');
    const full  = `${ad} ${soyad}`.trim();

    const tokens = q.split(' ').filter(Boolean);

    if (tokens.length === 1) {
        const t = tokens[0];
        return ad.includes(t) || soyad.includes(t) || full.includes(t);
    }
    return tokens.every((t) => ad.includes(t) || soyad.includes(t) || full.includes(t));
};



const styles = {
    page: { padding: '30px', backgroundColor: '#f8f9fa' },
    headerContainer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' },
    header: { fontSize: '28px', fontWeight: 'bold', color: '#333', margin: 0 },
    buttonLink: { textDecoration: 'none', padding: '10px 20px', backgroundColor: '#c8102e', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontSize: '16px' },
    content: { backgroundColor: 'white', padding: '20px', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }
};

const filterStyles = {
    panel: {
        display: 'flex',
        gap: '20px',
        marginBottom: '20px',
        padding: '20px',
        backgroundColor: '#fff',
        borderRadius: '8px',
        alignItems: 'flex-end',
        border: '1px solid #e9ecef'
    },
    inputGroup: { display: 'flex', flexDirection: 'column' },
    label: { marginBottom: '5px', fontSize: '14px', color: '#555', fontWeight: '600' },
    input: { padding: '8px', borderRadius: '4px', border: '1px solid #ccc' },
    button: { padding: '8px 15px', borderRadius: '4px', border: 'none', cursor: 'pointer', backgroundColor: '#6c757d', color: 'white' }
};

export default function UserManagementPage() {
    const { permissions } = useAuth();
    const [users, setUsers] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    const [filters, setFilters] = useState({
        name: '',
        email: '',
        personnelCode: '',
        isActive: '',
    });

    const fetchUsers = useCallback(async (currentFilters) => {
        setIsLoading(true);
        setError('');
        try {
            const { name, ...serverFilters } = currentFilters ?? {};
            const response = await getUsers(serverFilters);

            if (response?.isSuccess && Array.isArray(response.result)) {
                const base = response.result;
                const q = (name ?? '').trim();
                const filtered = q ? base.filter((u) => matchesNameSmart(u, q)) : base;
                setUsers(filtered);
            } else {
                setUsers([]);
                toast.warn(response?.message || 'Personel listesi alınamadı ama sunucudan hata dönmedi.');
            }
        } catch (err) {
            const msg = err?.message || 'Personel listesi yüklenemedi.';
            setError(msg);
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    }, []);


    useEffect(() => {
        const handler = setTimeout(() => {
            fetchUsers(filters);
        }, 500);
        return () => clearTimeout(handler);
    }, [fetchUsers, filters]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prevFilters => ({
            ...prevFilters,
            [name]: value,
        }));
    };

    const clearFilters = () => {
        setFilters({
            name: '',
            email: '',
            personnelCode: '',
            isActive: '',
        });
    };

    const canCreateUser = permissions.includes('user:create');

    return (
        <div style={styles.page}>
            <div style={styles.headerContainer}>
                <h1 style={styles.header}>Personel Yönetimi</h1>
                {canCreateUser && (
                    <Link to="/dashboard/users/new" style={styles.buttonLink}>
                        + Yeni Personel Davet Et
                    </Link>
                )}
            </div>
            
            <div style={filterStyles.panel}>
                <div style={filterStyles.inputGroup}>
                    <label style={filterStyles.label} htmlFor="name">Ad / Soyad</label>
                    <input style={filterStyles.input} type="text" id="name" name="name" value={filters.name} onChange={handleFilterChange} />
                </div>
                <div style={filterStyles.inputGroup}>
                    <label style={filterStyles.label} htmlFor="email">E-posta</label>
                    <input style={filterStyles.input} type="email" id="email" name="email" value={filters.email} onChange={handleFilterChange} />
                </div>
                <div style={filterStyles.inputGroup}>
                    <label style={filterStyles.label} htmlFor="personnelCode">Personel Kodu</label>
                    <input style={filterStyles.input} type="text" id="personnelCode" name="personnelCode" value={filters.personnelCode} onChange={handleFilterChange} />
                </div>
                <div style={filterStyles.inputGroup}>
                    <label style={filterStyles.label} htmlFor="isActive">Durum</label>
                    <select style={filterStyles.input} id="isActive" name="isActive" value={filters.isActive} onChange={handleFilterChange}>
                        <option value="">Tümü</option>
                        <option value="true">Aktif</option>
                        <option value="false">Pasif</option>
                    </select>
                </div>
                <div style={filterStyles.inputGroup}>
                    <button style={filterStyles.button} onClick={clearFilters}>Filtreleri Temizle</button>
                </div>
            </div>

            <div style={styles.content}>
                {isLoading && <p>Yükleniyor...</p>}
                {error && <p style={{ color: 'red' }}>{error}</p>}
                {!isLoading && !error && <UserTable users={users} />}
            </div>
        </div>
    );
}