import React from 'react';

/** === Sütun genişlikleri (px) === */
const COL = {
  code: 160,
  name: 260,
  email: 280,
  phone: 170,
  type: 120,
  status: 110,
  action: 140,
};

const styles = {
  tableWrap: {
    width: '100%',
    overflowX: 'auto', // dar ekranda yatay scroll
  },
  table: {
    width: '100%',
    minWidth: '1100px', // kolonların sıkışıp kaybolmasını engeller
    borderCollapse: 'collapse',
    backgroundColor: 'white',
    borderRadius: '8px',
    overflow: 'hidden',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    tableLayout: 'fixed', // sabit kolonlar + ellipsis
  },
  header: {
    backgroundColor: '#f8f9fa',
    borderBottom: '2px solid #dee2e6',
  },
  headerCell: {
    padding: '12px 16px',
    textAlign: 'left',
    fontWeight: 600,
    color: '#495057',
    fontSize: '14px',
    borderBottom: '1px solid #dee2e6',
    whiteSpace: 'nowrap',
  },
  row: {
    borderBottom: '1px solid #dee2e6',
    transition: 'background-color 0.2s',
  },
  rowHover: { backgroundColor: '#f8f9fa' },
  cell: {
    padding: '12px 16px',
    fontSize: '14px',
    color: '#495057',
    overflow: 'hidden', // ellipsis için gerekli
    whiteSpace: 'nowrap',
    textOverflow: 'ellipsis',
  },
  clientCode: {
    fontFamily: 'monospace',
    fontWeight: 600,
    color: '#007bff',
  },
  clientName: {
    fontWeight: 500,
    color: '#2c3e50',
  },
  clientType: {
    padding: '4px 8px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 600,
    textAlign: 'center',
    minWidth: '80px',
    display: 'inline-block',
    backgroundColor: '#f8f9fa',
    color: '#495057',
    border: '1px solid #dee2e6',
  },
  status: {
    padding: '4px 12px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 600,
    textAlign: 'center',
    minWidth: '60px',
    display: 'inline-block',
  },
  statusActive: {
    backgroundColor: '#d4edda',
    color: '#155724',
    border: '1px solid #c3e6cb',
  },
  statusInactive: {
    backgroundColor: '#f8d7da',
    color: '#721c24',
    border: '1px solid #f5c6cb', // <<< düzeltilen satır
  },
  actionButton: {
    padding: '6px 12px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    fontSize: '12px',
    cursor: 'pointer',
    transition: 'background-color 0.2s',
  },
  actionButtonHover: { backgroundColor: '#0056b3' },
  emptyState: {
    textAlign: 'center',
    padding: '40px 20px',
    color: '#6c757d',
    fontSize: '16px',
  },
};

const getVal = (...cands) =>
    cands.find((v) => v !== undefined && v !== null && String(v).trim() !== '');
const toStr = (v, d = '-') =>
    v === undefined || v === null || String(v).trim() === '' ? d : String(v);

const getId = (c) =>
    getVal(c?.id, c?._id, c?.uuid, c?.musteriId, c?.clientId, c?.clientCode) ||
    Math.random().toString(36).slice(2);
const getCode = (c) => toStr(getVal(c?.musteriKodu, c?.clientCode, c?.code));
const getEmail = (c) => toStr(getVal(c?.email, c?.eposta, c?.mail));
const getPhone = (c) => toStr(getVal(c?.telefon, c?.phone, c?.gsm, c?.tel));
const getTypeRaw = (c) => getVal(c?.customerType, c?.musteriTipi, c?.type, c?.tip);
const getTypeLabel = (t) =>
    String(t).toUpperCase() === 'GERCEK'
        ? 'Bireysel'
        : String(t).toUpperCase() === 'TUZEL'
            ? 'Kurumsal'
            : toStr(t);
const getStatusRaw = (c) => getVal(c?.status, c?.durum, c?.state);
const getStatusLabel = (s) => {
  const v = String(s || '').toLowerCase();
  if (['aktif', 'active', '1', 'true'].includes(v)) return 'Aktif';
  if (['pasif', 'inactive', '0', 'false'].includes(v)) return 'Pasif';
  return toStr(s, 'Aktif');
};
const getName = (c) => {
  const type = String(getTypeRaw(c) || '').toUpperCase();
  if (type === 'GERCEK') {
    const ad = toStr(getVal(c?.ad, c?.firstName, c?.isim), '').trim();
    const soyad = toStr(getVal(c?.soyad, c?.lastName), '').trim();
    const full = `${ad} ${soyad}`.trim();
    return full || '-';
  }
  return toStr(getVal(c?.sirketUnvani, c?.unvan, c?.companyName, c?.name, c?.adSoyad));
};

export default function CustomerTable({ clients = [], onDetail = () => {} }) {
  const [hoveredRow, setHoveredRow] = React.useState(null);
  const [hoveredButton, setHoveredButton] = React.useState(null);

  const rows = Array.isArray(clients) ? clients : [];

  if (rows.length === 0) {
    return (
        <div style={styles.emptyState}>
          <p>Henüz müşteri bulunmuyor.</p>
          <p style={{ fontSize: '14px', marginTop: '8px' }}>
            Yeni müşteri eklemek için yukarıdaki butonları kullanabilirsiniz.
          </p>
        </div>
    );
  }

  return (
      <div style={styles.tableWrap}>
        <table style={styles.table}>
          <colgroup>
            <col style={{ width: COL.code }} />
            <col style={{ width: COL.name }} />
            <col style={{ width: COL.email }} />
            <col style={{ width: COL.phone }} />
            <col style={{ width: COL.type }} />
            <col style={{ width: COL.status }} />
            <col style={{ width: COL.action }} />
          </colgroup>

          <thead style={styles.header}>
          <tr>
            <th style={styles.headerCell}>Müşteri Kodu</th>
            <th style={styles.headerCell}>Ad / Şirket</th>
            <th style={styles.headerCell}>E-posta</th>
            <th style={styles.headerCell}>Telefon</th>
            <th style={styles.headerCell}>Tip</th>
            <th style={styles.headerCell}>Durum</th>
            <th style={{ ...styles.headerCell, textAlign: 'right' }}>Eylemler</th>
          </tr>
          </thead>

          <tbody>
          {rows.map((client, index) => {
            const id = getId(client);
            const code = getCode(client);
            const name = getName(client);
            const email = getEmail(client);
            const phone = getPhone(client);
            const typeLabel = getTypeLabel(getTypeRaw(client));
            const statusLabel = getStatusLabel(getStatusRaw(client));

            return (
                <tr
                    key={id}
                    style={{ ...styles.row, ...(hoveredRow === index ? styles.rowHover : {}) }}
                    onMouseEnter={() => setHoveredRow(index)}
                    onMouseLeave={() => setHoveredRow(null)}
                >
                  <td style={{ ...styles.cell, ...styles.clientCode }} title={code}>{code}</td>
                  <td style={{ ...styles.cell, ...styles.clientName }} title={name}>{name}</td>
                  <td style={styles.cell} title={email}>{email}</td>
                  <td style={styles.cell} title={phone}>{phone}</td>
                  <td style={styles.cell}>
                    <span style={styles.clientType} title={typeLabel}>{typeLabel}</span>
                  </td>
                  <td style={styles.cell}>
                  <span
                      style={{
                        ...styles.status,
                        ...(statusLabel === 'Aktif' ? styles.statusActive : styles.statusInactive),
                      }}
                      title={statusLabel}
                  >
                    {statusLabel}
                  </span>
                  </td>
                  <td style={{ ...styles.cell, textAlign: 'right' }}>
                    <button
                        style={{ ...styles.actionButton, ...(hoveredButton === index ? styles.actionButtonHover : {}) }}
                        onClick={() => onDetail(id)}
                        onMouseEnter={() => setHoveredButton(index)}
                        onMouseLeave={() => setHoveredButton(null)}
                    >
                      Detay Görüntüle
                    </button>
                  </td>
                </tr>
            );
          })}
          </tbody>
        </table>
      </div>
  );
}
