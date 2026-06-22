package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UserPasswordAlreadyUsedException extends UserException {

  public UserPasswordAlreadyUsedException() {
    super(ErrorCode.USER_PASSWORD_ALREADY_USED,
        Map.of());
  }
}
