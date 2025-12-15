import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let stompClient = null;
let connectionPromise = null;

const channels = {};
const pendingSubs = {};

const connect = () => {
    if (stompClient && stompClient.active) {
        return Promise.resolve(stompClient);
    }
    if (connectionPromise) {
        return connectionPromise;
    }
    const token = localStorage.getItem('accessToken');

    connectionPromise = new Promise((resolve, reject) => {
        stompClient = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
            reconnectDelay: 5000,
            debug: (str) => console.log('STOMP DEBUG: ', str),
            onConnect: () => {
                console.log('WebSocket Bağlantısı Başarıyla Kuruldu!');
                resolve(stompClient);
                connectionPromise = null;
            },
            onStompError: (frame) => {
                const errorMessage = frame.headers['message'];
                console.error(`Broker hatası: ${errorMessage}`);
                if (errorMessage && (errorMessage.includes('Geçersiz token') || errorMessage.includes('Authorization'))) {
                    console.warn('WebSocket token hatası: Misafir bağlantı ile devam edilebilir.');
                }
                reject(frame);
                connectionPromise = null;
            },
        });

        stompClient.activate();
    });

    return connectionPromise;
};

export const subscribe = (destination, callback) => {
    if (!channels[destination]) {
        channels[destination] = { stompSub: null, listeners: new Set(), pending: true };
    }
    const channel = channels[destination];
    channel.listeners.add(callback);

    if (channel.stompSub && !channel.pending) {
        return {
            unsubscribe: () => {
                const ch = channels[destination];
                if (!ch) return;
                ch.listeners.delete(callback);
                if (ch.listeners.size === 0 && ch.stompSub) {
                    try { ch.stompSub.unsubscribe(); } catch (e) { /* noop */ }
                    delete channels[destination];
                    console.log(`'${destination}' aboneliği (STOMP) kapatıldı (dinleyici kalmadı).`);
                }
            }
        };
    }

    if (!pendingSubs[destination]) {
        pendingSubs[destination] = connect()
            .then(client => {
                const ch = channels[destination];
                if (!ch) return;

                if (ch.stompSub) return;

                const stompSub = client.subscribe(destination, (message) => {
                    const data = JSON.parse(message.body);
                    const listenersSnapshot = Array.from(channels[destination]?.listeners || []);
                    for (const fn of listenersSnapshot) {
                        try { fn(data); } catch (err) { console.error(`'${destination}' listener hatası:`, err); }
                    }
                });
                ch.stompSub = stompSub;
                ch.pending = false;
            })
            .catch(err => {
                console.error(`'${destination}' aboneliği sırasında hata:`, err);
            })
            .finally(() => {
                delete pendingSubs[destination];
            });
    }

    return {
        unsubscribe: () => {
            const ch = channels[destination];
            if (!ch) return;
            ch.listeners.delete(callback);
            if (ch.listeners.size === 0 && ch.stompSub && !ch.pending) {
                try { ch.stompSub.unsubscribe(); } catch (e) { /* noop */ }
                delete channels[destination];
                console.log(`'${destination}' aboneliği (STOMP) kapatıldı (dinleyici kalmadı).`);
            }
        }
    };
};

export const disconnect = () => {
    Object.keys(channels).forEach(dest => {
        const ch = channels[dest];
        try { ch?.stompSub?.unsubscribe(); } catch (e) { /* noop */ }
        delete channels[dest];
    });
    if (stompClient && stompClient.active) {
        stompClient.deactivate();
        stompClient = null;
        console.log('WebSocket bağlantısı manuel olarak sonlandırıldı.');
    }
};