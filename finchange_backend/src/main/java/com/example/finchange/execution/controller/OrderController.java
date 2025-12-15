package com.example.finchange.execution.controller;


import com.example.finchange.execution.dto.OrderResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.execution.dto.BulkOrderRequest;
import com.example.finchange.execution.dto.UpdateOrderRequest;
import com.example.finchange.execution.dto.ValidateLotRequest;
import com.example.finchange.execution.dto.ValidateLotResponse;
import com.example.finchange.execution.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<Map<String, String>>> createBulkOrder(@RequestBody @Valid BulkOrderRequest request) {
        String batchId = orderService.createBulkOrder(request);
        Map<String, String> response = Map.of("batchId", batchId);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(SuccessResponse.success(response, "Toplu emir talebi başarıyla alındı ve işleme konuldu."));
    }

    @PostMapping("/validate-lot")
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<SuccessResponse<ValidateLotResponse>> validateLot(@RequestBody @Valid ValidateLotRequest request) {
        ValidateLotResponse response = orderService.validateLotForCustomer(request);
        return ResponseEntity.ok(SuccessResponse.success(response));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('order:create')") 
    public ResponseEntity<SuccessResponse<Void>> cancelOrder(@PathVariable Integer orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(SuccessResponse.success("Emir başarıyla iptal edildi."));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('order:read:all') or hasAuthority('order:read:own')")
    public ResponseEntity<SuccessResponse<Page<OrderResponseDto>>> getOrders(
            @RequestParam(required = false) String batchId,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer assetId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "createdAt,desc") Pageable pageable
    ) {
        Page<OrderResponseDto> orders = orderService.getOrders(batchId, customerId, assetId, status, startDate, endDate, pageable);
        return ResponseEntity.ok(SuccessResponse.success(orders, "Emirler listelendi."));
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAuthority('order:create')") 
    public ResponseEntity<SuccessResponse<Void>> updateOrder(@PathVariable Integer orderId, @RequestBody @Valid UpdateOrderRequest request) {
        orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(SuccessResponse.success("Emir başarıyla güncellendi."));
    }
}