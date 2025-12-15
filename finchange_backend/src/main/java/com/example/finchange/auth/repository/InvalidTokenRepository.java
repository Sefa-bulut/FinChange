package com.example.finchange.auth.repository;

import com.example.finchange.auth.model.entity.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken, Long> {

    boolean existsByTokenId(String tokenId);

    void deleteAllByCreatedAtBefore(LocalDateTime createdAt);

    List<InvalidToken> findAllByTokenIdIn(Set<String> tokenIds);

}
