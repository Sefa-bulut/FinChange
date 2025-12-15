import React, { useState, useEffect, useMemo, useCallback, useReducer } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { useMarketData } from '../../hooks/useMarketData';
import { getMyGroups, getActiveMembers } from '../../services/portfolioGroupService';
import { createBulkOrder, validateLot } from '../../services/orderService';
import { toast } from 'react-toastify';
import { debounce } from 'lodash';
import { getMarketStatus } from '../../services/marketSessionService';
import { FaLock, FaUnlock, FaInfoCircle} from 'react-icons/fa';
import { getSettlementInfoByBist } from '../../services/portfolioInfoService';
import CustomerRow from './CustomerRow';

function getPriceColor(value, baseline) {
    const numValue = Number(value);
    const numBaseline = Number(baseline);

    if (isNaN(numValue) || isNaN(numBaseline)) return {};

    if (numValue > numBaseline) return { color: '#28a745' }; // Yeşil
    if (numValue < numBaseline) return { color: '#dc3545' }; // Kırmızı
    return { color: '#6c757d' }; // Nötr Gri
}

function getEffectivePrice(orderType, limitPriceInput, currentPrice) {
    if (orderType === 'LIMIT') {
        const lp = Number(limitPriceInput);
        return isNaN(lp) || lp <= 0 ? 0 : lp;
    }
    return Number(currentPrice) || 0;
}

function getTickForPrice(price) {
    const p = Number(price) || 0;
    if (p <= 19.999) return 0.01;
    if (p <= 49.999) return 0.02;
    if (p <= 99.999) return 0.05;
    if (p <= 249.999) return 0.10;
    if (p <= 499.999) return 0.25;
    if (p <= 999.999) return 0.50;
    if (p <= 2499.999) return 1.00;
    return 2.50;
}

function snapToTick(price) {
    const p = Number(price) || 0;
    const tick = getTickForPrice(p);
    if (tick === 0) return 0;
    const snapped = Math.round(p / tick) * tick;
    const decimals = tick >= 1 ? 1 : 2;
    return Number(snapped.toFixed(decimals));
}

const initialState = {
    members: [],
    customerOrders: {}
};

function orderPlacementReducer(state, action) {
    switch (action.type) {
        case 'SET_MEMBERS_AND_ORDERS': {
            const initialOrders = {};
            (action.payload || []).forEach(member => {
                (member.accounts || []).forEach(account => {
                    initialOrders[account.id] = {
                        data: account,
                        customer: member,
                        lot: 0,
                        isValid: true,
                        error: '',
                        isLocked: false,
                        isSelected: true
                    };
                });
            });
            return { ...state, members: action.payload || [], customerOrders: initialOrders };
        }
        case 'APPLY_PERCENTAGE': {
            const { percentage, transactionType, priceToUse, assetBistCode } = action.payload;
            const newOrders = { ...state.customerOrders };
            Object.values(newOrders).forEach(order => {
                if (!order.isLocked && order.isSelected) {
                    let calculatedLot = 0;
                    if (transactionType === 'BUY') {
                        const availableBalance = Math.max(0, (Number(order.data.balance) || 0) - (Number(order.data.blockedBalance) || 0));
                        const investmentAmount = availableBalance * (Number(percentage) || 0) / 100.0;
                        calculatedLot = priceToUse > 0 ? Math.floor(investmentAmount / priceToUse) : 0;
                    } else {
                        const assetInfo = order.data.assets?.find(a => a.bistCode === assetBistCode);
                        const availableLots = assetInfo ? Number(assetInfo.availableLots) || 0 : 0;
                        calculatedLot = Math.floor(availableLots * (Number(percentage) || 0) / 100.0);
                    }
                    order.lot = calculatedLot > 0 ? calculatedLot : 0;
                }
            });
            return { ...state, customerOrders: newOrders };
        }
        case 'UPDATE_LOT_MANUAL': {
            const { accountId, lot } = action.payload;
            if (!state.customerOrders[accountId]) return state;

            return {
                ...state,
                customerOrders: {
                    ...state.customerOrders,
                    [accountId]: {
                        ...state.customerOrders[accountId],
                        lot: lot,
                        isLocked: true,
                        isValid: true,
                        error: ''
                    }
                }
            };
        }
        case 'UNLOCK_LOT': {
            const { accountId } = action.payload;
            const prev = state.customerOrders[accountId];
            if (!prev) return state;
            return {
                ...state,
                customerOrders: {
                    ...state.customerOrders,
                    [accountId]: { ...prev, isLocked: false }
                }
            };
        }
        case 'TOGGLE_CUSTOMER_SELECTION': {
            const { accountId, isSelected } = action.payload;
            const prev = state.customerOrders[accountId];
            if (!prev) return state;
            return {
                ...state,
                customerOrders: {
                    ...state.customerOrders,
                    [accountId]: { ...prev, isSelected }
                }
            };
        }
        case 'TOGGLE_SELECT_ALL': {
            const newOrders = { ...state.customerOrders };
            Object.values(newOrders).forEach(o => { o.isSelected = action.payload.isSelected; });
            return { ...state, customerOrders: newOrders };
        }
        case 'SET_VALIDATION_STATUS': {
            const { accountId, isValid, error } = action.payload;
            const prev = state.customerOrders[accountId];
            if (!prev) return state;
            return {
                ...state,
                customerOrders: {
                    ...state.customerOrders,
                    [accountId]: { ...prev, isValid, error: error || '' }
                }
            };
        }
        default:
            return state;
    }
}

const styles = {
    pageContainer: {
        padding: '20px',
        backgroundColor: '#f8f9fa',
        minHeight: '100vh'
    },
    headerBar: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '20px'
    },
    backButton: {
        background: 'none',
        border: '1px solid #6c757d',
        padding: '8px 15px',
        borderRadius: '5px',
        cursor: 'pointer',
        fontSize: '16px',
        display: 'flex',
        alignItems: 'center',
        gap: '5px'
    },
    mainTitle: {
        fontSize: '24px',
        fontWeight: 'bold',
        margin: 0,
        color: '#c8102e'
    },
    layoutContainer: {
        display: 'grid',
        gridTemplateColumns: '350px 1fr',
        gap: '20px',
        marginBottom: '20px'
    },
    leftPanel: {
        display: 'flex',
        flexDirection: 'column',
        gap: '20px'
    },
    rightPanel: {
        display: 'flex',
        flexDirection: 'column',
        gap: '20px'
    },
    card: {
        backgroundColor: 'white',
        padding: '20px',
        borderRadius: '8px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        border: '1px solid #e9ecef'
    },
    cardTitle: {
        fontSize: '18px',
        fontWeight: 'bold',
        marginBottom: '15px',
        color: '#343a40',
        borderBottom: '2px solid #c8102e',
        paddingBottom: '8px'
    },
    formGroup: {
        marginBottom: '15px'
    },
    label: {
        display: 'block',
        marginBottom: '5px',
        fontWeight: '600',
        color: '#495057'
    },
    input: {
        width: '100%',
        padding: '10px',
        border: '1px solid #ced4da',
        borderRadius: '4px',
        fontSize: '14px'
    },
    select: {
        width: '100%',
        padding: '10px',
        border: '1px solid #ced4da',
        borderRadius: '4px',
        background: 'white',
        fontSize: '14px'
    },
    priceInfo: {
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '15px',
        backgroundColor: '#f8f9fa',
        borderRadius: '6px',
        marginBottom: '15px',
        border: '1px solid #dee2e6'
    },
    priceLabel: {
        fontWeight: '600',
        color: '#6c757d'
    },
    priceValue: {
        fontSize: '18px',
        fontWeight: 'bold',
        color: '#28a745'
    },
    groupTabs: {
        display: 'flex',
        gap: '5px',
        marginBottom: '15px',
        flexWrap: 'wrap'
    },
    groupTab: {
        padding: '8px 16px',
        border: '1px solid #ced4da',
        borderRadius: '20px',
        cursor: 'pointer',
        fontSize: '14px',
        fontWeight: '500',
        transition: 'all 0.2s',
        whiteSpace: 'nowrap'
    },
    groupTabActive: {
        backgroundColor: '#c8102e',
        color: 'white',
        borderColor: '#c8102e'
    },
    groupTabInactive: {
        backgroundColor: 'white',
        color: '#6c757d',
        borderColor: '#ced4da'
    },
    customerListContainer: {
        backgroundColor: 'white',
        borderRadius: '8px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        border: '1px solid #e9ecef'
    },
    customerListHeader: {
        padding: '15px 20px',
        borderBottom: '1px solid #e9ecef',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
    },
    customerListTitle: {
        fontSize: '18px',
        fontWeight: 'bold',
        color: '#343a40',
        margin: 0
    },
    tableContainer: {
        maxHeight: '400px',
        overflowY: 'auto'
    },
    table: {
        width: '100%',
        borderCollapse: 'collapse'
    },
    th: {
        background: '#f8f9fa',
        padding: '12px',
        textAlign: 'left',
        fontWeight: '600',
        color: '#495057',
        fontSize: '14px',
        borderBottom: '2px solid #dee2e6',
        position: 'sticky',
        top: 0,
        zIndex: 1
    },
    td: {
        padding: '10px 12px',
        borderBottom: '1px solid #e9ecef',
        fontSize: '14px'
    },
    lotInput: {
        width: '80px',
        padding: '6px 8px',
        border: '1px solid #ced4da',
        borderRadius: '4px',
        textAlign: 'center',
        fontSize: '14px'
    },
    validationError: {
        color: '#dc3545',
        fontSize: '12px',
        marginTop: '2px'
    },
    validationSuccess: {
        color: '#28a745',
        fontSize: '12px',
        marginTop: '2px'
    },
    submitButton: {
        backgroundColor: '#c8102e',
        color: 'white',
        border: 'none',
        padding: '12px 24px',
        borderRadius: '6px',
        fontSize: '16px',
        fontWeight: '600',
        cursor: 'pointer',
        transition: 'background-color 0.2s',
        marginTop: '20px'
    },
    submitButtonDisabled: {
        backgroundColor: '#6c757d',
        cursor: 'not-allowed'
    },
    percentageSlider: {
        width: '100%',
        marginTop: '10px'
    },
    noGroupSelected: {
        textAlign: 'center',
        padding: '40px',
        color: '#6c757d',
        fontSize: '16px'
    },
    modalOverlay: {
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0,0,0,0.35)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000
    },
    modalContent: {
        width: '480px',
        maxWidth: '90vw',
        backgroundColor: 'white',
        borderRadius: '8px',
        boxShadow: '0 8px 24px rgba(0,0,0,0.15)',
        padding: '20px',
        border: '1px solid #e9ecef'
    },
    modalTitle: {
        fontSize: '18px',
        fontWeight: 'bold',
        marginBottom: '10px',
        color: '#343a40'
    },
    modalMessage: {
        fontSize: '14px',
        color: '#495057',
        marginBottom: '16px'
    },
    modalFooter: {
        display: 'flex',
        justifyContent: 'flex-end',
        gap: '10px'
    },
    modalButton: {
        backgroundColor: '#c8102e',
        color: 'white',
        border: 'none',
        padding: '8px 16px',
        borderRadius: '6px',
        fontSize: '14px',
        fontWeight: 600,
        cursor: 'pointer'
    }
};

export default function OrderPlacement({ selectedAsset, onGoBack }) {
    const navigate = useNavigate();
    const livePriceData = useMarketData(selectedAsset.bistCode);
    const currentPrice = livePriceData?.price || selectedAsset.price;

    const { register, handleSubmit, watch, setValue, control } = useForm({
        defaultValues: {
            transactionType: 'BUY',
            orderType: 'MARKET',
            limitPrice: '',
            percentage: 0
        }
    });

    const watchOrderType = watch("orderType");
    const watchPercentage = watch("percentage");
    const watchLimitPrice = watch("limitPrice");
    const watchTransactionType = watch("transactionType");

    const handleApplyPercentage = () => {
        const priceToUse = getEffectivePrice(watchOrderType, watchLimitPrice, currentPrice);

        if (!priceToUse || isNaN(priceToUse)) {
            toast.error("Lot hesaplaması için geçerli bir fiyat bulunamadı.");
            return;
        }

        dispatch({
            type: 'APPLY_PERCENTAGE',
            payload: {
                percentage: watchPercentage,
                transactionType: watchTransactionType,
                priceToUse,
                assetBistCode: selectedAsset.bistCode
            }
        });
        toast.success(`%${watchPercentage} oranı seçili ve kilitsiz hesaplara uygulandı.`);
    };

    const [groups, setGroups] = useState([]);
    const [selectedGroupIds, setSelectedGroupIds] = useState([]);
    const [state, dispatch] = useReducer(orderPlacementReducer, initialState);
    const customerOrders = state.customerOrders;
    const [isLoading, setIsLoading] = useState({});
    const [isMarketOpen, setIsMarketOpen] = useState(true);
    const [settlementModal, setSettlementModal] = useState({ open: false, message: '' });

    const handleGroupToggle = (groupId) => {
        setSelectedGroupIds(prevIds =>
            prevIds.includes(groupId)
                ? prevIds.filter(id => id !== groupId)
                : [...prevIds, groupId]
        );
    };

    const handleSelectCustomer = (accountId, isSelected) => {
        dispatch({ type: 'TOGGLE_CUSTOMER_SELECTION', payload: { accountId, isSelected } });
    };

    const handleSelectAllCustomers = (e) => {
        const isSelected = e.target.checked;
        dispatch({ type: 'TOGGLE_SELECT_ALL', payload: { isSelected } });
    };

    const handleUnlockLot = (accountId) => {
        dispatch({ type: 'UNLOCK_LOT', payload: { accountId } });
    };

    const handleShowSettlementInfo = async (customerId) => {
        try {
            const info = await getSettlementInfoByBist(customerId, selectedAsset.bistCode);
            const msg = info && info.message ? info.message : 'Takas bilgisi alınamadı.';
            setSettlementModal({ open: true, message: msg });
        } catch (e) {
            console.error('Settlement info error', e);
            setSettlementModal({ open: true, message: 'Takas bilgisi alınamadı.' });
        }
    };

    useEffect(() => {
        const checkMarketStatus = async () => {
            try {
                const status = await getMarketStatus();
                const effectiveOpen = !!(status?.open || status?.overrideTrading);
                setIsMarketOpen(effectiveOpen);
                if (!effectiveOpen && watch("orderType") === 'MARKET') {
                    setValue('orderType', 'LIMIT');
                    toast.warn("Piyasa kapalı olduğu için emir türü 'Limit' olarak değiştirildi.");
                }
            } catch (e) {
                console.warn('Piyasa durumu alınamadı, açık varsayılıyor.', e);
                setIsMarketOpen(true); 
            }
        };
        checkMarketStatus();
    }, []); 

    useEffect(() => {
        if (watchOrderType === 'LIMIT') {
            const lp = Number(watchLimitPrice);
            if (!lp || isNaN(lp) || lp <= 0) {
                const snapped = snapToTick(currentPrice);
                setValue('limitPrice', snapped, { shouldValidate: true, shouldDirty: true });
            }
        }
    }, [watchOrderType]);

    useEffect(() => {
        if (watchOrderType === 'LIMIT') {
            const lp = Number(watchLimitPrice);
            if (!lp || isNaN(lp) || lp <= 0) {
                setValue('limitPrice', snapToTick(currentPrice), { shouldValidate: false });
            }
        }
    }, [currentPrice]);

    useEffect(() => {
        const loadGroups = async () => {
            try {
                const groupsData = await getMyGroups();
                setGroups(groupsData || []);
                if (groupsData && groupsData.length > 0) {
                    setSelectedGroupIds([groupsData[0].id]);
                }
            } catch (error) {
                toast.error("Gruplar yüklenemedi.");
                console.error("Gruplar yüklenemedi", error);
            }
        };
        loadGroups();
    }, []);

    useEffect(() => {
        const loadGroupMembers = async () => {
            if (selectedGroupIds.length === 0) {
                dispatch({ type: 'SET_MEMBERS_AND_ORDERS', payload: [] });
                return;
            }

            setIsLoading(prev => ({ ...prev, members: true }));
            try {
                const memberPromises = selectedGroupIds.map(id => getActiveMembers(id));
                const results = await Promise.all(memberPromises);

                const allMembers = results.flat();

                const uniqueMembers = Array.from(new Map(allMembers.map(m => [m.customerId, m])).values());
                dispatch({ type: 'SET_MEMBERS_AND_ORDERS', payload: uniqueMembers });

            } catch (error) {
                toast.error("Grup üyeleri yüklenemedi.");
                console.error("Error loading group members:", error);
                dispatch({ type: 'SET_MEMBERS_AND_ORDERS', payload: [] });
            } finally {
                setIsLoading(prev => ({ ...prev, members: false }));
            }
        };
        loadGroupMembers();
    }, [selectedGroupIds]);

    const debouncedValidateLot = useMemo(() => debounce(async (accountId, lot) => {
            if (lot === 0) {
                dispatch({ type: 'SET_VALIDATION_STATUS', payload: { accountId, isValid: true, error: '' } });
                return;
            }

            const payload = {
                customerAccountId: accountId,
                bistCode: selectedAsset.bistCode,
                lotAmount: lot,
                transactionType: watchTransactionType,
                orderType: watchOrderType,
                limitPrice: watchOrderType === 'LIMIT' ? Number(watchLimitPrice) || 0 : 0
            };

            try {
                const res = await validateLot(payload);
                dispatch({ type: 'SET_VALIDATION_STATUS', payload: { accountId, isValid: res.valid, error: res.valid ? '' : res.message } });
            } catch (error) {
                dispatch({ type: 'SET_VALIDATION_STATUS', payload: { accountId, isValid: false, error: error.message || 'Doğrulama hatası' } });
            }
        }, 500), [selectedAsset.bistCode, watchTransactionType, watchOrderType, watchLimitPrice]);

    const handleLotChange = (accountId, lot) => {
        const typedLot = parseInt(lot, 10) || 0;
        dispatch({ type: 'UPDATE_LOT_MANUAL', payload: { accountId, lot: typedLot } });
        debouncedValidateLot(accountId, typedLot);
    };

    const onSubmit = async (formData) => {
        const ordersToSubmit = Object.values(customerOrders)
            .filter(order => order && order.lot > 0 && order.isValid && order.isSelected) 
            .map(order => ({ 
                customerAccountId: order.data.id, 
                lotAmount: order.lot 
            }));

        if (ordersToSubmit.length === 0) {
            toast.error("Gönderilecek geçerli emir bulunmuyor.");
            return;
        }

        let limitPriceToSend = 0;
        if (formData.orderType === 'LIMIT') {
            const lp = Number(formData.limitPrice);
            if (!lp || isNaN(lp) || lp <= 0) {
                toast.error('Limit emir için geçerli bir limit fiyat giriniz.');
                return;
            }
            limitPriceToSend = lp;
        } else {
            limitPriceToSend = 0;
        }

        const payload = {
            bistCode: selectedAsset.bistCode,
            transactionType: formData.transactionType,
            orderType: formData.orderType,
            limitPrice: limitPriceToSend,
            customerOrders: ordersToSubmit
        };

        try {
            const response = await createBulkOrder(payload);
            toast.success("Toplu emir talebi başarıyla gönderildi.");
            navigate(`/dashboard/orders?batchId=${response.batchId}`);
        } catch (error) {
            const data = error?.response?.data;
            const dataText = (data && typeof data === 'object') ? (data.message || data.error || '')
                              : (typeof data === 'string' ? data : '');
            const axiosMsg = error?.message || '';

            const explicitMaxHit = /maksimum değeri|aşıyor/i.test(dataText);
            const isWrappedProcessError = (data && data.isSuccess === false) ||
                (typeof data?.header === 'string' && /process\s*error/i.test(data.header));

            let toastMsg = "Emir gönderilirken bir hata oluştu.";
            if (explicitMaxHit || isWrappedProcessError) {
                toastMsg = "Gönderilen emir miktarı maksimum emir değerini aşıyor.";
            } else if (dataText) {
                toastMsg = dataText;
            } else if (axiosMsg) {
                toastMsg = axiosMsg;
            }
            toast.error(toastMsg);
        }
    };

    const validOrdersCount = Object.values(customerOrders).filter(order => order && order.isSelected && order.lot > 0 && order.isValid).length;
    const totalAccounts = Object.keys(customerOrders).length;

    return (
        <div style={styles.pageContainer}>

            <div style={styles.headerBar}>
                <button style={styles.backButton} onClick={onGoBack}>
                    ← Geri
                </button>
                <h1 style={styles.mainTitle}>Emir Verme Ekranı</h1>
                <div></div>
            </div>


            <div style={styles.layoutContainer}>
                <div style={styles.leftPanel}>
                    <div style={styles.card}>
                        <h2 style={styles.cardTitle}>Emir Girişi</h2>
                        <form onSubmit={handleSubmit(onSubmit)}>
                            <div style={styles.formGroup}>
                                <label style={styles.label}>İşlem Türü</label>
                                <select style={styles.select} {...register("transactionType")}>
                                    <option value="BUY">Alım</option>
                                    <option value="SELL">Satım</option>
                                </select>
                            </div>

                            <div style={styles.formGroup}>
                                <label style={styles.label}>Emir Türü</label>
                                <select style={styles.select} {...register("orderType")}>
                                    <option value="MARKET" disabled={!isMarketOpen}>Piyasa{!isMarketOpen ? ' (Kapalı)' : ''}</option>
                                    <option value="LIMIT">Limit</option>
                                </select>
                            </div>

                            {watchOrderType === 'LIMIT' && (
                                <div style={styles.formGroup}>
                                    <label style={styles.label}>Limit Fiyat</label>
                                    <input
                                        type="number"
                                        step={getTickForPrice(watchLimitPrice || currentPrice)}
                                        placeholder="Limit fiyat giriniz"
                                        style={styles.input}
                                        {...register("limitPrice")}
                                        onBlur={(e) => {
                                            const snapped = snapToTick(e.target.value);
                                            if (snapped !== Number(e.target.value)) {
                                                setValue('limitPrice', snapped, { shouldValidate: true, shouldDirty: true });
                                                toast.info(`Fiyat tick kuralına göre ${snapped.toFixed(getTickForPrice(snapped) >= 1 ? 1 : 2)} olarak ayarlandı.`);
                                            }
                                        }}
                                    />
                                </div>
                            )}

                            <div style={styles.formGroup}>
                                <label style={styles.label}>
                                    Yatırım Yüzdesi: %{watchPercentage}
                                </label>
                                <Controller
                                    name="percentage"
                                    control={control}
                                    render={({ field }) => (
                                        <input
                                            type="range"
                                            min="0"
                                            max="100"
                                            step="1"
                                            style={styles.percentageSlider}
                                            {...field}
                                        />
                                    )}
                                />
                                <button
                                    type="button"
                                    onClick={handleApplyPercentage}
                                    style={{ ...styles.button,
                                        width: '100%',
                                        marginTop: '10px',
                                        backgroundColor: '#c8102e', 
                                        padding: '14px 0',          
                                        fontSize: '16px',           
                                        fontWeight: '600',          
                                        border: 'none',
                                        borderRadius: '6px',
                                        color: '#fff',
                                        transition: 'all 0.2s ease' }}
                                >
                                    Yüzdeyi Uygula
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <div style={styles.rightPanel}>
                    <div style={styles.card}>
                        <h2 style={styles.cardTitle}>Hisse Bilgileri - {selectedAsset.bistCode}</h2>
                        <div style={styles.priceInfo}>
                            <span style={styles.priceLabel}>Güncel Fiyat:</span>
                            <span style={{
                                ...styles.priceValue,
                                ...getPriceColor(currentPrice, selectedAsset.previousClose)
                            }}>
                                {currentPrice ? `₺${currentPrice.toFixed(2)}` : 'Yükleniyor...'}
                            </span>
                        </div>

                        <div style={styles.priceInfo}>
                            <span style={styles.priceLabel}>Hisse Adı:</span>
                            <span style={{ color: '#343a40', fontSize: '14px', fontWeight: '500' }}>
                                {selectedAsset.companyName || selectedAsset.bistCode}
                            </span>
                        </div>

                        {selectedAsset.openPrice && (
                            <div style={styles.priceInfo}>
                                <span style={styles.priceLabel}>Açılış:</span>
                                <span style={{ ...styles.priceValue, color: '#343a40' }}>
                                    ₺{Number(selectedAsset.openPrice).toFixed(2)}
                                </span>
                            </div>
                        )}

                        {selectedAsset.dailyHigh && (
                            <div style={styles.priceInfo}>
                                <span style={styles.priceLabel}>Gün İçi Yüksek:</span>
                                <span style={{ ...styles.priceValue, color: '#343a40' }}>
                                    ₺{Number(selectedAsset.dailyHigh).toFixed(2)}
                                </span>
                            </div>
                        )}

                        {selectedAsset.dailyLow && (
                            <div style={styles.priceInfo}>
                                <span style={styles.priceLabel}>Gün İçi Düşük:</span>
                                <span style={{ ...styles.priceValue, color: '#343a40' }}>
                                    ₺{Number(selectedAsset.dailyLow).toFixed(2)}
                                </span>
                            </div>
                        )}

                        {selectedAsset.previousClose && (
                            <div style={styles.priceInfo}>
                                <span style={styles.priceLabel}>Önceki Kapanış:</span>
                                <span style={{ ...styles.priceValue, color: '#343a40' }}>
                                    ₺{Number(selectedAsset.previousClose).toFixed(2)}
                                </span>
                            </div>
                        )}

                        {selectedAsset.previousClose && currentPrice && (
                            <div style={styles.priceInfo}>
                                <span style={styles.priceLabel}>Günlük Değişim:</span>
                                <span style={{
                                    ...styles.priceValue,
                                    ...getPriceColor(currentPrice, selectedAsset.previousClose)
                                }}>
                                    {((currentPrice / selectedAsset.previousClose - 1) * 100).toFixed(2)}%
                                    {currentPrice > selectedAsset.previousClose ? ' ↑' :
                                     currentPrice < selectedAsset.previousClose ? ' ↓' : ' →'}
                                </span>
                            </div>
                        )}
                    </div>
                </div>
            </div>


            <div style={styles.customerListContainer}>
                <div style={styles.customerListHeader}>
                    <h2 style={styles.customerListTitle}>Grup ve Müşteri Seçimi</h2>
                    <div>
                        {validOrdersCount > 0 && (
                            <span style={{ color: '#28a745', fontWeight: '600' }}>
                                {validOrdersCount}/{totalAccounts} hesap seçili
                            </span>
                        )}
                    </div>
                </div>


                <div style={{ padding: '0 20px 15px' }}>
                    <div style={styles.groupTabs}>
                        {groups.map(group => (
                            <div
                                key={group.id}
                                onClick={() => handleGroupToggle(group.id)}
                                style={{
                                    ...styles.groupTab,
                                    ...(selectedGroupIds.includes(group.id)
                                        ? styles.groupTabActive
                                        : styles.groupTabInactive)
                                }}
                            >
                                {group.groupName}
                            </div>
                        ))}
                    </div>
                </div>


                {selectedGroupIds.length === 0 ? (
                    <div style={styles.noGroupSelected}>
                        Lütfen bir grup seçiniz
                    </div>
                ) : isLoading.members ? (
                    <div style={styles.noGroupSelected}>
                        Müşteriler yükleniyor...
                    </div>
                ) : state.members.length === 0 ? (
                    <div style={styles.noGroupSelected}>
                        Seçili grupta müşteri bulunmuyor
                    </div>
                ) : (
                    <div style={styles.tableContainer}>
                        <table style={styles.table}>
                            <thead>
                                <tr>
                                    <th style={styles.th}>
                                        <input type="checkbox" onChange={handleSelectAllCustomers} />
                                    </th>
                                    <th style={styles.th}>Müşteri Kodu</th>
                                    <th style={styles.th}>Müşteri Adı</th>
                                    <th style={styles.th}>Hesap No</th>
                                    <th style={styles.th}>Hesap Adı</th>
                                    <th style={styles.th}>
                                        {watch("transactionType") === 'BUY' ? 'Kullanılabilir Bakiye' : 'Kullanılabilir Lot'}
                                    </th>
                                    <th style={styles.th}>Lot Miktarı</th>
                                    <th style={styles.th}>Durum</th>
                                </tr>
                            </thead>
                            <tbody>
                                {state.members.map(member => {
                                    const assetCurrency = selectedAsset.currency;
                                    const transactionType = watch("transactionType");

                                    const hasAnyMatchingAccount = member.accounts.some(account => {
                                        if (transactionType === 'BUY') {
                                            return account.currency === assetCurrency;
                                        } else { 
                                            return account.assets && account.assets.some(asset => asset.bistCode === selectedAsset.bistCode);
                                        }
                                    });

                                    if (!hasAnyMatchingAccount) {
                                        return (
                                            <tr key={member.customerId} style={{ backgroundColor: '#f8f9fa', color: '#6c757d' }}>
                                                <td style={styles.td}><input type="checkbox" disabled /></td>
                                                <td style={styles.td}>{member.customerCode}</td>
                                                <td style={styles.td}>{member.fullName}</td>
                                                <td colSpan="5" style={styles.td}>
                                                    {transactionType === 'BUY' 
                                                        ? `Bu müşteri için uygun (${selectedAsset.currency || '...'}) hesap bulunmuyor.`
                                                        : `Bu müşteri ${selectedAsset.bistCode} hissesine sahip değil.`
                                                    }
                                                </td>
                                            </tr>
                                        );
                                    }

                                    return member.accounts.map(account => (
                                        <CustomerRow
                                            key={account.id}
                                            member={member}
                                            account={account}
                                            order={customerOrders[account.id]}
                                            selectedAsset={selectedAsset}
                                            transactionType={watchTransactionType}
                                            orderType={watchOrderType}
                                            limitPrice={watchLimitPrice}
                                            currentPrice={currentPrice}
                                            onSelectCustomer={handleSelectCustomer}
                                            onLotChange={handleLotChange}
                                            onUnlockLot={handleUnlockLot}
                                            onShowSettlementInfo={handleShowSettlementInfo}
                                            styles={styles}
                                        />
                                    ));
                                })}
                            </tbody>
                        </table>
                    </div>
                )}


                <div style={{ padding: '20px', borderTop: '1px solid #e9ecef' }}>
                    <button
                        onClick={handleSubmit(onSubmit)}
                        style={{
                            ...styles.submitButton,
                            ...(validOrdersCount === 0 ? styles.submitButtonDisabled : {})
                        }}
                        disabled={validOrdersCount === 0}
                    >
                        {validOrdersCount} Hesap için Emir Gönder
                    </button>
                </div>
            </div>
            {settlementModal.open && (
                <div style={styles.modalOverlay} onClick={() => setSettlementModal({ open: false, message: '' })}>
                    <div style={styles.modalContent} onClick={(e) => e.stopPropagation()}>
                        <div style={styles.modalTitle}>T+2 Takas Bilgisi</div>
                        <div style={styles.modalMessage}>{settlementModal.message}</div>
                        <div style={styles.modalFooter}>
                            <button style={styles.modalButton} onClick={() => setSettlementModal({ open: false, message: '' })}>
                                Tamam
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}