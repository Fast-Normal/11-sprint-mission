package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
class MessageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MessageRepository messageRepository;


  private UUID userId;
  private UUID channelId;

  @BeforeEach
  void setUp() throws Exception {
    // User 생성
    MockMultipartFile userRequestPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(
            new UserCreateRequest("msgTester", "msg@test.com", "pass1234!"))
    );
    String userBody = mockMvc.perform(multipart("/api/users").file(userRequestPart))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    userId = UUID.fromString(objectMapper.readTree(userBody).get("id").asText());

    // Channel 생성
    String channelBody = mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new PublicChannelCreateRequest("테스트채널", "설명"))))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    channelId = UUID.fromString(objectMapper.readTree(channelBody).get("id").asText());
  }

  private UUID createMessageAndGetId(String content) throws Exception {
    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(
            new MessageCreateRequest(userId, channelId, content, List.of()))
    );

    String body = mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    return UUID.fromString(objectMapper.readTree(body).get("id").asText());
  }

  @Test
  @DisplayName("메시지 생성 - 성공 - 201")
  void createMessage_success() throws Exception {
    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(
            new MessageCreateRequest(userId, channelId, "안녕하세요", List.of()))
    );

    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.content").value("안녕하세요"))
        .andExpect(jsonPath("$.channelId").value(channelId.toString()));
  }

  @Test
  @DisplayName("메시지 생성 - 실패 - 존재하지 않는 authorId 404")
  void createMessage_invalidAuthor_fail() throws Exception {
    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(
            new MessageCreateRequest(UUID.randomUUID(), channelId, "안녕하세요", List.of()))
    );

    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.USER_NOT_FOUND.getMessage()));
  }

  @Test
  @DisplayName("메시지 목록 조회 - 성공")
  void findAllMessages_success() throws Exception {
    createMessageAndGetId("첫 번째 메시지");
    createMessageAndGetId("두 번째 메시지");

    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2));
  }

  @Test
  @DisplayName("메시지 목록 조회 - 성공 - 메시지 없으면 빈 배열")
  void findAllMessages_empty() throws Exception {
    mockMvc.perform(get("/api/messages")
            .param("channelId", channelId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  @Test
  @DisplayName("메시지 수정 - 성공 - content 변경 반영")
  void updateMessage_success() throws Exception {
    UUID messageId = createMessageAndGetId("원본 메시지");

    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MessageUpdateRequest("수정된 메시지", null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("수정된 메시지"));
  }

  @Test
  @DisplayName("메시지 수정 - 실패 - 존재하지 않는 messageId 404")
  void updateMessage_notFound() throws Exception {
    mockMvc.perform(patch("/api/messages/{messageId}", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MessageUpdateRequest("수정된 메시지", null))))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("메시지 수정 - 실패 - 빈 내용으로 수정 시도 400")
  void updateMessage_emptyContent_fail() throws Exception {
    UUID messageId = createMessageAndGetId("원본 메시지");

    mockMvc.perform(patch("/api/messages/{messageId}", messageId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MessageUpdateRequest("", null))))  // 빈 문자열
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("유효성 검사 실패"));
  }

  @Test
  @DisplayName("메시지 삭제 - 성공 - 204 + DB에서 실제로 사라짐 검증")
  void deleteMessage_success() throws Exception {
    UUID messageId = createMessageAndGetId("삭제할 메시지");

    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNoContent());

    assertThat(messageRepository.findById(messageId)).isEmpty();
  }

  @Test
  @DisplayName("메시지 삭제 - 실패 - 존재하지 않는 messageId 404")
  void deleteMessage_notFound() throws Exception {
    mockMvc.perform(delete("/api/messages/{messageId}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
  }
}
