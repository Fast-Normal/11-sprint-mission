package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserStatusService userStatusService;

  private UserDto sampleUserDto() {
    return new UserDto(
        UUID.randomUUID(), "woody", "woody@test.com", null, true
    );
  }

  // get 전체 조회
  @Test
  @DisplayName("GET /api/users - 전체 유저 목록 조회 성공 - 200")
  void findAll_success() throws Exception {
    given(userService.findAll()).willReturn(List.of(sampleUserDto()));

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").value("woody"))
        .andExpect(jsonPath("$[0].email").value("woody@test.com"));
  }

  @Test
  @DisplayName("GET /api/users - 빈 목록도 200 + 빈 배열 반환")
  void findAll_empty() throws Exception {
    given(userService.findAll()).willReturn(List.of());

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // get 단건 조회
  @Test
  @DisplayName("GET /api/users/{userId} - 존재하는 유저 조회 - 200")
  void findById_success() throws Exception {
    UserDto dto = sampleUserDto();
    given(userService.findById(dto.id())).willReturn(dto);

    mockMvc.perform(get("/api/users/{userId}", dto.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(dto.id().toString()))
        .andExpect(jsonPath("$.username").value("woody"));
  }

  @Test
  @DisplayName("GET /api/users/{userId} - 존재하지 않는 유저 - 404")
  void findById_notFound() throws Exception {
    UUID unknown = UUID.randomUUID();
    given(userService.findById(unknown))
        .willThrow(new DiscodeitException(ErrorCode.USER_NOT_FOUND, Map.of("userId", unknown)));

    mockMvc.perform(get("/api/users/{userId}", unknown))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
  }

  // post 유저 생성
  @Test
  @DisplayName("POST /api/users - 유저 생성 성공 - 201 + Location 헤더")
  void create_success() throws Exception {
    UserDto dto = sampleUserDto();
    given(userService.create(any(), any())).willReturn(dto);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsString(
            new UserCreateRequest("woody", "woody@test.com", "password1")
        ).getBytes()
    );

    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/users/" + dto.id()))
        .andExpect(jsonPath("$.username").value("woody"));
  }

  @Test
  @DisplayName("POST /api/users - 중복 이메일 - 409")
  void create_duplicateEmail() throws Exception {
    given(userService.create(any(), any()))
        .willThrow(new DiscodeitException(ErrorCode.USER_EMAIL_ALREADY_EXISTS, Map.of()));

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "",
        "application/json",
        objectMapper.writeValueAsString(
            new UserCreateRequest("tester", "dup@email.com", "password1")
        ).getBytes()
    );

    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_EMAIL_ALREADY_EXISTS.getMessage()));
  }

  // patch 유저 수정

  @Test
  @DisplayName("PATCH /api/users/{userId} - 유저 수정 성공 - 200")
  void update_success() throws Exception {
    UserDto dto = sampleUserDto();
    given(userService.update(eq(dto.id()), any(), any())).willReturn(dto);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest", "",
        "application/json",
        objectMapper.writeValueAsString(
            new UserUpdateRequest("woody2", "woody2@test.com", "newpassword1")
        ).getBytes()
    );

    mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", dto.id())
            .file(requestPart))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("woody"));
  }

  @Test
  @DisplayName("PATCH /api/users/{userId} - 존재하지 않는 유저 수정 - 404")
  void update_notFound() throws Exception {
    UUID unknown = UUID.randomUUID();
    given(userService.update(eq(unknown), any(), any()))
        .willThrow(new DiscodeitException(ErrorCode.USER_NOT_FOUND, Map.of()));

    MockMultipartFile requestPart = new MockMultipartFile(
        "userUpdateRequest", "",
        "application/json",
        objectMapper.writeValueAsString(
            new UserUpdateRequest("woody2", "woody2@test.com", "newpassword1")
        ).getBytes()
    );

    mockMvc.perform(multipart(HttpMethod.PATCH, "/api/users/{userId}", unknown)
            .file(requestPart))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
  }

  // delete 유저 삭제

  @Test
  @DisplayName("DELETE /api/users/{userId} - 삭제 성공 - 204 반환")
  void delete_success() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(userService).delete(id);

    mockMvc.perform(delete("/api/users/{userId}", id))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/users/{userId} - 존재하지 않는 유저 삭제 - 404")
  void delete_notFound() throws Exception {
    UUID id = UUID.randomUUID();

    Mockito.doThrow(new DiscodeitException(ErrorCode.USER_NOT_FOUND, Map.of()))
        .when(userService).delete(id);

    mockMvc.perform(delete("/api/users/{userId}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
  }

}
