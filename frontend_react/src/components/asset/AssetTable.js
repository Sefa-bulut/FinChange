import React from "react";

const styles = {
    table: {
        width: '100%',
        borderCollapse: 'collapse',
        fontSize: '14px',
    },
    trBody: {
        borderBottom: '1px solid #dee2e6',
    },
    th: {
        backgroundColor: '#f8f9fa',
        borderBottom: '2px solid #dee2e6',
        padding: '12px 15px',
        textAlign: 'left',
        fontWeight: '600',
        color: '#495057',
    },
    thRight: {
        backgroundColor: '#f8f9fa',
        borderBottom: '2px solid #dee2e6',
        padding: '12px 15px',
        textAlign: 'right',
        fontWeight: '600',
        color: '#495057',
    },
    td: {
        padding: '12px 15px',
        textAlign: 'left',
        color: '#212529',
        verticalAlign: 'middle',
    },
    tdRight: {
        padding: '12px 15px',
        textAlign: 'right',
        color: '#212529',
        fontFamily: 'monospace',
        verticalAlign: 'middle',
    },
    noData: {
        textAlign: 'center',
        padding: '30px',
        fontSize: '16px',
        color: '#6c757d',
    },
    bistCode: {
        fontWeight: 'bold',
        fontFamily: 'monospace',
        fontSize: '15px',
    }
};

export default function AssetTable({ assets }) {
    if (!assets || assets.length === 0) {
        return <p style={styles.noData}>Gösterilecek varlık bulunamadı.</p>;
    }

    const formatNumber = (num) => {
        if (num === null || num === undefined) {
            return 'N/A';
        }
        return parseFloat(num).toLocaleString('tr-TR', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    };

    return (
        <table style={styles.table}>
            <thead>
            <tr>
                <th style={styles.th}>ISIN Kodu</th>
                <th style={styles.th}>BIST Kodu</th>
                <th style={styles.th}>Şirket Adı</th>
                <th style={styles.th}>Sektör</th>
                <th style={styles.th}>Para Birimi</th>
                <th style={styles.thRight}>Maks. Emir Değeri</th>
            </tr>
            </thead>
            <tbody>
            {assets.map(asset => (
                <tr key={asset.id} style={styles.trBody}>
                    <td style={styles.td}>{asset.isinCode}</td>
                    <td style={styles.td}>
                        <span style={styles.bistCode}>{asset.bistCode}</span>
                    </td>
                    <td style={styles.td}>{asset.companyName}</td>
                    <td style={styles.td}>{asset.sector || 'N/A'}</td>
                    <td style={styles.td}>{asset.currency}</td>
                    <td style={styles.tdRight}>
                        {formatNumber(asset.maxOrderValue)}
                    </td>
                </tr>
            ))}
            </tbody>
        </table>
    );
}