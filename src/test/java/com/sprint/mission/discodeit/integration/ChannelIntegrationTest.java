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
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@ActiveProfiles("test")
@Transactional
@AutoConfigureMockMvc(addFilters = false)
class ChannelIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ChannelRepository channelRepository;

  private UUID createPublicChannelAndGetId(String name) throws Exception {
    String body = mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new PublicChannelCreateRequest(name, "테스트 채널 설명"))))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    return UUID.fromString(objectMapper.readTree(body).get("id").asText());
  }

  private UUID createPrivateChannelAndGetId() throws Exception {
    MockMultipartFile userRequestPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(
            new UserCreateRequest("participant", "part@test.com", "pass1234!"))
    );
    String userBody = mockMvc.perform(multipart("/api/users").file(userRequestPart))
        .andReturn().getResponse().getContentAsString();
    UUID participantId = UUID.fromString(objectMapper.readTree(userBody).get("id").asText());

    String body = mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new PrivateChannelCreateRequest(List.of(participantId)))))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    return UUID.fromString(objectMapper.readTree(body).get("id").asText());
  }

  @Test
  @DisplayName("공개 채널 생성 - 성공 - 201")
  void createChannel_success() throws Exception {

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new PublicChannelCreateRequest("공개채널", "설명"))))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.name").value("공개채널"))
        .andExpect(jsonPath("$.type").value("PUBLIC"));

    assertThat(channelRepository.findAll()).hasSize(1);
  }

  @Test
  @DisplayName("프라이빗 채널 생성 - 성공 - 201 ")
  void createPrivateChannel_success() throws Exception {
    // 프라이빗 채널은 participantIds가 필요하므로 User 먼저 생성
    MockMultipartFile userRequestPart = new MockMultipartFile(
        "userCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(
            new UserCreateRequest("participant", "part@test.com", "pass1234!"))
    );
    String userBody = mockMvc.perform(multipart("/api/users").file(userRequestPart))
        .andReturn().getResponse().getContentAsString();
    UUID participantId = UUID.fromString(objectMapper.readTree(userBody).get("id").asText());

    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new PrivateChannelCreateRequest(List.of(participantId)))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PRIVATE"));
  }

  @Test
  @DisplayName("채널 단건 조회 - 성공")
  void findChannel_success() throws Exception {
    UUID channelId = createPublicChannelAndGetId("공개채널");

    mockMvc.perform(get("/api/channels/{channelId}", channelId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(channelId.toString()));
  }

  @Test
  @DisplayName("채널 단건 조회 - 실패 - 존재하지 않는 채널 404")
  void findChannel_notFound() throws Exception {
    mockMvc.perform(get("/api/channels/{channelId}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.CHANNEL_NOT_FOUND.getMessage()));
  }

  @Test
  @DisplayName("채널 수정 - 성공 - name 변경")
  void updateChannel_success() throws Exception {
    UUID channelId = createPublicChannelAndGetId("공개채널");

    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new ChannelUpdateRequest("수정된 채널", "수정된 설명"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("수정된 채널"));
  }

  @Test
  @DisplayName("채널 수정 - 실패 - 프라이빗 채널 수정 시도")
  void updateChannel_privateChannelUpdate_denied() throws Exception {

    UUID privateChannelId = createPrivateChannelAndGetId();

    mockMvc.perform(patch("/api/channels/{channelId}", privateChannelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new ChannelUpdateRequest("수정", "수정설명"))))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value(ErrorCode.PRIVATE_CHANNEL_UPDATE_DENIED.getMessage()));
  }

  @Test
  @DisplayName("채널 삭제 - 성공 - 204")
  void deleteChannel_success() throws Exception {
    UUID channelId = createPublicChannelAndGetId("공개");

    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andExpect(status().isNoContent());

    // db에 실제로 없는지 확인
    assertThat(channelRepository.findById(channelId)).isEmpty();
  }

  @Test
  @DisplayName("사용자 삭제 - 실패 - 존재하지 않는 채널 404")
  void deleteChannel_notFound_fail() throws Exception {
    mockMvc.perform(delete("/api/channels/{channelId}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.CHANNEL_NOT_FOUND.getMessage()));
  }
}
