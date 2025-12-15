import React from 'react';

const filterStyles = {
    panel: { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '20px', marginBottom: '30px', padding: '24px', backgroundColor: '#f8f9fa', borderRadius: '12px', border: '1px solid #e9ecef', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' },
    inputGroup: { display: 'flex', flexDirection: 'column', gap: '6px' },
    label: { fontSize: '13px', color: '#495057', fontWeight: '600', textTransform: 'uppercase', letterSpacing: '0.5px' },
    input: { padding: '10px 12px', borderRadius: '6px', border: '1px solid #ced4da', fontSize: '14px', backgroundColor: 'white' },
    select: { padding: '10px 12px', borderRadius: '6px', border: '1px solid #ced4da', fontSize: '14px', backgroundColor: 'white', cursor: 'pointer' },
    buttonGroup: { display: 'flex', gap: '10px', alignItems: 'flex-end', gridColumn: '1 / -1' },
    clearButton: { padding: '10px 16px', borderRadius: '6px', border: 'none', cursor: 'pointer', backgroundColor: '#6c757d', color: 'white', fontSize: '14px', fontWeight: '500' }
};

const OrderFilterPanel = ({ filters, onFilterChange, onClearFilters }) => {

    // Input'lardan gelen değişiklikleri doğrudan ana bileşene iletir.
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        onFilterChange(name, value);
    };

    return (
        <div style={filterStyles.panel}>
            <div style={filterStyles.inputGroup}>
                <label style={filterStyles.label} htmlFor="status">Durum</label>
                <select style={filterStyles.select} id="status" name="status" value={filters.status} onChange={handleInputChange}>
                    <option value="">Tümü</option>
                    <option value="ACTIVE">Aktif</option>
                    <option value="PARTIALLY_FILLED">Kısmi Gerçekleşti</option>
                    <option value="FILLED">Gerçekleşti</option>
                    <option value="CANCELLED">İptal Edildi</option>
                </select>
            </div>
            <div style={filterStyles.inputGroup}>
                <label style={filterStyles.label} htmlFor="startDate">Başlangıç Tarihi</label>
                <input style={filterStyles.input} type="date" id="startDate" name="startDate" value={filters.startDate} onChange={handleInputChange} />
            </div>
            <div style={filterStyles.inputGroup}>
                <label style={filterStyles.label} htmlFor="endDate">Bitiş Tarihi</label>
                <input style={filterStyles.input} type="date" id="endDate" name="endDate" value={filters.endDate} onChange={handleInputChange} />
            </div>
            <div style={filterStyles.buttonGroup}>
                <button style={filterStyles.clearButton} onClick={onClearFilters} title="Tüm filtreleri temizle">Filtreleri Temizle</button>
            </div>
        </div>
    );
};

export default OrderFilterPanel;