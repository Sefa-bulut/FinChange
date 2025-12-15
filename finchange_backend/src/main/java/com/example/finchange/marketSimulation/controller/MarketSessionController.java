package com.example.finchange.marketSimulation.controller;

import com.example.finchange.common.model.dto.response.SuccessResponse;
import com.example.finchange.marketSimulation.service.MarketSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.finchange.execution.service.impl.QueuedOrderProcessor;
import com.example.finchange.portfolio.service.PortfolioService;

@RestController
@RequestMapping("/api/v1/market-session")
@RequiredArgsConstructor
public class MarketSessionController {

    private final MarketSessionService marketSessionService;
    private final StringRedisTemplate redisTemplate;
    private final QueuedOrderProcessor queuedOrderProcessor;
    private final PortfolioService portfolioService;

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Map<String, Object>> getStatus() {
        boolean isOpen = marketSessionService.isMarketOpenNow();
        String sim = safeGet("market:override:simulation");
        String trading = safeGet("market:override:trading");
        String settlementControls = safeGet("market:override:settlement_controls");
        boolean simulationOverride = "true".equalsIgnoreCase(sim);
        boolean simulationActive = isOpen || simulationOverride; 
        return SuccessResponse.success(Map.of(
                "open", isOpen,
                "overrideSimulation", simulationOverride,
                "overrideTrading", "true".equalsIgnoreCase(trading),
                "simulationActive", simulationActive,
                "settlementControlsActive", !"false".equalsIgnoreCase(settlementControls) // default: true
        ));
    }

    @PostMapping("/override/simulation/open")
    @PreAuthorize("hasAuthority('order:create')")
    public SuccessResponse<Map<String, Object>> openSimulationOverride() {
        redisTemplate.opsForValue().set("market:override:simulation", "true");
        return SuccessResponse.success(Map.of("overrideSimulation", true));
    }

    @PostMapping("/override/simulation/close")
    @PreAuthorize("hasAuthority('order:create')")
    public SuccessResponse<Map<String, Object>> closeSimulationOverride() {
        redisTemplate.opsForValue().set("market:override:simulation", "false");
        return SuccessResponse.success(Map.of("overrideSimulation", false));
    }

    @PostMapping("/override/trading/open")
    @PreAuthorize("hasAuthority('order:create')")
    public SuccessResponse<Map<String, Object>> openTradingOverride() {
        redisTemplate.opsForValue().set("market:override:trading", "true");
        queuedOrderProcessor.processQueuedOrders();
        return SuccessResponse.success(Map.of("overrideTrading", true));
    }

    @PostMapping("/override/trading/close")
    @PreAuthorize("hasAuthority('order:create')")
    public SuccessResponse<Map<String, Object>> closeTradingOverride() {
        redisTemplate.opsForValue().set("market:override:trading", "false");
        return SuccessResponse.success(Map.of("overrideTrading", false));
    }

    @PostMapping("/override/settlement/open")
    @PreAuthorize("hasAuthority('order:create')")
    public SuccessResponse<Map<String, Object>> openSettlementControls() {
        redisTemplate.opsForValue().set("market:override:settlement_controls", "true");
        return SuccessResponse.success(Map.of("settlementControlsActive", true));
    }

    @PostMapping("/override/settlement/close")
    @PreAuthorize("hasAuthority('order:create')")
    public SuccessResponse<Map<String, Object>> closeSettlementControls() {
        redisTemplate.opsForValue().set("market:override:settlement_controls", "false");
        try {
            portfolioService.releaseAllBlocksForOverride();
        } catch (Exception e) {
        }
        return SuccessResponse.success(Map.of("settlementControlsActive", false));
    }

    private String safeGet(String key) {
        try { return redisTemplate.opsForValue().get(key); } catch (Exception e) { return null; }
    }
}


