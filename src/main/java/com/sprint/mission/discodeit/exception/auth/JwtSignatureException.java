package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class JwtSignatureException extends AuthException {

  public JwtSignatureException(Map<String, Object> details) {
    super(ErrorCode.JWT_SIGNATURE_INVALID, details);
  }

  public JwtSignatureException(Map<String, Object> details, Throwable cause) {
    super(ErrorCode.JWT_SIGNATURE_INVALID, details, cause);
  }
}
