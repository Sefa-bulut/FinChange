package com.example.finchange.auth.service.impl;

import com.example.finchange.auth.model.entity.InvalidToken;
import com.example.finchange.auth.repository.InvalidTokenRepository;
import com.example.finchange.auth.service.InvalidTokenService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvalidTokenServiceImpl implements InvalidTokenService {

    private final InvalidTokenRepository invalidTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REDIS_BLACKLIST_PREFIX = "blacklist:";

    /**
     * Verilen token ID'lerini hem Redis'e hem de veritabanına ekleyerek geçersiz kılar.
     * Redis hız için, veritabanı kalıcılık için kullanılır.
     */
    @Override
    @Transactional
    public void invalidateTokens(Set<String> tokenIds, Date tokenExpiry) {
        if (tokenIds == null || tokenIds.isEmpty()) {
            return;
        }

        // 1. Redis'e Ekle (Hızlı Yol)
        // Redis'e erişilemezse işlem durmaz, sadece loglanır.
        try {
            long ttlMillis = tokenExpiry.getTime() - System.currentTimeMillis();
            if (ttlMillis > 0) {
                tokenIds.forEach(tokenId -> {
                    String redisKey = REDIS_BLACKLIST_PREFIX + tokenId;
                    redisTemplate.opsForValue().set(redisKey, "1", ttlMillis, TimeUnit.MILLISECONDS);
                });
                log.info("[Auth] {} adet token Redis blacklist'e eklendi.", tokenIds.size());
            }
        } catch (DataAccessException e) {
            log.error("[Auth] Redis'e token eklenirken hata oluştu. İşlem veritabanı üzerinden devam edecek.", e);
        }

        // 2. Veritabanına Ekle (Garantici Yol)
        // Veritabanına sadece daha önce eklenmemiş olanları ekleyerek gereksiz yazma işlemlerini önle.
        Set<String> existingTokenIdsInDb = invalidTokenRepository.findAllByTokenIdIn(tokenIds)
                .stream()
                .map(InvalidToken::getTokenId)
                .collect(Collectors.toSet());

        Set<InvalidToken> tokensToSave = tokenIds.stream()
                .filter(tokenId -> !existingTokenIdsInDb.contains(tokenId))
                .map(tokenId -> {
                    // Mevcut DB şemasına uygun olarak entity oluşturuluyor.
                    InvalidToken entity = new InvalidToken();
                    entity.setTokenId(tokenId);
                    entity.setCreatedAt(LocalDateTime.now());
                    // 'createdBy' alanı JWT'den alınarak doldurulabilir, şimdilik null kalıyor.
                    // entity.setCreatedBy(getCurrentUserFromContext()); 
                    return entity;
                })
                .collect(Collectors.toSet());

        if (!tokensToSave.isEmpty()) {
            invalidTokenRepository.saveAll(tokensToSave);
            log.info("[Auth] {} adet yeni token veritabanındaki invalid_token tablosuna eklendi.", tokensToSave.size());
        }
    }

    /**
     * Bir token'ın geçersiz kılınıp kılınmadığını kontrol eder.
     * Önce Redis'e bakar, Redis'e ulaşılamazsa veya token orada yoksa veritabanına bakar.
     */
    @Override
    public void checkForInvalidityOfToken(String tokenId) {
        String redisKey = REDIS_BLACKLIST_PREFIX + tokenId;

        // 1. Hızlı Kontrol: Redis
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                log.warn("[Auth] Token Redis blacklist'te bulundu (Hızlı Red): {}", tokenId);
                throw new JwtException("Token has been invalidated");
            }
        } catch (DataAccessException e) {
            log.error("[Auth] Redis kontrolü sırasında hata. Veritabanı kontrolüne (Fallback) geçiliyor...", e);
        }

        // 2. Garanti Kontrolü: Veritabanı
        if (invalidTokenRepository.existsByTokenId(tokenId)) {
            log.warn("[Auth] Token veritabanı blacklist'te bulundu (Garanti Red): {}", tokenId);
            
            // İyileştirme: DB'de bulunan token'ı Redis'e de ekle.
            // Böylece bir sonraki sorguda direkt Redis'ten yakalanır ve DB yorulmaz.
            try {
                redisTemplate.opsForValue().set(redisKey, "1", 1, TimeUnit.DAYS); // TTL'i uzun tutabiliriz.
            } catch (DataAccessException redisEx) {
                log.error("[Auth] DB'de bulunan token Redis'e eklenirken hata oluştu.", redisEx);
            }
            
            throw new JwtException("Token has been invalidated");
        }
    }

    /**
     * Veritabanındaki süresi dolmuş token kayıtlarını temizler.
     * Bu metodun periyodik olarak bir zamanlayıcı (Scheduler) ile çalıştırılması önerilir.
     */
    @Override
    @Transactional
    public void deleteAllByCreatedAtBefore(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        // Bu metot, repository'de custom bir query ile int dönecek şekilde implemente edilirse daha iyi olur.
        // Şimdilik void dönüş tipine göre bırakıyorum.
        invalidTokenRepository.deleteAllByCreatedAtBefore(createdAt);
        log.info("[Auth] {} tarihinden eski geçersiz token kayıtları veritabanından silindi.", createdAt);
    }
}