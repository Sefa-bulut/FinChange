package com.example.finchange.operation.model;


import com.example.finchange.common.model.AuditableBaseEntity;
import com.example.finchange.operation.model.enums.HolidayType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "Tatiller")
public class Holiday extends AuditableBaseEntity {


    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate; // Sadece tarih bilgisi, saat yok.

    @Enumerated(EnumType.STRING) // Enum'ı veritabanında "RESMI_TATIL" gibi metin olarak saklar. Bu en güvenli yöntemdir.
    @Column(name = "type", nullable = false)
    private HolidayType type;

    @Column(name = "description", nullable = false, length = 255)
    private String description;
}
