package com.sprint.mission.discodeit.security.filter;

import com.sprint.mission.discodeit.exception.auth.JwtExpiredException;
import com.sprint.mission.discodeit.exception.auth.JwtSignatureException;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetailService;
import com.sprint.mission.discodeit.security.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailService userDetailsService;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    // Bearer 토큰 없으면 이 필터 스킵 -> doFilterInternal 호출 x
    String authorization = request.getHeader(AUTHORIZATION_HEADER);
    return !StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX);
  }

  @Override
  public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // Bearer 이후 토큰 추출
    String token = request.getHeader(AUTHORIZATION_HEADER).substring(BEARER_PREFIX.length());

    // 유효성 검사
    if (jwtTokenProvider.validateToken(token)) {

      // subject(userId) 추출
      String subject = jwtTokenProvider.getSubject(token);

      // UserDetails 로드
      UserDetails userDetails = userDetailsService.loadUserByUserId(UUID.fromString(subject));

      // Authentication 객체 생성
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authentication.setDetails(
          new WebAuthenticationDetailsSource().buildDetails(request));

      // SecurityContext에 등록
      SecurityContextHolder.getContext().setAuthentication(authentication);
      log.debug("JWT 인증 성공 - userId: {}", subject);
    } else {
      log.debug("유효하지 않은 JWT - 인증 생략");
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

}
