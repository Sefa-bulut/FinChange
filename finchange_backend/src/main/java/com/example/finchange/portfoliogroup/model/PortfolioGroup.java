package com.example.finchange.portfoliogroup.model;

import com.example.finchange.common.model.AuditableBaseEntity;
import com.example.finchange.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "PortfoyGruplari")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioGroup extends AuditableBaseEntity {

    @Column(name = "group_name", nullable = false, length = 100) 
    private String groupName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false) 
    private User owner;

    @Column(name = "status", nullable = false, length = 20) 
    private String status;
}