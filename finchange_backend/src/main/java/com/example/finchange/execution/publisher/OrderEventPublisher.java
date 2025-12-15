package com.example.finchange.execution.publisher;

import com.example.finchange.execution.dto.event.OrderCancelledEvent;
import com.example.finchange.execution.dto.event.OrderExecutedEvent;

public interface OrderEventPublisher {

    void publishOrderExecutedEvent(OrderExecutedEvent event);
    void publishOrderCancelledEvent(OrderCancelledEvent event); 
}