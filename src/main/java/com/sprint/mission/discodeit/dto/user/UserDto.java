package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.entity.Role;
import java.util.UUID;

public record UserDto(
    UUID id,
    String username,
    String email,
    UUID profileId,
    boolean online,
    Role role
    // password 없음
) {

  public UserDto withOnline(boolean online) {
    return new UserDto(id, username, email, profileId, online, role);
  }
}
