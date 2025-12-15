package com.example.finchange.brokerage.service.impl;

import com.example.finchange.brokerage.model.BrokerageFirm;
import com.example.finchange.brokerage.repository.BrokerageFirmRepository;
import com.example.finchange.brokerage.service.BrokerageFirmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrokerageFirmServiceImpl implements BrokerageFirmService {

    private final BrokerageFirmRepository brokerageFirmRepository;
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String INACTIVE_STATUS = "INACTIVE";

    @Override
    @Cacheable(value = "commissionRate", key = "'active'")
    @Transactional(readOnly = true)
    public BigDecimal getActiveCommissionRate() {
        log.info("Aktif komisyon oranı veritabanından okunuyor...");
        return brokerageFirmRepository.findByStatus(ACTIVE_STATUS)
                .map(BrokerageFirm::getCommissionRate)
                .orElseThrow(() -> new IllegalStateException("Sistemde aktif bir aracı kurum bulunamadı."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrokerageFirm> findAll() {
        return brokerageFirmRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public BrokerageFirm getById(Integer id) {
        return brokerageFirmRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aracı kurum bulunamadı: id=" + id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "commissionRate", key = "'active'")
    public BrokerageFirm create(BrokerageFirm firm) {
        validateCommissionRateFraction(firm.getCommissionRate());
        if (firm.getStatus() == null || firm.getStatus().isBlank()) {
            firm.setStatus(INACTIVE_STATUS);
        }
        if (ACTIVE_STATUS.equalsIgnoreCase(firm.getStatus()) && brokerageFirmRepository.existsByStatus(ACTIVE_STATUS)) {
            throw new IllegalStateException("Zaten aktif bir aracı kurum mevcut. Önce mevcut aktifi pasif hale getirin veya 'activate' endpointini kullanın.");
        }
        firm.setId(null); 
        return brokerageFirmRepository.save(firm);
    }

    @Override
    @Transactional
    @CacheEvict(value = "commissionRate", key = "'active'")
    public BrokerageFirm update(Integer id, BrokerageFirm firm) {
        BrokerageFirm existing = getById(id);
        validateCommissionRateFraction(firm.getCommissionRate());

        String newStatus = firm.getStatus() == null ? existing.getStatus() : firm.getStatus();
        if (ACTIVE_STATUS.equalsIgnoreCase(newStatus) && brokerageFirmRepository.existsByStatusAndIdNot(ACTIVE_STATUS, id)) {
            throw new IllegalStateException("Yalnızca 1 aktif aracı kurum olabilir. Başkasını pasifleştirmeden bu kaydı AKTIF yapamazsınız.");
        }

        existing.setKurumKodu(firm.getKurumKodu());
        existing.setKurumAdi(firm.getKurumAdi());
        existing.setApiUrl(firm.getApiUrl());
        existing.setUsername(firm.getUsername());
        existing.setPassword(firm.getPassword());
        existing.setIntegrationType(firm.getIntegrationType());
        existing.setEmail(firm.getEmail());
        existing.setCommissionRate(firm.getCommissionRate());
        existing.setStatus(newStatus);

        return brokerageFirmRepository.save(existing);
    }

    @Override
    @Transactional
    @CacheEvict(value = "commissionRate", key = "'active'")
    public void delete(Integer id) {
        BrokerageFirm existing = getById(id);
        if (ACTIVE_STATUS.equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException("Aktif aracı kurum silinemez. Önce başka bir kurumu aktif yapın veya bu kurumu pasif hale getirin.");
        }
        brokerageFirmRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "commissionRate", key = "'active'")
    public BrokerageFirm activate(Integer id) {
        BrokerageFirm toActivate = getById(id);
        brokerageFirmRepository.findByStatus(ACTIVE_STATUS).ifPresent(active -> {
            if (!active.getId().equals(id)) {
                active.setStatus(INACTIVE_STATUS);
                brokerageFirmRepository.save(active);
            }
        });
        toActivate.setStatus(ACTIVE_STATUS);
        return brokerageFirmRepository.save(toActivate);
    }

    private void validateCommissionRateFraction(BigDecimal rate) {
        if (rate == null) {
            throw new IllegalArgumentException("Komisyon oranı zorunludur.");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Komisyon oranı 0 ile 1 arasında (ondalık) olmalıdır. Örn: %10 için 0.10");
        }
    }
}