package com.sprint.mission.discodeit.security.jwt.handler;

import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtRegistry jwtRegistry;
  private final CacheManager cacheManager;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) {

    if (request.getCookies() == null) {
      log.debug("로그아웃 요청에 쿠키 없음");
      return;
    }

    Arrays.stream(request.getCookies())
        .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
        .findFirst()
        .ifPresent(cookie -> {
          String refreshToken = cookie.getValue();

          if (jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
            jwtRegistry.invalidateJwtInformationByRefreshToken(refreshToken);
            evictUserListCache();
            log.info("로그아웃 - Refresh Token 무효화 완료");
          } else {
            log.debug("로그아웃 - 이미 무효화된 Refresh Token");
          }

          ResponseCookie expiredCookie = ResponseCookie
              .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "")
              .httpOnly(true)
              .path("/")
              .maxAge(0)
              .sameSite("Strict")
              .build();

          response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
        });
  }

  private void evictUserListCache() {
    Cache cache = cacheManager.getCache(CacheConfig.USERS);
    if (cache != null) {
      cache.clear();
    }
  }

}
