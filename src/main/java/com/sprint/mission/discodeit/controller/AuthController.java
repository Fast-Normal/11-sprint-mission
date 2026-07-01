package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetailService;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.util.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailService userDetailService;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  @Value("${jwt.cookie.secure}")
  private boolean cookieSecure;

  @Operation(summary = "액세스 토큰 재발급")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "재발급 성공"),
      @ApiResponse(responseCode = "401", description = "리프레시 토큰 유효하지 않음")
  })
  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(value = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {

    if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
      throw new RefreshTokenInvalidException(Map.of());
    }

    String subject = jwtTokenProvider.getSubject(refreshToken);
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) userDetailService.loadUserByUserId(
        UUID.fromString(subject));

    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    ResponseCookie refreshCookie = ResponseCookie
        .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken)
        .httpOnly(true)
        .secure(cookieSecure)
        .path("/")
        .maxAge(refreshTokenExpiration)
        .sameSite("Strict")
        .build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    log.info("Access Token 재발급 성공 - userId: {}", subject);
    return ResponseEntity.ok(new JwtDto(userDetails.getUserDto(), newAccessToken));
  }


  @Operation(summary = "사용자 권한 수정 API")
  @ApiResponse(responseCode = "200", description = "사용자 권한 수정 완료")
  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(@RequestBody UserRoleUpdateRequest request) {
    UserDto userDto = authService.updateRole(request);
    return ResponseEntity.ok(userDto);
  }
}
