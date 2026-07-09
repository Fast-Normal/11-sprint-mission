package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import java.time.Instant;
import java.util.UUID;

public interface AuthService {

  UserDto updateRole(UserRoleUpdateRequest request);

  JwtInformation refresh(String refreshToken);

  void issueRefreshToken(UUID userId, String refreshToken, Instant expiresAt);

}
