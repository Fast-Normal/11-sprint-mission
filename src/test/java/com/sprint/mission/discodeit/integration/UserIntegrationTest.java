package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
class UserIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  private MockMultipartFile toRequestPart(String partName, Object dto) throws Exception {
    return new MockMultipartFile(
        partName,
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(dto)
    );
  }

  private UUID createUserAndGetId(String username, String email) throws Exception {
    MockMultipartFile requestPart = toRequestPart(
        "userCreateRequest",
        new UserCreateRequest(username, email, "password1234")
    );

    String body = mockMvc.perform(multipart("/api/users").file(requestPart))
        .andReturn().getResponse().getContentAsString();

    return UUID.fromString(objectMapper.readTree(body).get("id").asText());
  }

  @Test
  @DisplayName("사용자 생성 API E2E 테스트")
  void createUser_success() throws Exception {
    MockMultipartFile requestPart = toRequestPart(
        "userCreateRequest",
        new UserCreateRequest("woody", "woody@test.com", "password1234")
    );

    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.username").value("woody"))
        .andExpect(jsonPath("$.email").value("woody@test.com"));
  }

  @Test
  @DisplayName("사용자 생성 실패 - 중복 이메일 409")
  void createUser_duplicateEmail_fail() throws Exception {
    createUserAndGetId("woody", "woody@test.com");

    MockMultipartFile requestPart = toRequestPart(
        "userCreateRequest",
        new UserCreateRequest("woody2", "woody@test.com", "password1234")
    );

    mockMvc.perform(multipart("/api/users").file(requestPart))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_EMAIL_ALREADY_EXISTS.getMessage()));
  }

  @Test
  @DisplayName("사용자 목록 조회 - 성공")
  void findAllUsers_success() throws Exception {
    createUserAndGetId("user1", "user1@test.com");
    createUserAndGetId("user2", "user2@test.com");

    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  @DisplayName("사용자 목록 조회 - 성공 - 아무것도 없을 때 빈 배열")
  void findAllUsers_empty() throws Exception {
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @DisplayName("사용자 수정 - 성공 - username 변경")
  void updateUser_success() throws Exception {
    UUID userId = createUserAndGetId("woody", "woody@test.com");

    MockMultipartFile requestPart = toRequestPart(
        "userUpdateRequest",
        new UserUpdateRequest("woodyUpdated", "woody@test.com", "password1234")
    );

    mockMvc.perform(multipart("/api/users/" + userId)
            .file(requestPart)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("woodyUpdated"));
  }

  @Test
  @DisplayName("사용자 수정 - 실패 - 존재하지 않는 userId - 404")
  void updateUser_notFound_fail() throws Exception {
    MockMultipartFile requestPart = toRequestPart(
        "userUpdateRequest",
        new UserUpdateRequest("none", "none@test.com", "password1234")
    );

    mockMvc.perform(multipart("/api/users/" + UUID.randomUUID())
            .file(requestPart)
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            }))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
  }

  @Test
  @DisplayName("사용자 삭제 - 성공 - 204")
  void deleteUser_success() throws Exception {
    UUID userId = createUserAndGetId("woody", "woody@test.com");

    mockMvc.perform(delete("/api/users/" + userId))
        .andExpect(status().isNoContent());

    // db에 실제로 없는지 확인
    assertThat(userRepository.findById(userId)).isEmpty();
  }

  @Test
  @DisplayName("사용자 삭제 - 실패 - 존재하지 않는 userId 404")
  void deleteUser_notFound_fail() throws Exception {
    mockMvc.perform(delete("/api/users/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
  }
}
