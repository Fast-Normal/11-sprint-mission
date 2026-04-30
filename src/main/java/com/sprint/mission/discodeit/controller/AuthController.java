package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "로그인")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
          content = @Content(schema = @Schema(example = "User with username {username} not found"))),
      @ApiResponse(responseCode = "400", description = "비밀번호가 일치하지 않음",
          content = @Content(schema = @Schema(example = "Wrong password")))
  })
  @PostMapping("/login")
  public ResponseEntity<UserDto> login(
      @RequestBody LoginRequest request) {
    UserDto user = authService.login(request);

    return ResponseEntity.ok(user);
  }
}
