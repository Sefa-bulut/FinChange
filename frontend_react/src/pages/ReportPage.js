import React, { useState, useMemo } from 'react';
import { useAuth } from '../context/AuthContext';
import { AsyncPaginate } from 'react-select-async-paginate';
import { getFullReport, downloadExcel, fetchClientOptions } from '../services/reportService';
import ProfitLineChart from '../components/report/ProfitLineChart';
import DonutChart from '../components/report/DonutChart';
import PositionsAndTradesTables from '../components/report/PositionsAndTradesTables';

const UX = {
    shell: { background:'#fff', borderRadius:16, boxShadow:'0 8px 24px rgba(0,0,0,.06)', border:'1px solid #eceff3' },
    bar: { background:'#eef0f3', borderRadius:'12px 12px 0 0', padding:'10px 12px', fontWeight:700, color:'#111827' },
    grid: { display:'grid', gridTemplateColumns:'2fr 1fr', gap:16, padding:16 },
    left: { display:'grid', gridTemplateColumns:'1.2fr 1fr', gap:12 },
    card: { background:'#f5f6f7', border:'1px solid #e3e6ea', borderRadius:10, padding:12 },
    kpiBox:  { background:'#fff', border:'1px dashed #e3e6ea', borderRadius:12, padding:12, display:'grid', gap:6 },
    kpiLabel: { fontSize:12, color:'#6b7280' },
    kpiValue: { fontSize:18, fontWeight:700 },
    kpiGreen: { color:'#16a34a', fontWeight:700 },
    kpiRed: { color:'#dc2626', fontWeight:700 },
    donutWrap:{ background:'#f5f6f7', border:'1px solid #e3e6ea', borderRadius:10, padding:12, display:'grid', gap:8 },
    legend:{ display:'grid', gap:6, fontSize:12, color:'#374151' },
};

const fmt = (n, d=2) =>
    (n==null || isNaN(+n)) ? '—' :
        Number(n).toLocaleString('tr-TR',{minimumFractionDigits:d,maximumFractionDigits:d});

const safeDate = v => v ? new Date(v).toLocaleString('tr-TR') : '—';

const nvdText = (report) => {
    const accounts = report?.accounts || [];
    const byCcy = accounts.reduce((acc, a) => {
        acc[a.currency] = (acc[a.currency] || 0) + Number(a.balance || 0);
        return acc;
    }, {});
    if (byCcy.TRY != null) return `${fmt(byCcy.TRY)} TL (Toplam Portföy Değeri)`;
    const [ccy, amt] = Object.entries(byCcy)[0] || [];
    return ccy ? `${fmt(amt)} ${ccy}` : '—';
};

const toIsoLocal = (v) => {
    const d = v instanceof Date ? v : new Date(v);
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};

export default function ReportPage() {
    const { token } = useAuth();
    const [selectedClient, setSelectedClient] = useState(null);
    const [reportData, setReportData] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState('');

    const stocksData = useMemo(() => {
        const openPositions = reportData?.openPositions || [];
        const tradeHistory = reportData?.tradeHistory || [];

        const byPos = {};
        for (const p of openPositions) {
            const sym = p?.symbol;
            const lot = Number(p?.lot) || 0;
            if (!sym || lot <= 0) continue;
            byPos[sym] = (byPos[sym] || 0) + lot;
        }
        const fromPos = Object.entries(byPos).map(([name, value]) => ({ name, value }));
        if (fromPos.length > 0) return fromPos;

        const byTrade = {};
        for (const t of tradeHistory) {
            const sym = t?.symbol;
            const lot = Number(t?.lot) || 0;
            if (!sym || lot <= 0) continue;
            const side = String(t?.side || '').toUpperCase();
            const sign = side === 'SATIM' || side === 'S' ? -1 : 1;
            byTrade[sym] = (byTrade[sym] || 0) + sign * lot;
        }
        return Object.entries(byTrade)
            .filter(([, v]) => v > 0)
            .map(([name, value]) => ({ name, value }));
    }, [reportData]);

    const loadClients = async (search, loadedOptions, { page }) => {
        try {
            const { options, hasMore, nextPage } = await fetchClientOptions(search, page);
            return {
                options: options.map(opt => ({
                    value: opt.value,
                    label: opt.label,
                    data: opt.data ?? opt.raw ?? opt,
                })),
                hasMore,
                additional: { page: nextPage },
            };
        } catch (err) {
            console.error('Müşteri listesi alınamadı:', err);
            return { options: [], hasMore: false, additional: { page } };
        }
    };

    const handleClientSelect = (selected) => {
        if (!selected) {
            setSelectedClient(null);
            setReportData(null);
            setMessage('');
            return;
        }

        const client = selected?.data ?? null;
        setSelectedClient(client);
        setReportData(null);
        setIsLoading(true);
        setMessage('');

        const customerCode = client?.musteriKodu ?? selected.value;
        const startIso = toIsoLocal('2025-01-01T00:00:00');
        const endIso   = toIsoLocal(new Date());

        getFullReport(customerCode, startIso, endIso)
            .then((data) => {
                if (!data || !data.customerInfo) {
                    setMessage('Bu müşteriye ait rapor verisi bulunamadı.');
                    setReportData(null);
                } else {
                    setReportData(data);
                }
            })
            .catch((err) => {
                console.error('Rapor verisi alınırken hata oluştu:', err);
                setMessage('🛘 Sunucudan rapor verileri alınamadı.');
                setReportData(null);
            })
            .finally(() => setIsLoading(false));
    };

    const thStyle = {
        padding: '10px',
        fontWeight: 'bold',
        textAlign: 'left',
        borderBottom: '2px solid #dee2e6',
    };

    const tdStyle = {
        padding: '10px',
        borderBottom: '1px solid #dee2e6',
        fontSize: '14px',
    };

    const zebraStyle = (index) => ({
        backgroundColor: index % 2 === 0 ? '#ffffff' : '#f8f9fa',
    });

    const cardStyle = {
        backgroundColor: '#f8f9fa',
        padding: '1rem 2rem',
        borderRadius: '10px',
        boxShadow: '0 0 10px rgba(0,0,0,0.05)',
        textAlign: 'center',
        minWidth: '180px',
    };

    const donutData = (reportData?.accounts || []).reduce((arr, acc) => {
        const idx = arr.findIndex(x => x.name === acc.currency);
        if (idx >= 0) arr[idx].value += Number(acc.balance || 0);
        else arr.push({ name: acc.currency, value: Number(acc.balance || 0) });
        return arr;
    }, []);

    const donutLegend = donutData.map(d => `${d.name}: ${fmt(d.value, 0)}`);

    const kpis = reportData?.kpis;
    const periodValue = (kpis && kpis.periodReturnPct != null) ? Number(kpis.periodReturnPct) : null;
    const periodSign = (periodValue == null || isNaN(periodValue)) ? null : (periodValue > 0 ? 1 : (periodValue < 0 ? -1 : 0));
    const periodReturn = (periodSign == null) ? '—' : `${periodSign>0?'+':''}%${fmt(Math.abs(periodValue), 2)}`;
    const periodStyle = periodSign == null ? UX.kpiValue : (periodSign > 0 ? UX.kpiGreen : (periodSign < 0 ? UX.kpiRed : UX.kpiValue));
    const benchmarkText = (() => {
        if (!kpis) return '—';
        const code = kpis.benchmarkCode ?? '—';
        const pct = kpis.benchmarkPct != null ? `%${fmt(kpis.benchmarkPct, 2)}` : '—';
        return `${code} ${pct}`;
    })();
    const riskGroupText = kpis?.riskGroup ?? '—';

    return (
        <div style={{ padding: '2rem', fontFamily: 'Arial' }}>
            <h2 style={{ marginBottom: '1rem' }}>📊 Yönetici / Kar-Zarar Analizi ve Raporlama</h2>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                <label>Müşteri Arayın:</label>
                <AsyncPaginate
                    placeholder="Müşteri ID, ad veya soyad yazın..."
                    loadOptions={loadClients}
                    onChange={handleClientSelect}
                    isClearable
                    additional={{ page: 0 }}
                    styles={{ container: (base) => ({ ...base, width: 350, marginTop: '8px' }) }}
                />
                <button
                    onClick={() => downloadExcel(reportData)}
                    style={{
                        padding: '10px 20px',
                        backgroundColor: '#FF0000',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontWeight: 'bold',
                    }}
                    title="Excel olarak indir"
                    disabled={!reportData}
                >
                    Excel İndir
                </button>
            </div>

            {reportData && (
                <div style={{ ...UX.shell, marginBottom:16 }}>
                    <div style={UX.bar}>
                        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                            <span>Müşteri Bilgi Özeti</span>
                            <span style={{ opacity:.7, fontWeight:600 }}>Varlık Dağılımı</span>
                        </div>
                    </div>

                    <div style={UX.grid}>
                        <div style={UX.left}>
                            <div style={UX.card}>
                                <div style={{ fontWeight:700, marginBottom:8, color:'#374151' }}>Müşteri Bilgileri</div>
                                <div style={{ fontSize:13, color:'#374151', display:'grid', gap:4 }}>
                                    <div><b>Kod:</b> {reportData.customerInfo?.customerCode || '—'}</div>
                                    <div><b>İsim:</b> {reportData.customerInfo?.customerName || '—'}</div>
                                    <div><b>Türü:</b> {reportData.customerInfo?.customerType || '—'}</div>
                                </div>
                            </div>

                            <div style={{ display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:10 }}>
                                <div style={UX.kpiBox}>
                                    <div style={UX.kpiLabel}>Rapor Dönemi</div>
                                    <div style={UX.kpiValue}>{/* dönem seçimi yok, statik bırak */}Son Dönem</div>
                                </div>
                                <div style={UX.kpiBox}>
                                    <div style={UX.kpiLabel}>Benchmark</div>
                                    <div style={UX.kpiValue}>{benchmarkText}</div>
                                </div>
                                <div style={UX.kpiBox}>
                                    <div style={UX.kpiLabel}>Oluşturulma Tarihi</div>
                                    <div style={UX.kpiValue}>{safeDate(reportData.reportGeneratedAt)}</div>
                                </div>

                                <div style={{ ...UX.kpiBox, gridColumn:'span 2' }}>
                                    <div style={UX.kpiLabel}>Net Varlık Değeri (NVD)</div>
                                    <div style={UX.kpiValue}>{nvdText(reportData)}</div>
                                </div>
                                <div style={UX.kpiBox}>
                                    <div style={UX.kpiLabel}>Dönemsel Getiri</div>
                                    <div style={periodStyle}>{periodReturn}</div>
                                </div>
                                <div style={UX.kpiBox}>
                                    <div style={UX.kpiLabel}>Risk Profili/Grubu</div>
                                    <div style={UX.kpiValue}>{riskGroupText}</div>
                                </div>
                            </div>
                        </div>

                        <div style={UX.donutWrap}>
                            <DonutChart
                                data={donutData}
                                stocksData={stocksData}
                            />
                            <div style={UX.legend}>
                                {donutData.length > 0 && (
                                    <>
                                        <div style={{ fontWeight: 600 }}>Para Birimi Dağılımı</div>
                                        {donutLegend.map((t,i)=>(<div key={`ccy-${i}`}>• {t}</div>))}
                                    </>
                                )}
                                {stocksData.length > 0 && (
                                    <>
                                        <div style={{ fontWeight: 600, marginTop: 8 }}>Hisse Dağılımı (Lot)</div>
                                        {stocksData.map((s,i)=>(
                                            <div key={`stk-${i}`}>• {s.name}: {Number(s.value).toLocaleString('tr-TR')} lot</div>
                                        ))}
                                    </>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {isLoading && (
                <p>Rapor verileri yükleniyor, lütfen bekleyin...</p>
            )}

            {!reportData && !isLoading && (
                <p style={{ color: '#6c757d', fontStyle: 'italic' }}>
                    Lütfen bir müşteri seçiniz.
                </p>
            )}

            {message && <p style={{ color: 'red' }}>{message}</p>}

            {reportData && (
                <div style={{ marginTop: '3rem' }}>
                    <h4>📉 Kar-Zarar Eğrisi</h4>
                    {reportData?.profitLoss?.trades?.length > 0 ? (
                        <ProfitLineChart data={reportData.profitLoss.trades} />
                    ) : (
                        <p style={{ color: '#6c757d', fontStyle: 'italic' }}>
                            Bu müşteriye ait kar-zarar verisi bulunmamaktadır.
                        </p>
                    )}
                </div>
            )}
            {reportData && (
                <PositionsAndTradesTables
                    openPositions={reportData?.openPositions || []}
                    tradeHistory={reportData?.tradeHistory || []}
                    currency="TL"
                />
            )}
        </div>
    );
}
