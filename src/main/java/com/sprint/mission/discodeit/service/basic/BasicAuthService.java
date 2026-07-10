package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.RefreshToken;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.notification.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.RefreshTokenRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtRegistry jwtRegistry;
  private final ApplicationEventPublisher eventPublisher;

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Override
  public UserDto updateRole(UserRoleUpdateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    Role oldRole = user.getRole();
    user.updateRole(request.newRole());
    userRepository.save(user);

    // 역할 변경 시 기존 토큰 모두 무효화
    refreshTokenRepository.deleteByUserId(user.getId());
    jwtRegistry.invalidateJwtInformationByUserId(user.getId());
    log.info("권한 변경으로 인한 강제 로그아웃 - userId: {}", user.getId());

    eventPublisher.publishEvent(new RoleUpdatedEvent(
        user.getId(),
        oldRole,
        request.newRole()
    ));

    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public JwtInformation refresh(String refreshToken) {
    if (!StringUtils.hasText(refreshToken) ||
        !jwtTokenProvider.isValidToken(refreshToken) ||
        !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RefreshTokenInvalidException(Map.of("reason", "missing"));
    }

    UUID userId = UUID.fromString(jwtTokenProvider.getSubject(refreshToken));

    // DB에 저장된 현재 유효 토큰과 일치하는지 확인
    RefreshToken saved = refreshTokenRepository.findByUserId(userId)
        .orElseThrow(() -> new RefreshTokenInvalidException(
            Map.of("reason", "token-reused", "userId", userId)));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    // 새 access token + refresh token 발급
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(userMapper.toDto(user),
        user.getPassword());

    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
    Instant newExpiry = jwtTokenProvider.getExpiration(newRefreshToken);

    JwtInformation newInfo = new JwtInformation(
        userMapper.toDto(user),
        newAccessToken,
        newRefreshToken,
        newExpiry
    );
    jwtRegistry.rotateJwtInformation(refreshToken, newInfo);

    saved.rotate(newRefreshToken, newExpiry);
    refreshTokenRepository.save(saved);

    return newInfo;
  }

  @Transactional
  @Override
  public void issueRefreshToken(UUID userId, String refreshToken, Instant expiresAt) {
    refreshTokenRepository.findByUserId(userId)
        .ifPresentOrElse(existing -> existing.rotate(refreshToken, expiresAt),
            () -> refreshTokenRepository.save(new RefreshToken(userId, refreshToken, expiresAt)));
  }
}
