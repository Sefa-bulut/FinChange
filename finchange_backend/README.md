finchange_backend/
├── src/
│   └── main/
│       └── java/
│           ├── auth/
│           │   ├── config/            # Kimlik doğrulama yapılandırmaları
│           │   ├── exception/         # Auth ile ilgili özel istisnalar
│           │   ├── model/             # Kullanıcı kimlik doğrulama modelleri
│           │   ├── repository/        # Auth veri erişim katmanı
│           │   ├── service/           # Auth iş kuralları
│           │   └── util/              # Yardımcı yardımcı sınıflar
│           ├── common/
│           │   ├── exception/         # Ortak istisna sınıfları
│           │   └── model/             # Paylaşılan temel modeller
│           ├── customer/
│           │   ├── controller/        # Müşteri API uç noktaları
│           │   ├── exception/         # Müşteriye özel hata yönetimi
│           │   ├── model/             # Müşteri varlık sınıfları
│           │   ├── repository/        # Müşteri veri erişimi
│           │   └── service/           # Müşteri işlemleri servisi
│           ├── user/
│           │   ├── controller/        # Kullanıcı yönetimi API'leri
│           │   ├── exception/         # Kullanıcıya özel istisnalar
│           │   ├── model/             # Kullanıcı varlık ve DTO sınıfları
│           │   ├── repository/        # Kullanıcı veri deposu
│           │   └── service/           # Kullanıcı işlemleri servisi
│           └── FinchangeApplication.java  # Spring Boot ana uygulama sınıfı
├── README.md                          # Proje açıklaması
└── pom.xml                            # Maven bağımlılık ve yapılandırma dosyası