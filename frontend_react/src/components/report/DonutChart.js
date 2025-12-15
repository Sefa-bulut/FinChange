import React, { useState, useMemo } from 'react';
import { PieChart, Pie, Cell, Legend, Tooltip, ResponsiveContainer } from 'recharts';

const COLORS = ['#8884d8', '#82ca9d', '#ffc658', '#ff7f50', '#00c49f', '#ffbb28'];

export default function DonutChart({ data, stocksData }) {
    const [chartIndex, setChartIndex] = useState(0);

    const charts = useMemo(() => {
        const arr = [];
        if (data && data.length) arr.push({ key: 'currency', title: 'Varlık Dağılımı (USD / TL)', data });
        if (stocksData && stocksData.length) arr.push({ key: 'stocks', title: 'Alınan Hisseler', data: stocksData });
        return arr;
    }, [data, stocksData]);

    const currentChart = charts[chartIndex];

    const prevChart = () => setChartIndex((i) => (i - 1 + charts.length) % charts.length);
    const nextChart = () => setChartIndex((i) => (i + 1) % charts.length);

    if (!currentChart) return <p>Veri bulunamadı.</p>;

    const isStocks = currentChart.key === 'stocks';

    return (
        <div style={{ textAlign: 'center' }}>
            <h4>{currentChart.title}</h4>
            <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                    <Pie
                        data={currentChart.data}
                        dataKey="value"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        innerRadius={60}
                        outerRadius={90}
                        fill="#8884d8"
                    >
                        {currentChart.data.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                    </Pie>
                    <Tooltip
                        formatter={(value, name) =>
                            isStocks ? [`${Number(value).toLocaleString('tr-TR')} lot`, name]
                                : [`${Number(value).toLocaleString('tr-TR')}`, name]
                        }
                    />
                    <Legend />
                </PieChart>
            </ResponsiveContainer>

            {charts.length > 1 && (
                <div style={{ marginTop: '10px', display: 'flex', justifyContent: 'center', gap: '10px' }}>
                    <button onClick={prevChart} style={{ padding: '5px 10px' }}>{'<'}</button>
                    <button onClick={nextChart} style={{ padding: '5px 10px' }}>{'>'}</button>
                </div>
            )}
        </div>
    );
}
