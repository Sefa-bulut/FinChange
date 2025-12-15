package com.example.finchange.brokerage.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.finchange.brokerage.model.BrokerageFirm;

public interface BrokerageFirmService {
    BigDecimal getActiveCommissionRate();

    List<BrokerageFirm> findAll();

    BrokerageFirm getById(Integer id);

    BrokerageFirm create(BrokerageFirm firm);

    BrokerageFirm update(Integer id, BrokerageFirm firm);

    void delete(Integer id);

    BrokerageFirm activate(Integer id);
}