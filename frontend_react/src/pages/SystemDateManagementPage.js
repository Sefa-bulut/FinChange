import React, { useState, useEffect, useCallback } from 'react'; // <-- DÜZELTME BURADA: useCallback eklendi.
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import systemDateService from '../services/systemDateService';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

// Sayfa içinde kullanılacak stiller (değişiklik yok)
const styles = {
    container: { padding: '2rem', maxWidth: '900px', margin: '0 auto', fontFamily: 'sans-serif' },
    fieldset: { border: '1px solid #ddd', borderRadius: '8px', padding: '1.5rem', marginBottom: '2rem' },
    legend: { padding: '0 0.5em', fontWeight: 'bold' },
    form: { display: 'flex', alignItems: 'flex-end', gap: '1rem', flexWrap: 'wrap' },
    formGroup: { display: 'flex', flexDirection: 'column', flex: 1 },
    label: { marginBottom: '0.5rem', fontWeight: 'bold', fontSize: '0.9rem' },
    input: { padding: '0.7rem', border: '1px solid #ccc', borderRadius: '4px', fontSize: '0.9rem', width: '100%', boxSizing: 'border-box' },
    button: { padding: '0.7rem 1.5rem', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.9rem', color: 'white' },
    updateButton: { backgroundColor: '#007bff' },
    cancelButton: { backgroundColor: '#6c757d' },
    modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
    modalContent: { background: 'white', padding: '2rem', borderRadius: '8px', width: '400px', textAlign: 'center' },
    modalButtons: { marginTop: '1.5rem', display: 'flex', justifyContent: 'center', gap: '1rem' },
    errorMessage: { color: 'red', marginTop: '1rem' },
    currentDateInfo: {
        backgroundColor: '#f8f9fa',
        borderLeft: '5px solid #007bff',
        borderRadius: '4px',
        padding: '1rem 1.5rem',
        marginBottom: '2rem',
    },
    infoTextContainer: {
        display: 'flex',
        flexDirection: 'column',
    },
    infoTitle: {
        fontSize: '0.9rem',
        color: '#6c757d',
        fontWeight: 'bold',
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
        marginBottom: '0.25rem',
    },
    infoDateTime: {
        fontSize: '1.2rem',
        color: '#212529',
        fontWeight: '500',
    }
};

// Modal Component (değişiklik yok)
const ConfirmationModal = ({ isOpen, onClose, onConfirm, title, children }) => {
    if (!isOpen) return null;
    return (
        <div style={styles.modalOverlay} onClick={onClose}>
            <div style={styles.modalContent} onClick={e => e.stopPropagation()}>
                <h3>{title}</h3>
                <p>{children}</p>
                <div style={styles.modalButtons}>
                    <button style={{...styles.button, backgroundColor: '#dc3545'}} onClick={onConfirm}>Evet</button>
                    <button style={{...styles.button, ...styles.cancelButton}} onClick={onClose}>Hayır</button>
                </div>
            </div>
        </div>
    );
};

const SystemDateManagementPage = () => {
    const navigate = useNavigate();
    const [initialDateTime, setInitialDateTime] = useState(null);
    const [selectedDateTime, setSelectedDateTime] = useState(new Date());
    const [description, setDescription] = useState('');
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [isConfirmModalOpen, setConfirmModalOpen] = useState(false);

    const fetchCurrentTime = useCallback(async () => {
        try {
            setIsLoading(true);
            const timeString = await systemDateService.getTime();
            if (timeString) {
                const dateObject = new Date(timeString);
                setInitialDateTime(dateObject);
                setSelectedDateTime(dateObject);
                setError('');
            } else {
                 throw new Error("Sunucudan geçerli bir tarih verisi alınamadı.");
            }
        } catch (err) {
            setError("Sistem tarihi alınamadı. Lütfen daha sonra tekrar deneyin.");
            toast.error(err.message || "Sistem tarihi alınamadı.");
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchCurrentTime();
    }, [fetchCurrentTime]);
    
    const isFormDirty = () => {
        if (!initialDateTime) return false;
        return (description.trim() !== '') || (initialDateTime.getTime() !== selectedDateTime.getTime());
    };

    const handleCancel = () => {
        if (isFormDirty() && !window.confirm("Girilen bilgiler silinecektir. Çıkış işlemi gerçekleşsin mi?")) { return; }
        navigate('/dashboard');
    };

    const handleUpdateClick = (e) => {
        e.preventDefault();
        setError('');
        setConfirmModalOpen(true);
    };

    const handleConfirmUpdate = async () => {
        setConfirmModalOpen(false);
        setIsLoading(true);
        const isoString = selectedDateTime.toISOString().slice(0, 19);
        const updateData = { systemTime: isoString, description: description.trim() };
        
        try {
            const isSuccess = await systemDateService.update(updateData);

            if (isSuccess) {
                toast.success("Sistem tarihi başarıyla güncellendi.");
                fetchCurrentTime(); 
                setDescription('');
            } else {
                throw new Error("Sunucudan güncelleme onayı alınamadı.");
            }
        } catch (err) {
            setError(err.message || "Sistem tarihi güncellenirken bir hata oluştu.");
            toast.error(err.message || "Güncelleme başarısız oldu.");
        } finally {
            setIsLoading(false);
        }
    };

    if (isLoading && !initialDateTime) {
        return <p style={{textAlign: 'center', padding: '2rem'}}>Sistem tarihi yükleniyor...</p>;
    }

    return (
        <div style={styles.container}>
            <ConfirmationModal
                isOpen={isConfirmModalOpen}
                onClose={() => setConfirmModalOpen(false)}
                onConfirm={handleConfirmUpdate}
                title="Sistem Tarihini Güncelleme Onayı"
            >
                Sistem tarihinin değiştirilmesi; takas, değerleme ve emir işleme akışlarını etkileyebilir. Emin misiniz?
            </ConfirmationModal>

            <h1>Sistem Tarihi Yönetimi</h1>

            {!isLoading && initialDateTime && (
                <div style={styles.currentDateInfo}>
                    <div style={styles.infoTextContainer}>
                        <span style={styles.infoTitle}>Mevcut Sistem Tarihi</span>
                        <span style={styles.infoDateTime}>
                            {initialDateTime.toLocaleDateString('tr-TR', { day: '2-digit', month: 'long', year: 'numeric' })}
                            {' - '}
                            {initialDateTime.toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}
                        </span>
                    </div>
                </div>
            )}
            
            <form onSubmit={handleUpdateClick}>
                <fieldset style={styles.fieldset}>
                    <legend style={styles.legend}>Yeni Sistem Tarihi Belirle</legend>
                    <div style={styles.form}>
                        <div style={styles.formGroup}>
                            <label style={styles.label}>Tarih ve Saat Seçimi</label>
                            <DatePicker
                                selected={selectedDateTime}
                                onChange={date => setSelectedDateTime(date)}
                                showTimeSelect
                                timeFormat="HH:mm"
                                timeIntervals={15}
                                dateFormat="dd.MM.yyyy HH:mm"
                                customInput={<input style={styles.input} />}
                            />
                        </div>
                        <div style={{...styles.formGroup, flex: 2}}>
                            <label style={styles.label}>Açıklama (İsteğe Bağlı)</label>
                            <input
                                type="text"
                                value={description}
                                onChange={e => setDescription(e.target.value)}
                                placeholder="Örn: Yıl sonu testi için"
                                style={styles.input}
                            />
                        </div>
                    </div>
                </fieldset>

                <div style={{display: 'flex', gap: '1rem'}}>
                    <button type="submit" style={{...styles.button, ...styles.updateButton}} disabled={isLoading || !isFormDirty()}>
                        {isLoading ? 'Güncelleniyor...' : 'Sistem Tarihini Güncelle'}
                    </button>
                    <button type="button" onClick={handleCancel} style={{...styles.button, ...styles.cancelButton}}>
                        İptal
                    </button>
                </div>
            </form>

            {error && <p style={styles.errorMessage}>{error}</p>}

        </div>
    );
};

export default SystemDateManagementPage;