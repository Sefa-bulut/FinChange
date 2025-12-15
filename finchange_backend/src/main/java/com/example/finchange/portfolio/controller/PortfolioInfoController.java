package com.example.finchange.portfolio.controller;

import com.example.finchange.execution.util.BusinessDayCalculator;
import com.example.finchange.portfolio.dto.SettlementInfoResponse;
import com.example.finchange.portfolio.model.CustomerAsset;
import com.example.finchange.portfolio.repository.AssetRepository;
import com.example.finchange.portfolio.repository.CustomerAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioInfoController {

    private final CustomerAssetRepository customerAssetRepository;
    private final BusinessDayCalculator businessDayCalculator;
    private final StringRedisTemplate redisTemplate;
    private final AssetRepository assetRepository;

    @GetMapping("/customers/{customerId}/assets/{assetId}/settlement-info")
    @PreAuthorize("hasAuthority('client:read:all') or hasAuthority('order:create')")
    public ResponseEntity<SettlementInfoResponse> getSettlementInfo(
            @PathVariable Integer customerId,
            @PathVariable Integer assetId
    ) {
        CustomerAsset asset = customerAssetRepository.findByCustomerIdAndAssetId(customerId, assetId)
                .orElseGet(() -> {
                    CustomerAsset ca = new CustomerAsset();
                    ca.setCustomerId(customerId);
                    ca.setAssetId(assetId);
                    ca.setTotalLot(0);
                    ca.setBlockedLot(0);
                    return ca;
                });

        int total = asset.getTotalLot();
        int blocked = asset.getBlockedLot();
        int available = Math.max(0, total - blocked);

        boolean settlementControlsActive = true;
        try {
            String val = redisTemplate.opsForValue().get("market:override:settlement_controls");
            if ("false".equalsIgnoreCase(val)) {
                settlementControlsActive = false;
            }
        } catch (Exception ignore) {}

        int settlementDays = assetRepository.findById(assetId)
                .map(a -> a.getSettlementDays() == null ? 2 : a.getSettlementDays())
                .orElse(2);
        LocalDate tPlusN = businessDayCalculator.getBusinessDayAfter(LocalDate.now(), settlementDays);
        LocalDateTime unlockAt = tPlusN.atTime(9, 0);

        String message;
        if (blocked > 0 && settlementControlsActive) {
            message = String.format("Kullanıcı %d lota sahip, ancak takas kuralları nedeniyle %s tarihinde saat 09:00 itibarıyla takas süresi dolacaktır.",
                    total,
                    unlockAt.toLocalDate());
        } else if (blocked > 0) {
            message = String.format("Takas kontrolleri kapalı. Blokedeki %d lot hemen kullanılabilir.", blocked);
        } else {
            message = "Bloke bulunan lot yok. Tüm lotlar kullanılabilir.";
        }

        SettlementInfoResponse resp = SettlementInfoResponse.builder()
                .customerId(customerId)
                .assetId(assetId)
                .totalLot(total)
                .blockedLot(blocked)
                .availableLot(available)
                .settlementUnlockDateTime(unlockAt)
                .settlementControlsActive(settlementControlsActive)
                .message(message)
                .build();

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/customers/{customerId}/assets/by-bist/{bistCode}/settlement-info")
    @PreAuthorize("hasAuthority('client:read:all') or hasAuthority('order:create')")
    public ResponseEntity<SettlementInfoResponse> getSettlementInfoByBist(
            @PathVariable Integer customerId,
            @PathVariable String bistCode
    ) {
        return assetRepository.findByBistCode(bistCode)
                .map(a -> getSettlementInfo(customerId, a.getId()))
                .orElseGet(() -> {
                    SettlementInfoResponse resp = SettlementInfoResponse.builder()
                            .customerId(customerId)
                            .assetId(null)
                            .totalLot(0)
                            .blockedLot(0)
                            .availableLot(0)
                            .settlementUnlockDateTime(null)
                            .message("Varlık bulunamadı: " + bistCode)
                            .build();
                    return ResponseEntity.status(404).body(resp);
                });
    }
}
