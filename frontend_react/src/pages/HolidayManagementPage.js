import React, { useState, useEffect } from 'react';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import holidayService from '../services/holidayService';
import { toast } from 'react-toastify'; // toast'ı import ediyoruz

// --- BÖLÜM 1: STİLLER ---
const styles = {
    container: { padding: '2rem', maxWidth: '900px', margin: '0 auto', fontFamily: 'sans-serif' },
    fieldset: { border: '1px solid #ddd', borderRadius: '8px', padding: '1rem 1.5rem 1.5rem 1.5rem', marginBottom: '2rem' },
    legend: { padding: '0 0.5em', fontWeight: 'bold', color: '#333' },
    compactForm: { display: 'flex', alignItems: 'flex-end', gap: '1rem', flexWrap: 'wrap' },
    compactFormGroup: { display: 'flex', flexDirection: 'column', flex: '1 1 180px' },
    label: { marginBottom: '0.5rem', fontWeight: 'bold', fontSize: '0.9rem', color: '#333' },
    input: { padding: '0.6rem', border: '1px solid #ccc', borderRadius: '4px', fontSize: '0.9rem' },
    compactSubmitButton: { backgroundColor: '#007bff', color: 'white', border: 'none', padding: '0.6rem 1.5rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.9rem', height: 'calc(0.6rem * 2 + 18px)' },
    listContainer: { border: '1px solid #e0e0e0', borderRadius: '8px', overflow: 'hidden' },
    listHeader: { display: 'flex', padding: '0.75rem 1rem', backgroundColor: '#f5f5f5', fontWeight: 'bold', fontSize: '0.9rem', borderBottom: '1px solid #e0e0e0' },
    list: { listStyleType: 'none', padding: 0, margin: 0 },
    listItem: { display: 'flex', alignItems: 'center', padding: '0.75rem 1rem', borderBottom: '1px solid #f0f0f0', transition: 'background-color 0.2s' },
    cell: { fontSize: '0.9rem', color: '#555', paddingRight: '1rem' },
    dateCell: { flex: '0 0 180px' },
    descriptionCell: { flex: '1 1 0', fontWeight: '500', color: '#222' },
    typeCell: { flex: '0 0 150px' },
    actionsCell: { flex: '0 0 50px', textAlign: 'right' },
    deleteButton: { backgroundColor: 'transparent', color: '#dc3545', border: 'none', padding: '0.25rem', borderRadius: '4px', cursor: 'pointer', fontSize: '1.2rem', lineHeight: '1' },
    paginationContainer: { display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '1rem 0' },
    pageButton: { border: '1px solid #ddd', padding: '0.5rem 0.75rem', margin: '0 0.25rem', backgroundColor: 'white', cursor: 'pointer', borderRadius: '4px' },
    activePageButton: { backgroundColor: '#007bff', color: 'white', border: '1px solid #007bff' },
    disabledPageButton: { color: '#aaa', cursor: 'not-allowed' },
    errorMessage: { color: '#dc3545', fontWeight: 'bold' },
    placeholderItem: {
        display: 'flex',
        alignItems: 'center',
        padding: '0.75rem 1rem',
        borderBottom: '1px solid #f0f0f0',
        height: '49px',
        boxSizing: 'border-box'
    }
};

const hoverStyles = `
    .submit-btn:hover { background-color: #0056b3 !important; }
    .delete-btn-hover:hover { background-color: #fce8e6 !important; }
    .list-item-hover:hover { background-color: #fafafa !important; }
`;

// --- BÖLÜM 2: YARDIMCI BİLEŞEN VE VERİLER ---
const holidayTypeOptions = [
    { value: 'RESMI_TATIL', label: 'Resmi Tatil' },
    { value: 'DINI_BAYRAM', label: 'Dini Bayram' },
    { value: 'BORSA_OZEL',  label: 'Borsaya Özel Tatil' },
    { value: 'DIGER',       label: 'Diğer' }
];

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
    const pageNumbers = Array.from({ length: totalPages }, (_, i) => i + 1);
    return (
        <nav style={styles.paginationContainer}>
            <button style={{...styles.pageButton, ...(currentPage === 1 ? styles.disabledPageButton : {})}} onClick={() => onPageChange(currentPage - 1)} disabled={currentPage === 1}>&laquo; Geri</button>
            {pageNumbers.map(number => <button key={number} onClick={() => onPageChange(number)} style={{...styles.pageButton, ...(currentPage === number ? styles.activePageButton : {})}}>{number}</button>)}
            <button style={{...styles.pageButton, ...(currentPage === totalPages ? styles.disabledPageButton : {})}} onClick={() => onPageChange(currentPage + 1)} disabled={currentPage === totalPages}>İleri &raquo;</button>
        </nav>
    );
};

// --- BÖLÜM 3: ANA BİLEŞEN ---
const HolidayManagementPage = () => {
    const [holidays, setHolidays] = useState([]);
    const [selectedDate, setSelectedDate] = useState(new Date());
    const [description, setDescription] = useState('');
    const [holidayType, setHolidayType] = useState(holidayTypeOptions[0].value);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentPage, setCurrentPage] = useState(1);
    const ITEMS_PER_PAGE = 7;

    useEffect(() => {
        const fetchHolidays = async () => {
            try {
                setIsLoading(true);
                const data = await holidayService.getAll();
                if (Array.isArray(data)) {
                    setHolidays(data.sort((a, b) => new Date(b.holidayDate) - new Date(a.holidayDate)));
                }
                setError(null);
            } catch (err) { 
                setError("Tatil verileri yüklenemedi.");
                toast.error(err.message || "Tatil verileri yüklenemedi.");
            } finally { 
                setIsLoading(false); 
            }
        };
        fetchHolidays();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        const newHolidayData = { holidayDate: selectedDate.toISOString().split('T')[0], description, type: holidayType };
        try {
            const createdHoliday = await holidayService.create(newHolidayData);
            if (createdHoliday) {
                const updatedHolidays = [createdHoliday, ...holidays].sort((a, b) => new Date(b.holidayDate) - new Date(a.holidayDate));
                setHolidays(updatedHolidays);
                setDescription('');
                setCurrentPage(1);
                // === DEĞİŞİKLİK BURADA: Başarı bildirimi ekleniyor ===
                toast.success("Tatil başarıyla eklendi!");
            }
        } catch (err) { 
            toast.error(err.message || 'Tatil eklenemedi.'); 
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm('Bu tatil kaydını silmek istediğinizden emin misiniz?')) {
            try {
                const isSuccess = await holidayService.delete(id);
                if (isSuccess) {
                    const remainingHolidays = holidays.filter(h => h.id !== id);
                    setHolidays(remainingHolidays);
                    toast.success("Kayıt başarıyla silindi.");
                    if (Math.ceil(remainingHolidays.length / ITEMS_PER_PAGE) < currentPage) {
                        setCurrentPage(prev => prev > 1 ? prev - 1 : 1);
                    }
                } else {
                     throw new Error("Sunucudan silme onayı alınamadı.");
                }
            } catch (err) { 
                toast.error(err.message || 'Kayıt silinemedi.'); 
            }
        }
    };
    
    // Sayfalama ve yer tutucu mantığı (değişiklik yok)
    const totalPages = Math.ceil(holidays.length / ITEMS_PER_PAGE);
    const indexOfLastItem = currentPage * ITEMS_PER_PAGE;
    const indexOfFirstItem = indexOfLastItem - ITEMS_PER_PAGE;
    const currentHolidays = holidays.slice(indexOfFirstItem, indexOfLastItem);
    const placeholdersCount = holidays.length > 0 ? ITEMS_PER_PAGE - currentHolidays.length : 0;

    return (
        <div style={styles.container}>
            <style>{hoverStyles}</style>
            <h1>Takvim ve Tatil Yönetimi</h1>

            <form onSubmit={handleSubmit}>
                <fieldset style={styles.fieldset}>
                    <legend style={styles.legend}>Yeni Tatil Ekle</legend>
                    <div style={styles.compactForm}>
                        <div style={styles.compactFormGroup}><label style={styles.label}>Tarih</label><DatePicker selected={selectedDate} onChange={(date) => setSelectedDate(date)} dateFormat="dd/MM/yyyy" customInput={<input style={styles.input} />} /></div>
                        <div style={styles.compactFormGroup}><label htmlFor="description" style={styles.label}>Açıklama</label><input type="text" id="description" value={description} onChange={(e) => setDescription(e.target.value)} required style={styles.input} /></div>
                        <div style={styles.compactFormGroup}><label htmlFor="type" style={styles.label}>Tür</label><select id="type" value={holidayType} onChange={(e) => setHolidayType(e.target.value)} style={styles.input}>{holidayTypeOptions.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}</select></div>
                        <button type="submit" style={styles.compactSubmitButton} className="submit-btn">Ekle</button>
                    </div>
                </fieldset>
            </form>

            <div style={styles.listContainer}>
                <div style={styles.listHeader}>
                    <span style={{...styles.cell, ...styles.dateCell}}>Tarih</span>
                    <span style={{...styles.cell, ...styles.descriptionCell}}>Açıklama</span>
                    <span style={{...styles.cell, ...styles.typeCell}}>Tür</span>
                    <span style={{...styles.cell, ...styles.actionsCell, paddingRight: 0}}>Sil</span>
                </div>

                {isLoading && <p style={{ padding: '1rem' }}>Yükleniyor...</p>}
                {error && <p style={{...styles.errorMessage, padding: '1rem'}}>{error}</p>}
                
                <ul style={styles.list}>
                    {currentHolidays.map(holiday => (
                        <li key={holiday.id} style={styles.listItem} className="list-item-hover">
                            <span style={{...styles.cell, ...styles.dateCell}}>{new Date(holiday.holidayDate).toLocaleDateString('tr-TR', { day: '2-digit', month: 'long', year: 'numeric' })}</span>
                            <span style={{...styles.cell, ...styles.descriptionCell}}>{holiday.description}</span>
                            <span style={{...styles.cell, ...styles.typeCell}}>{holidayTypeOptions.find(opt => opt.value === holiday.type)?.label || holiday.type}</span>
                            <div style={{...styles.cell, ...styles.actionsCell, paddingRight: 0}}><button onClick={() => handleDelete(holiday.id)} style={styles.deleteButton} className="delete-btn-hover" title="Sil">&#x1F5D1;</button></div>
                        </li>
                    ))}
                    
                    {placeholdersCount > 0 && Array.from({ length: placeholdersCount }).map((_, index) => (
                        <li key={`placeholder-${index}`} style={styles.placeholderItem}>&nbsp;</li>
                    ))}
                </ul>
            </div>
            
            {totalPages > 1 && (
                <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={setCurrentPage} />
            )}
        </div>
    );
};

export default HolidayManagementPage;