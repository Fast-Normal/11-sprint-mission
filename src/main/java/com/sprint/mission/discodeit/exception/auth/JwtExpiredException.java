package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class JwtExpiredException extends AuthException {

  public JwtExpiredException(Map<String, Object> details) {
    super(ErrorCode.JWT_EXPIRED, details);
  }

  public JwtExpiredException(Map<String, Object> details, Throwable cause) {
    super(ErrorCode.JWT_EXPIRED, details, cause);
  }
}
