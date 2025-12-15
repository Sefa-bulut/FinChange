# AuthController Unit Testleri

Bu proje AuthController için kapsamlı unit testler içermektedir.

## Test Dosyaları

### 1. AuthControllerTest.java
- **Konum**: `src/test/java/com/example/finchange/auth/controller/AuthControllerTest.java`
- **Açıklama**: Standalone unit testler - MockMvc ile controller testleri
- **Kapsam**: Tüm endpoint'ler için başarılı ve başarısız senaryolar

### 2. AuthControllerIntegrationTest.java
- **Konum**: `src/test/java/com/example/finchange/auth/controller/AuthControllerIntegrationTest.java`
- **Açıklama**: Integration testler - Spring context ile testler
- **Kapsam**: Temel endpoint'ler için başarılı senaryolar

## Test Senaryoları

### Login Endpoint (`/api/v1/auth/login`)
- ✅ Başarılı giriş
- ❌ Geçersiz email formatı
- ❌ Boş email
- ❌ Kısa şifre
- ❌ Null request body
- ❌ Geçersiz JSON formatı

### Force Change Password Endpoint (`/api/v1/auth/force-change-password`)
- ✅ Başarılı şifre değiştirme
- ❌ Geçersiz request

### Forgot Password Endpoint (`/api/v1/auth/forgot-password`)
- ✅ Başarılı şifre sıfırlama kodu gönderme
- ❌ Geçersiz email

### Reset Password Endpoint (`/api/v1/auth/reset-password`)
- ✅ Başarılı şifre sıfırlama
- ❌ Geçersiz request

### Refresh Token Endpoint (`/api/v1/auth/refresh`)
- ✅ Başarılı token yenileme
- ❌ Geçersiz request

### Logout Endpoint (`/api/v1/auth/logout`)
- ✅ Başarılı çıkış
- ❌ Geçersiz request

## Test Çalıştırma

### Tüm Testleri Çalıştırma
```bash
# Proje dizininde
mvn test
```

### Sadece AuthController Testlerini Çalıştırma
```bash
# Unit testler
mvn test -Dtest=AuthControllerTest

# Integration testler
mvn test -Dtest=AuthControllerIntegrationTest

# Belirli bir test metodu
mvn test -Dtest=AuthControllerTest#login_Success
```

### IDE'den Çalıştırma
1. IntelliJ IDEA: Test sınıfını açın ve yeşil "Run" butonuna tıklayın
2. Eclipse: Test sınıfını sağ tıklayın ve "Run As" > "JUnit Test" seçin

## Test Konfigürasyonu

### Test Properties
- **Dosya**: `src/test/resources/application-test.properties`
- **Veritabanı**: H2 in-memory database
- **JWT**: RSA key pair (otomatik oluşturulur)
- **Mail**: Test mail server konfigürasyonu

### JWT Key Pair Yönetimi
- **Otomatik Oluşturma**: Test çalıştırıldığında RSA key pair otomatik oluşturulur
- **Konum**: `src/test/resources/keys/`
- **Dosyalar**: `test-private-key.pem`, `test-public-key.pem`
- **Utility**: `TestKeyGenerator` sınıfı ile yönetilir

### Bağımlılıklar
- Spring Boot Test Starter
- Spring Security Test
- H2 Database (test scope)
- Mockito
- JUnit 5

## Test Sonuçları

### Başarılı Test Çıktısı
```
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

### Test Raporu
Test sonuçları `target/surefire-reports/` dizininde HTML formatında oluşturulur.

## Test Coverage

Bu testler şu alanları kapsar:
- ✅ HTTP status kodları
- ✅ Response body içeriği
- ✅ Validation hataları
- ✅ Service method çağrıları
- ✅ Exception handling

## Sorun Giderme

### Yaygın Hatalar

1. **Port çakışması**: Test portları kullanımda olabilir
   ```bash
   # Kullanılan portları kontrol et
   netstat -ano | findstr :8080
   ```

2. **Bağımlılık eksikliği**: Maven dependencies'i yeniden indir
   ```bash
   mvn clean install
   ```

3. **Test context yüklenemiyor**: Application context'i kontrol et
   ```bash
   mvn test -X
   ```

### Debug Modu
```bash
# Debug logları ile test çalıştır
mvn test -Dspring.profiles.active=test -Dlogging.level.com.example.finchange=DEBUG
```

## Katkıda Bulunma

Yeni test eklerken:
1. Test metodunu uygun test sınıfına ekleyin
2. Test adını açıklayıcı yapın
3. Given-When-Then formatını kullanın
4. Mock'ları doğru şekilde ayarlayın
5. Assertion'ları kapsamlı yapın

## Notlar

- Testler Türkçe mesajlar kullanır
- Validation hataları GlobalExceptionHandler tarafından yakalanır
- Mock'lar AuthenticationService için kullanılır
- Test verileri gerçek verilerle uyumludur
