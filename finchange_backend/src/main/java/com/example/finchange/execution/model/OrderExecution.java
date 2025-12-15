package com.example.finchange.execution.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "order")
@Entity
@Table(name = "EmirGerceklesmeleri")
public class OrderExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "executed_lot_amount", nullable = false)
    private int executedLotAmount;

    @Column(name = "executed_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal executedPrice;

    @Column(name = "locked_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal lockedPrice;

    @Column(name = "execution_timestamp", nullable = false)
    private LocalDateTime executionTimestamp;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "is_settled", nullable = false)
    private boolean isSettled;

    @Column(name = "commission_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal commissionAmount;

}

