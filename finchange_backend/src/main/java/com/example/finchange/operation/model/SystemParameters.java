package com.example.finchange.operation.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SistemParametreleri", schema = "dbo")
public class SystemParameters extends AuditableBaseEntity {

    @Column(name = "system_time", nullable = false)
    private LocalDateTime systemTime;

    @Column(name = "time_zone", nullable = false, length = 50)
    private String timeZone;

    @Column(name = "description", length = 255)
    private String description;

    @PrePersist
    public void prePersist() {
        if (this.systemTime == null) {
            this.systemTime = ZonedDateTime.now(ZoneId.of("Europe/Istanbul")).toLocalDateTime();
        }
        if (this.timeZone == null) {
            this.timeZone = ZonedDateTime.now(ZoneId.of("Europe/Istanbul"))
                    .getOffset()
                    .getId(); // Örn: "+03:00"
        }
    }
}
