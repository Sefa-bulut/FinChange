package com.example.finchange.customer.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "MusteriBelgeleri")
public class CustomerDocuments extends AuditableBaseEntity {

    @Column(name = "musteri_id", nullable = false)
    private Integer clientId;

    @Column(name = "yukleyen_kullanici_id", nullable = false)
    private Integer uploaderUserId;

    @Column(name = "belge_tipi", nullable = false, length = 100)
    private String documentType;

    @Column(name = "dosya_yolu", nullable = false)
    private String filePath;


}