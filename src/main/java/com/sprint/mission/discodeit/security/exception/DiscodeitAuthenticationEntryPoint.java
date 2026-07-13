package com.sprint.mission.discodeit.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.exception.auth.JwtExpiredException;
import com.sprint.mission.discodeit.exception.auth.JwtSignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscodeitAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authenticationException) throws IOException {

    // jwt 관련 예외 있는지 확인
    Object jwtException = request.getAttribute("exception");

    ErrorCode errorCode;
    if (jwtException instanceof JwtExpiredException) {
      errorCode = ErrorCode.JWT_EXPIRED;
    } else if (jwtException instanceof JwtSignatureException) {
      errorCode = ErrorCode.JWT_SIGNATURE_INVALID;
    } else {
      errorCode = ErrorCode.AUTHENTICATION_FAILED;
    }

    ErrorResponse errorResponse = new ErrorResponse(
        errorCode.getStatus().value(),
        authenticationException.getClass().getSimpleName(),
        errorCode.getMessage(),
        Map.of(),
        Instant.now(),
        errorCode.name()
    );

    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
