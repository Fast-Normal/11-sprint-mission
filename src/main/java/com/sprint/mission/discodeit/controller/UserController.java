package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserStatusService userStatusService;

  // 사용자 등록
  @Operation(summary = "User 등록")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "User가 성공적으로 생성됨",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username을 사용하는 User가 이미 존재함",
          content = @Content(schema = @Schema(example = "User with email {email} already exists")))
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> create(
      @RequestPart("userCreateRequest") UserCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profileImg)
      throws IOException {

    BinaryContentCreateRequest profileImageRequest = null;
    if (profileImg != null && !profileImg.isEmpty()) {
      profileImageRequest = new BinaryContentCreateRequest(
          profileImg.getOriginalFilename(),
          profileImg.getContentType(),
          profileImg.getBytes()
      );
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userService.create(new UserCreateRequest(
            request.username(),
            request.email(),
            request.password(),
            profileImageRequest
        )));
  }

  // 유저 아이디로 조회
  @Operation(summary = "User 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User 조회 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음",
          content = @Content(schema = @Schema(example = "User with id {id} not found")))
  })
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> findById(
      @Parameter(description = "조회할 User Id") @PathVariable UUID userId) {
    UserDto user = userService.findById(userId);
    return ResponseEntity.ok(user);
  }

  // 유저 전체 조회
  @Operation(summary = "전체 User 목록 조회")
  @ApiResponse(responseCode = "200", description = "User 목록 조회 성공",
      content = @Content(schema = @Schema(implementation = UserDto.class)))
  @GetMapping
  public ResponseEntity<List<UserDto>> findAll() {
    List<UserDto> users = userService.findAll();
    return ResponseEntity.ok(users);
  }

  // 유저 정보 수정
  @Operation(summary = "User 정보 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User 정보가 성공적으로 수정됨",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음",
          content = @Content(schema = @Schema(example = "User with id {userId} not found"))),
      @ApiResponse(responseCode = "400", description = "같은 email 또는 username를 사용하는 User가 이미 존재함",
          content = @Content(schema = @Schema(example = "user with email {newEmail} already exists")))
  })
  @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<UserDto> update(
      @Parameter(description = "수정할 User ID") @PathVariable UUID userId,
      @RequestPart("userUpdateRequest") UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) @Parameter(description = "수정할 User 프로필 이미지") MultipartFile newProfileImg)
      throws IOException {

    BinaryContentCreateRequest profileImageRequest = null;
    if (newProfileImg != null && !newProfileImg.isEmpty()) {
      profileImageRequest = new BinaryContentCreateRequest(
          newProfileImg.getOriginalFilename(),
          newProfileImg.getContentType(),
          newProfileImg.getBytes()
      );
    }

    return ResponseEntity.ok(userService.update(userId, new UserUpdateRequest(
        request.newUsername(),
        request.newEmail(),
        profileImageRequest,
        request.newPassword()
    )));
  }

  // 특정 유저 온라인 상태 업데이트
  @Operation(summary = "User 온라인 상태 업데이트")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "User 온라인 상태가 성공적으로 업데이트됨",
          content = @Content(schema = @Schema(implementation = UserStatusDto.class))),
      @ApiResponse(responseCode = "404", description = "해당 User의 UserStatus를 찾을 수 없음",
          content = @Content(schema = @Schema(example = "UserStatus with userId {userId}")))
  })
  @PatchMapping("/{userId}/userStatus")
  public ResponseEntity<UserStatusDto> updateUserStatus(
      @Parameter(description = "상태를 변경할 User ID") @PathVariable UUID userId,
      @RequestBody UserStatusUpdateRequest request) {

    UserStatusDto updated = userStatusService.updateByUserId(userId, request);
    return ResponseEntity.ok(updated);
  }

  // 유저 삭제
  @Operation(summary = "User 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "User가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "User를 찾을 수 없음",
          content = @Content(schema = @Schema(example = "User with id {id} not found")))
  })
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 User ID") @PathVariable UUID userId) {
    userService.delete(userId);
    return ResponseEntity.noContent().build();
  }
}
