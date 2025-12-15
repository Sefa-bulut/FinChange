package com.example.finchange.portfoliogroup.model;

import com.example.finchange.customer.model.Customers;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "PortfoyGrupUyeleri")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PortfolioGroupMemberId.class)
public class PortfolioGroupMember {

     @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private PortfolioGroup portfolioGroup;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customers customer;
}