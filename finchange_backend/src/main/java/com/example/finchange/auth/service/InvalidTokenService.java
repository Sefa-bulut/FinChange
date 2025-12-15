package com.example.finchange.auth.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

public interface InvalidTokenService {

    void invalidateTokens(final Set<String> tokenIds, Date tokenExpiry);

    void checkForInvalidityOfToken(final String tokenId);

    void deleteAllByCreatedAtBefore(LocalDateTime createdAt);
}
