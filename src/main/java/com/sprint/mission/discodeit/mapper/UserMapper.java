package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDto toDto(User user) {

    BinaryContentDto profileDto = null;
    if (user.getProfile() != null) {
      BinaryContent profile = user.getProfile();
      profileDto = new BinaryContentDto(
          profile.getId(),
          profile.getFileName(),
          profile.getSize(),
          profile.getContentType(),
          null // UserDto에선 bytes는 제공 안 하게
      );
    }
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        profileDto,
        user.getUserStatus() != null && user.getUserStatus().isOnline()
    );
  }


}
