package com.sprint.mission.discodeit.security.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    log.warn("로그인 실패: {}", exception.getMessage());

    ErrorCode errorCode = ErrorCode.AUTHENTICATION_FAILED;

    ErrorResponse errorResponse = new ErrorResponse(
        errorCode.getStatus().value(),
        errorCode.getClass().getSimpleName(),
        errorCode.getMessage(),
        Map.of(),
        Instant.now(),
        errorCode.name()
    );

    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    String json = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(json);
  }
}
