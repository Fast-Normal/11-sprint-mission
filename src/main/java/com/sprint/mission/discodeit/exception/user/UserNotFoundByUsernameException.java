package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UserNotFoundByUsernameException extends UserException {

  public UserNotFoundByUsernameException(String username) {
    super(ErrorCode.USER_NOT_FOUND,
        Map.of("username", username));
  }
}
