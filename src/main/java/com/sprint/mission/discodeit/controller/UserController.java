package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserStatusService userStatusService;

    // 사용자 등록
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<UserDto> create(
            @RequestPart("userInfo") UserCreateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImg) throws IOException {

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
                        request.userName(),
                        request.userEmail(),
                        request.password(),
                        profileImageRequest
                )));
    }

    // 유저 아이디로 조회
    @RequestMapping(value ="/{userId}", method = RequestMethod.GET)
    public ResponseEntity<UserDto> findById(@PathVariable UUID userId) {
        UserDto user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

    // 유저 전체 조회
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public ResponseEntity<List<UserDto>> findAll() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    // 유저 정보 수정
    @RequestMapping(value = "/{userId}", method = RequestMethod.PATCH)
    public ResponseEntity<UserDto> update(
            @PathVariable UUID userId,
            @RequestPart("userInfo") UserUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile newProfileImg) throws IOException{

        BinaryContentCreateRequest profileImageRequest = null;
        if (newProfileImg != null && !newProfileImg.isEmpty()) {
            profileImageRequest = new BinaryContentCreateRequest(
                    newProfileImg.getOriginalFilename(),
                    newProfileImg.getContentType(),
                    newProfileImg.getBytes()
            );
        }

        return ResponseEntity.ok(userService.update(userId, new UserUpdateRequest(
                request.newUserName(),
                request.newUserEmail(),
                profileImageRequest,
                request.newPassword()
        )));
    }

    // 특정 유저 온라인 상태 업데이트
    @RequestMapping(value ="/{userId}/userStatus", method = RequestMethod.PATCH)
    public ResponseEntity<UserStatusDto> updateUserStatus(
            @PathVariable UUID userId) {

        UserStatusDto updated = userStatusService.updateByUserId(userId, new UserStatusUpdateRequest(Instant.now()));
        return ResponseEntity.ok(updated);
    }

    // 유저 삭제
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
