// com/example/finchange/customer/model/SuitabilityProfiles.java
package com.example.finchange.customer.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "YerindelikProfilleri")
public class SuitabilityProfiles extends AuditableBaseEntity {

    // BaseIdEntity'den gelen @Id'yi tekrar tanımlamaya gerek yok.

    @ManyToOne(fetch = FetchType.LAZY) // LAZY: Sadece ihtiyaç duyulduğunda customer bilgisini yükle
    @JoinColumn(name = "musteri_id", nullable = false) // Veritabanındaki 'musteri_id' kolonuna bağlan
    private Customers customer;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;



    // Gerçek Kişi Soruları
    @Column(name = "yatirim_amaci", length = 255)
    private String yatirimAmaci;

    @Column(name = "yatirim_suresi", length = 255)
    private String yatirimSuresi;

    @Column(name = "risk_toleransi", length = 255)
    private String riskToleransi;

    @Column(name = "mali_durum", length = 255)
    private String maliDurum;

    @Column(name = "yatirim_deneyimi", length = 255)
    private String yatirimDeneyimi;

    @Column(name = "likidite_ihtiyaci", length = 255)
    private String likiditeIhtiyaci;

    @Column(name = "vergi_durumu", length = 255)
    private String vergiDurumu;

    // Tüzel Kişi Soruları
    @Column(name = "sirket_yatirim_stratejisi", columnDefinition = "NVARCHAR(MAX)")
    private String sirketYatirimStratejisi;

    @Column(name = "risk_yonetimi_politikasi", columnDefinition = "NVARCHAR(MAX)")
    private String riskYonetimiPolitikasi;

    @Column(name = "finansal_durum_tuzel", columnDefinition = "NVARCHAR(MAX)")
    private String finansalDurumTuzel;

    @Column(name = "yatirim_suresi_vade_tuzel", columnDefinition = "NVARCHAR(MAX)")
    private String yatirimSuresiVadeTuzel;
}