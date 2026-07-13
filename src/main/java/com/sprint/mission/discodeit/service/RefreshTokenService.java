package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.RefreshToken;
import com.sprint.mission.discodeit.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public void issueRefreshToken(UUID userId, String refreshToken, Instant expiresAt) {
    refreshTokenRepository.findByUserId(userId)
        .ifPresentOrElse(
            existing -> existing.rotate(refreshToken, expiresAt),
            () -> refreshTokenRepository.save(new RefreshToken(userId, refreshToken, expiresAt))
        );
  }
}
