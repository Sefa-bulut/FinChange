package com.example.finchange.portfoliogroup.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PortfolioGroupMemberId implements Serializable {
    private Integer portfolioGroup;
    private Integer customer;
}