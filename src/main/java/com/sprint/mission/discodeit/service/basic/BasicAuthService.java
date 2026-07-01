package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.RefreshToken;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.RefreshTokenRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.util.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Override
  public UserDto updateRole(UserRoleUpdateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    user.updateRole(request.newRole());
    userRepository.save(user);

    refreshTokenRepository.deleteByUserId(user.getId());

    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public JwtDto refresh(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)) {
      throw new RefreshTokenInvalidException(Map.of("reason", "missing"));
    }

    // 서명/만료 검증
    Map<String, Object> claims = jwtTokenProvider.getClaims(refreshToken);
    UUID userId = UUID.fromString(claims.get("sub").toString());

    // DB에 저장된 현재 유효 토큰과 일치하는지 확인
    RefreshToken saved = refreshTokenRepository.findByUserId(userId)
        .orElseThrow(() -> new RefreshTokenInvalidException(
            Map.of("reason", "token-reused", "userId", userId)));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    // 새 access token + refresh token 발급
    String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

    saved.rotate(newRefreshToken, Instant.now().plus(Duration.ofDays(7)));
    refreshTokenRepository.save(saved);

    return new JwtDto(userMapper.toDto(user), newAccessToken);
  }
}
