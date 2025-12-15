import React from 'react';
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';


export default function ProfitLineChart({ data, trades = [] }) {
    let series =
        Array.isArray(data) &&
        data.length > 0 &&
        'date' in (data[0] || {}) &&
        'profit' in (data[0] || {})
            ? data
            : null;

    if (!series) {
        const getTime = (t) =>
            t.sellTime || t.buyTime || t.executionTimestamp || t.timestamp || t.time;

        const getNet = (t) => {
            const explicit = t.netProfit ?? t.net;
            if (explicit != null) return Number(explicit) || 0;
            return Number(t.profit ?? 0) - Number(t.commission ?? t.commissionFee ?? 0);
        };

        let cumulative = 0;
        series = [...(trades || [])]
            .filter((t) => getTime(t))
            .sort((a, b) => new Date(getTime(a)) - new Date(getTime(b)))
            .map((t) => {
                cumulative += getNet(t);
                return {
                    date: new Date(getTime(t)).toLocaleDateString('tr-TR'),
                    profit: Number(cumulative.toFixed(2)),
                };
            });
    }

    if (!series || series.length === 0) {
        return <p style={{ color: '#6c757d', fontStyle: 'italic' }}>📉 Bu aralıkta işlem yok.</p>;
    }

    const allZero = series.every((p) => Number(p.profit) === 0);
    if (allZero) {
        return <p style={{ color: '#6c757d', fontStyle: 'italic' }}>📉 Bu aralıkta anlamlı kar-zarar verisi yok.</p>;
    }

    return (
        <ResponsiveContainer width="100%" height={300}>
            <LineChart data={series}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="profit" strokeWidth={2} />
            </LineChart>
        </ResponsiveContainer>
    );
}
