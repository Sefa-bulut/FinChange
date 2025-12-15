package com.example.finchange.execution.service;

import com.example.finchange.execution.dto.BulkOrderRequest;
import com.example.finchange.execution.dto.OrderResponseDto; 
import com.example.finchange.execution.dto.UpdateOrderRequest;
import com.example.finchange.execution.dto.ValidateLotRequest;
import com.example.finchange.execution.dto.ValidateLotResponse;
import com.example.finchange.execution.model.Order;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 
import java.time.LocalDate; 
import java.util.List;

public interface OrderService {
    String createBulkOrder(BulkOrderRequest request);
    ValidateLotResponse validateLotForCustomer(ValidateLotRequest request);

    void cancelOrder(Integer orderId);

    Page<OrderResponseDto> getOrders(
            String batchId,
            Integer customerId,
            Integer assetId,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    void updateOrder(Integer orderId, UpdateOrderRequest request);

    void tryToMatchOrdersImmediately(List<Order> orders);
}