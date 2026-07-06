package com.sprint.mission.discodeit.security.jwt.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Value("${jwt.refresh-token-expiration}")
  private int refreshTokenExpiration;

  @Value("${jwt.cookie.secure}")
  private boolean cookieSecure;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    log.info("로그인 성공 - JWT 발급");

    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();
    UUID userId = userDetails.getUserDto().id();

    // 동시 로그인 제한: 기존 세션 무효화
    if (jwtRegistry.hasActiveJwtInformationByUserId(userId)) {
      log.info("기존 로그인 세션 무효화 - userId: {}", userId);
      jwtRegistry.invalidateJwtInformationByUserId(userId);
    }

    // 토큰 발급
    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    jwtRegistry.registerJwtInformation(new JwtInformation(
        userDetails.getUserDto(),
        accessToken,
        refreshToken,
        jwtTokenProvider.getExpiration(refreshToken)
    ));

    // 리프레시 토큰 쿠키에 저장
    ResponseCookie refreshCookie = ResponseCookie
        .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(refreshTokenExpiration)
        .sameSite("Strict")
        .build();

    response.addHeader("Set-Cookie", refreshCookie.toString());

    // 액세스 토큰 응답 바디에 포함
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), accessToken);
    response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
  }

}
