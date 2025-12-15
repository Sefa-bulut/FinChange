package com.example.finchange.user.model;

import com.example.finchange.common.model.AuditableBaseEntity; // Ortak ID, created_at, updated_at için
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Kullanicilar")
public class User extends AuditableBaseEntity {

    @Column(name = "ad", nullable = false, length = 50)
    private String firstName;

    @Column(name = "soyad", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    public boolean isActive() {
        return this.isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = true;

    @Column(name = "personel_kodu", nullable = false, unique = true, length = 50)
    private String personnelCode;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "KullaniciRolleri",
            joinColumns = @JoinColumn(name = "kullanici_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Role> roles;
}