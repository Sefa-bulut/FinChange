import React from 'react';
import { Link } from 'react-router-dom'; 
import { FaPen } from 'react-icons/fa';

const styles = {
    table: { width: '100%', borderCollapse: 'collapse', fontSize: '14px' },
    th: { 
        background: '#f8f9fa', 
        padding: '12px', 
        borderBottom: '2px solid #dee2e6', 
        textAlign: 'left', 
        fontWeight: '600',
        color: '#495057' 
    },
    td: { 
        padding: '12px', 
        borderBottom: '1px solid #e9ecef' 
    },
    statusBadge: { 
        padding: '4px 8px', 
        borderRadius: '12px', 
        fontSize: '12px', 
        fontWeight: '500' 
    },
    activeStatus: { 
        backgroundColor: '#dcfce7', 
        color: '#166534' 
    },
    inactiveStatus: { 
        backgroundColor: '#fee2e2', 
        color: '#991b1b' 
    },
    actionLink: { 
        textDecoration: 'none', 
        color: '#3b82f6',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '6px',
        borderRadius: '50%',
        transition: 'background-color 0.2s'
    },
    actionLinkHover: {
        backgroundColor: '#e0f2fe'
    }
};

export default function UserTable({ users }) {
    if (!users || users.length === 0) {
        return <p style={{ textAlign: 'center', padding: '20px', color: '#6b7280' }}>
            Gösterilecek personel bulunamadı.
        </p>;
    }

    return (
        <table style={styles.table}>
            <thead>
                <tr>
                    <th style={styles.th}>Ad Soyad</th>
                    <th style={styles.th}>E-posta</th>
                    <th style={styles.th}>Personel Kodu</th>
                    <th style={styles.th}>Roller</th>
                    <th style={styles.th}>Durum</th>
                    <th style={styles.th}>Eylemler</th>
                </tr>
            </thead>
            <tbody>
                {users.map((user) => (
                    <tr key={user.id}>
                        <td style={styles.td}>{user.firstName} {user.lastName}</td>
                        <td style={styles.td}>{user.email}</td>
                        <td style={styles.td}>{user.personnelCode}</td>
                        <td style={styles.td}>{user.roles?.join(', ') || 'Rol yok'}</td>
                        <td style={styles.td}>
                            <span style={{...styles.statusBadge, ...(user.isActive ? styles.activeStatus : styles.inactiveStatus)}}>
                                {user.isActive ? 'Aktif' : 'Pasif'}
                            </span>
                        </td>
                        <td style={styles.td}>
                            <Link 
                                to={`/dashboard/users/${user.id}`} 
                                style={styles.actionLink}
                                title="Personeli Düzenle"
                                onMouseEnter={e => e.currentTarget.style.backgroundColor = styles.actionLinkHover.backgroundColor}
                                onMouseLeave={e => e.currentTarget.style.backgroundColor = 'transparent'}
                            >
                                <FaPen />
                            </Link>
                        </td>
                    </tr>
                ))}
            </tbody>
        </table>
    );
}