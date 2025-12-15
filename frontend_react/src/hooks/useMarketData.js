import { useState, useEffect } from 'react';
import { subscribe } from '../services/websocketService';

export const useMarketData = (assetCode) => {
    const [data, setData] = useState(null);

    useEffect(() => {
        if (!assetCode) {
            setData(null);
            return; 
        }

        const destination = `/topic/prices/${assetCode}`;
        
        // WebSocket kanalına abone ol ve gelen her mesajda state'i güncelle
        const subscription = subscribe(destination, (priceUpdate) => {
            setData(priceUpdate);
        });

        // Bu hook'u kullanan component ekrandan kaldırıldığında aboneliği sonlandır
        return () => {
            if (subscription && subscription.unsubscribe) {
                subscription.unsubscribe();
            }
        };
    }, [assetCode]); 

    return data;
};