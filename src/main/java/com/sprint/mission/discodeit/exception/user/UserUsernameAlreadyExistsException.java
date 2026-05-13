package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UserUsernameAlreadyExistsException extends UserException {

  public UserUsernameAlreadyExistsException(String username) {
    super(ErrorCode.USER_USERNAME_ALREADY_EXISTS,
        Map.of("username", username));
  }
}
