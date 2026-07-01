package com.sprint.mission.discodeit.service;


import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;

public interface AuthService {

  UserDto updateRole(UserRoleUpdateRequest request);

  JwtDto refresh(String refreshToken);

}
