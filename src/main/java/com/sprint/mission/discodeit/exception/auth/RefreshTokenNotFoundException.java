package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class RefreshTokenNotFoundException extends DiscodeitException {

  public RefreshTokenNotFoundException(Map<String, Object> details) {
    super(ErrorCode.REFRESH_TOKEN_NOT_FOUND, details);
  }

}
