import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { getAllFirms, activateFirm } from '../services/brokerageService';
import ConfirmationModal from '../components/common/Modal';

const styles = {
    page: { padding: '30px' },
    headerContainer: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' },
    header: { fontSize: '28px', fontWeight: 'bold', color: '#333', margin: 0 },
    newButton: { backgroundColor: '#28a745', color: 'white', padding: '10px 20px', borderRadius: '5px', border: 'none', cursor: 'pointer', fontWeight: 'bold' },
    table: { width: '100%', borderCollapse: 'collapse', backgroundColor: 'white', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' },
    th: { backgroundColor: '#f8f9fa', padding: '12px', borderBottom: '2px solid #dee2e6', textAlign: 'left' },
    td: { padding: '12px', borderBottom: '1px solid #e9ecef', verticalAlign: 'middle' },
    actionButton: { padding: '6px 12px', fontSize: '13px', border: 'none', borderRadius: '5px', cursor: 'pointer', color: 'white', marginRight: '5px' },
    editButton: { backgroundColor: '#007bff' },
    activateButton: { backgroundColor: '#17a2b8' },
    disabledButton: { backgroundColor: '#6c757d', cursor: 'not-allowed' },
    statusPill: { padding: '4px 12px', borderRadius: '12px', fontSize: '12px', fontWeight: '600' },
    activePill: { backgroundColor: '#d4edda', color: '#155724' },
    inactivePill: { backgroundColor: '#f8d7da', color: '#721c24' },
};

export default function BrokerageFirmListPage() {
    const [firms, setFirms] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [firmToActivate, setFirmToActivate] = useState(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const navigate = useNavigate();

    const fetchFirms = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await getAllFirms();
            setFirms(data);
        } catch (error) {
            toast.error(error.message || 'Aracı kurumlar getirilirken bir hata oluştu.');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchFirms();
    }, [fetchFirms]);

    const handleActivateClick = (firm) => {
        setFirmToActivate(firm);
        setIsModalOpen(true);
    };

    const handleConfirmActivate = async () => {
        if (!firmToActivate) return;
        try {
            await activateFirm(firmToActivate.id);
            toast.success(`'${firmToActivate.kurumAdi}' başarıyla aktif edildi.`);
            fetchFirms();
        } catch (error) {
            toast.error(error.message || 'Kurum aktif edilirken bir hata oluştu.');
        } finally {
            setIsModalOpen(false);
            setFirmToActivate(null);
        }
    };

    if (isLoading) {
        return <div style={styles.page}>Yükleniyor...</div>;
    }

    return (
        <div style={styles.page}>
            <div style={styles.headerContainer}>
                <h1 style={styles.header}>Aracı Kurum Yönetimi</h1>
                <button style={styles.newButton} onClick={() => navigate('/dashboard/brokerage-firms/new')}>
                    + Yeni Kurum Ekle
                </button>
            </div>

            <table style={styles.table}>
                <thead>
                    <tr>
                        <th style={styles.th}>Kurum Adı</th>
                        <th style={styles.th}>Kurum Kodu</th>
                        <th style={styles.th}>Komisyon (%)</th>
                        <th style={styles.th}>Durum</th>
                        <th style={styles.th}>Eylemler</th>
                    </tr>
                </thead>
                <tbody>
                    {firms.map(firm => (
                        <tr key={firm.id}>
                            <td style={styles.td}>{firm.kurumAdi}</td>
                            <td style={styles.td}>{firm.kurumKodu}</td>
                            <td style={styles.td}>{firm.commissionRatePercent.toFixed(4)}</td>
                            <td style={styles.td}>
                                <span style={{...styles.statusPill, ...(firm.status === 'ACTIVE' ? styles.activePill : styles.inactivePill)}}>
                                    {firm.status === 'ACTIVE' ? 'Aktif' : 'Pasif'}
                                </span>
                            </td>
                            <td style={styles.td}>
                                <button 
                                    style={{...styles.actionButton, ...styles.editButton}}
                                    onClick={() => navigate(`/dashboard/brokerage-firms/${firm.id}`)}
                                >
                                    Düzenle
                                </button>
                                <button 
                                    style={{...styles.actionButton, ...(firm.status === 'ACTIVE' ? styles.disabledButton : styles.activateButton)}}
                                    onClick={() => handleActivateClick(firm)}
                                    disabled={firm.status === 'ACTIVE'}
                                >
                                    Aktif Et
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <ConfirmationModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onConfirm={handleConfirmActivate}
                title="Kurum Aktivasyon Onayı"
            >
                <p>
                    <strong>{firmToActivate?.kurumAdi}</strong> kurumunu aktif etmek üzeresiniz.
                </p>
                <p>
                    Bu işlem, mevcut aktif kurumu (varsa) pasif hale getirecektir. Devam etmek istiyor musunuz?
                </p>
            </ConfirmationModal>
        </div>
    );
}
