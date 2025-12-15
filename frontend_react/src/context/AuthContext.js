import React, { createContext, useContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
// GÜNCELLENDİ: Hem login hem de logout fonksiyonlarını servisimizden import ediyoruz.
import { login as loginService, logout as logoutService } from '../services/authService';

const AuthContext = createContext(null);

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [permissions, setPermissions] = useState([]);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    // Sayfa ilk yüklendiğinde token'ı kontrol etme (Bu kısım doğru ve değişmiyor)
    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            try {
                const decoded = jwtDecode(token);
                if (decoded.exp * 1000 > Date.now()) {
                    setUser({ email: decoded.userMail, name: decoded.userFirstName });
                    setPermissions(decoded.userPermissions || []);
                    setIsLoggedIn(true);
                } else {
                    // Token süresi dolmuşsa temizle
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('refreshToken');
                }
            } catch (error) {
                console.error("Token decode hatası:", error);
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
            }
        }
        setIsLoading(false);
    }, []);

    // === LOGIN FONKSİYONU GÜNCELLENDİ ===
    const login = async (email, password) => {
        // Servisimiz artık doğrudan 'result' nesnesini veya hata durumunda null/undefined döndürecek.
        const loginData = await loginService(email, password);

        // Gelen verinin geçerli olup olmadığını kontrol ediyoruz.
        // Artık 'isSuccess' veya 'result' gibi alanlara bakmamıza gerek yok.
        if (!loginData || !loginData.token) {
            // Hata, zaten interceptor'da formatlandığı için,
            // loginData'nın boş gelmesi bir hata olduğunu gösterir.
            // Servis hata fırlatacağı için bu blok genellikle beklenmedik durumlar içindir.
            throw new Error('Giriş başarısız oldu veya sunucudan geçersiz yanıt alındı.');
        }

        const { token, mustChangePassword } = loginData;
        
        // Token'ları localStorage'a kaydet
        localStorage.setItem('accessToken', token.accessToken);
        localStorage.setItem('refreshToken', token.refreshToken);
        
        // Token'ı decode edip state'leri güncelle
        const decoded = jwtDecode(token.accessToken);
        setUser({ email: decoded.userMail, name: decoded.userFirstName });
        setPermissions(decoded.userPermissions || []);
        setIsLoggedIn(true);

        // Component'e şifre değiştirme zorunluluğu bilgisini döndür
        return { mustChangePassword };
    };

    // Logout fonksiyonu zaten güncel servisi kullandığı için doğru çalışıyor.
    const logout = () => {
        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');

        if (accessToken && refreshToken) {
            // Backend'e çıkış isteği gönder (hata fırlatmaz)
            logoutService(accessToken, refreshToken);
        }
        
        // Client-side (tarayıcı) tarafını temizle
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        setUser(null);
        setPermissions([]);
        setIsLoggedIn(false);
        // Login sayfasına yönlendirme, api.js'deki interceptor tarafından zaten yapılıyor.
    };

    const value = { isLoggedIn, user, permissions, login, logout, isLoading };

    return (
        <AuthContext.Provider value={value}>
            {/* isLoading false olana kadar çocukları render etme, böylece ilk yüklemede yetki kontrolü tamamlanmış olur. */}
            {!isLoading && children}
        </AuthContext.Provider>
    );
};