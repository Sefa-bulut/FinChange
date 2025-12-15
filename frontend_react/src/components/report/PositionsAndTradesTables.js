import React from 'react';

export default function PositionsAndTradesTables({ openPositions = [], tradeHistory = [], currency = 'TL' }) {

    return (
        <div style={{ marginTop: '2rem' }}>

            <section>
                <SectionHeader
                    title="Rapor Dönemindeki İşlemler (Özet)"
                    rightBadge={`Para Birimi: ${currency}`}
                />
                <SimpleTable
                    columns={[
                        { key: 'executionTime', header: 'Tarih/Saat', render: v => formatDate(v) },
                        { key: 'market',        header: 'Pazar' },
                        { key: 'side',          header: 'Yön' },
                        { key: 'symbol',        header: 'HS Kodu' },
                        { key: 'lot',           header: 'Lot Adedi', render: v => fmtNum(v, 0) },
                        { key: 'price',         header: 'Fiyat', render: v => fmtNum(v) },
                        { key: 'amount',        header: 'Tutar', render: v => fmtNum(v) },
                        { key: 'commission',    header: 'Komisyon', render: v => fmtNum(v) },
                    ]}
                    data={tradeHistory}
                />

            </section>
        </div>
    );
}

function SectionHeader({ title, rightBadge }) {
    return (
        <div style={{
            display: 'flex', justifyContent: 'space-between', alignItems: 'center',
            background: '#e9ecef', color: '#111827', padding: '8px 12px',
            borderRadius: '6px', marginBottom: '8px', fontWeight: 600
        }}>
            <div>{title}</div>
            {rightBadge ? (
                <span style={{
                    background: '#cfd2d6', padding: '3px 8px', borderRadius: '4px',
                    fontSize: 12, color: '#111827'
                }}>
          {rightBadge}
        </span>
            ) : null}
        </div>
    );
}

function SimpleTable({ columns, data }) {
    const thStyle = {
        padding: '10px',
        fontWeight: 'bold',
        textAlign: 'left',
        borderBottom: '2px solid #dee2e6',
        backgroundColor: '#f5f6f7'
    };
    const tdStyle = {
        padding: '10px',
        borderBottom: '1px solid #dee2e6',
        fontSize: '14px'
    };

    const tableData = Array.isArray(data) ? data : [];

    return (
        <div style={{ border: '1px solid #e5e7eb', borderRadius: '8px', overflow: 'hidden' }}>
            <table style={{ width: '100%', borderCollapse: 'separate', borderSpacing: 0 }}>
                <thead>
                <tr>
                    {columns.map((c) => (
                        <th key={c.key} style={thStyle}>{c.header}</th>
                    ))}
                </tr>
                </thead>
                <tbody>
                {tableData.map((row, i) => (
                    <tr key={i} style={{ backgroundColor: i % 2 === 0 ? '#ffffff' : '#f8f9fa' }}>
                        {columns.map((c) => {
                            const raw = row?.[c.key];
                            const value = c.render ? c.render(raw, row) : raw;
                            const rightAlign = ['amount', 'price', 'lot', 'commission'].includes(c.key);
                            return (
                                <td key={c.key} style={{ ...tdStyle, textAlign: rightAlign ? 'right' : 'left' }}>
                                    {value ?? '—'}
                                </td>
                            );
                        })}
                    </tr>
                ))}
                {tableData.length === 0 && (
                    <tr>
                        <td colSpan={columns.length} style={{ ...tdStyle, textAlign: 'center', color: '#6b7280' }}>
                            Kayıt yok
                        </td>
                    </tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

function fmtNum(n, minFrac = 2) {
    if (n === null || n === undefined || n === '' || isNaN(Number(n))) return '—';
    try {
        return Number(n).toLocaleString('tr-TR', { minimumFractionDigits: minFrac, maximumFractionDigits: 2 });
    } catch {
        return String(n);
    }
}

function formatDate(v) {
    if (!v) return '—';
    const d = new Date(v);
    if (isNaN(d.getTime())) return String(v);
    const pad = (x) => String(x).padStart(2, '0');
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}-${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}