package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.login.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

  @Operation(summary = "CSRF 토큰 발급")
  @ApiResponse(responseCode = "203", description = "토큰 발급 성공")
  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    csrfToken.getToken();
    log.debug("CSRF 토큰 요청");

    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
  }

  @Operation(summary = "세션 기반 현재 사용자 조회")
  @ApiResponse(responseCode = "200", description = "사용자 조회 성공")
  @GetMapping("/me")
  public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    UserDto userDto = userDetails.getUserDto();
    return ResponseEntity.ok(userDto);
  }

  @Operation(summary = "사용자 권한 수정 API")
  @ApiResponse(responseCode = "200", description = "사용자 권한 수정 완료")
  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(@RequestBody UserRoleUpdateRequest request) {
    UserDto userDto = authService.updateRole(request);
    return ResponseEntity.ok(userDto);
  }
}
