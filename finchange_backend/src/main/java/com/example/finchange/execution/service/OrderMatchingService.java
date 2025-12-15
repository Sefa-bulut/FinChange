package com.example.finchange.execution.service;

import com.example.finchange.marketSimulation.kafka.PriceUpdateEvent;

public interface OrderMatchingService {

    void handlePriceUpdate(PriceUpdateEvent message);
}