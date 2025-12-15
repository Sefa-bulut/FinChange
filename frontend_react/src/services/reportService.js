import api from './api';
import * as XLSX from 'xlsx';

const pick = (res) => res?.data ?? res;
const resultOf = (res) => pick(res)?.result ?? pick(res);

export const getFullReport = async (customerCode, start, end) => {
    try {
        const params = { customerCode, start, end };
        const response = await api.get('reports/details', { params });

        const responseData = response && response.data ? response.data : response;
        const result = responseData && responseData.result ? responseData.result : null;

        if (responseData && responseData.isSuccess === true && result) {
            return result;
        }

        console.error("API'den beklenen formatta veri gelmedi. Gelen yanıt:", responseData);
        return null; // UI'ın "veri yok" akışını tetikle
    } catch (error) {
        console.error('getFullReport API çağrısı başarısız oldu:', error);
        if (error && error.response) {
            console.error('Hata Detayı:', error.response.data);
        }
        return null;
    }
};

export const fetchClientOptions = async (search = '', page = 0, pageSize = 10) => {
    try {
        const res = await api.get('clients');
        const allClients = resultOf(res) ?? [];

        const term = (search || '').toLowerCase();
        const filtered = allClients.filter((c) =>
            c?.musteriKodu?.toLowerCase()?.includes(term) ||
            c?.ad?.toLowerCase()?.includes(term) ||
            c?.soyad?.toLowerCase()?.includes(term) ||
            c?.sirketUnvani?.toLowerCase()?.includes(term)
        );

        const paged = filtered.slice(page * pageSize, (page + 1) * pageSize);

        const options = paged.map((client) => ({
            value: client.musteriKodu,
            label: `${client.musteriKodu} - ${
                [client.ad, client.soyad].filter(Boolean).join(' ') || client.sirketUnvani || ''
            }`.trim(),
            data: client,
        }));

        return {
            options,
            hasMore: (page + 1) * pageSize < filtered.length,
            nextPage: page + 1,
        };
    } catch (err) {
        console.error('fetchClientOptions hata:', err);
        return { options: [], hasMore: false, nextPage: page };
    }
};

export const downloadExcel = (fullReportData) => {
    if (!fullReportData || !fullReportData.customerInfo) {
        alert('Rapor verisi bulunamadı.');
        return;
    }

    const {
        customerInfo,
        reportGeneratedAt,
        accounts = [],
        openPositions = [],
        tradeHistory = [],
    } = fullReportData;

    const rows = [];
    const nowStr = reportGeneratedAt
        ? new Date(reportGeneratedAt).toLocaleString('tr-TR')
        : new Date().toLocaleString('tr-TR');

    rows.push([`Rapor: ${customerInfo.customerCode ?? ''}`, '', '', '', '', '']);
    rows.push(['Oluşturulma Tarihi', nowStr]);
    rows.push([]);

    rows.push(['Müşteri Bilgileri']);
    rows.push(['Kod', customerInfo.customerCode ?? '']);
    rows.push(['Ad Soyad / Ünvan', customerInfo.customerName ?? '']);
    rows.push(['Tür', customerInfo.customerType ?? '']);
    rows.push([]);

    const balanceByCcy = accounts.reduce((acc, a) => {
        acc[a.currency] = (acc[a.currency] || 0) + Number(a.balance || 0);
        return acc;
    }, {});
    rows.push(['Para Birimi Dağılımı']);
    rows.push(['Para Birimi', 'Toplam Bakiye']);
    Object.entries(balanceByCcy).forEach(([ccy, amount]) => rows.push([ccy, Number(amount ?? 0)]));
    rows.push([]);

    rows.push(['Hesap Listesi']);
    rows.push(['Hesap No', 'Hesap Adı', 'Para Birimi', 'Bakiye']);
    accounts.forEach((acc) => rows.push([
        acc.accountNumber ?? '',
        acc.accountName ?? '',
        acc.currency ?? '',
        Number(acc.balance ?? 0),
    ]));
    rows.push([]);

    rows.push(['Açık Pozisyonlar']);
    rows.push(['Tarih', 'Piyasa', 'Yön', 'Anlık Fiyat', 'Sembol', 'Lot', 'Ort. Maliyet', 'Tutar', 'Anlık K/Z']);
    openPositions.forEach((p) => rows.push([
        p.datetime ?? '',
        p.market ?? '',
        p.side ?? '',
        Number(p.price ?? 0),
        p.symbol ?? '',
        Number(p.lot ?? 0),
        Number(p.avgCost ?? 0),
        Number(p.amount ?? 0),
        Number(p.pnl ?? 0),
    ]));
    rows.push([]);

    rows.push(['İşlem Geçmişi']);
    rows.push(['Tarih', 'Piyasa', 'Yön', 'Sembol', 'Lot', 'Fiyat', 'Tutar', 'Komisyon']);
    tradeHistory.forEach((t) => rows.push([
        t.executionTime ? new Date(t.executionTime).toLocaleString('tr-TR') : '',
        t.market ?? '',
        t.side ?? '',
        t.symbol ?? '',
        Number(t.lot ?? 0),
        Number(t.price ?? 0),
        Number(t.amount ?? 0),
        Number(t.commission ?? 0),
    ]));

    const ws = XLSX.utils.aoa_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Rapor');
    XLSX.writeFile(wb, `${customerInfo.customerCode ?? 'rapor'}_rapor.xlsx`);
};
