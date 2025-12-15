package com.example.finchange.execution.events;

import java.util.List;

public class OrdersCreatedEvent {
    private final List<Integer> createdOrderIds;

    public OrdersCreatedEvent(List<Integer> createdOrderIds) {
        this.createdOrderIds = createdOrderIds;
    }

    public List<Integer> getCreatedOrderIds() {
        return createdOrderIds;
    }
}
