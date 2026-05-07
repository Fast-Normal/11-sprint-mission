package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;


  private MessageDto sampleMessageDto() {
    return new MessageDto(
        UUID.randomUUID(), Instant.now(), Instant.now(), "안녕하세요", UUID.randomUUID(), null, List.of()
    );
  }

  // get
  @Test
  @DisplayName("GET /api/messages - 채널 메시지 목록 조회 - 200")
  void findByChannelId_success() throws Exception {
    UUID channelId = UUID.randomUUID();
    PageResponse<MessageDto> page = new PageResponse<>(
        List.of(sampleMessageDto()), null, 1, false, 1L
    );
    given(messageService.findAllByChannelId(eq(channelId), any(), any())).willReturn(page);

    mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].content").value("안녕하세요"));
  }

  @Test
  @DisplayName("GET /api/messages - 메시지 없으면 빈 content 반환 - 200")
  void findAllByChannelId_empty() throws Exception {
    UUID channelId = UUID.randomUUID();
    PageResponse<MessageDto> page = new PageResponse<>(List.of(), null, 0, false, 0L);
    given(messageService.findAllByChannelId(eq(channelId), any(), any()))
        .willReturn(page);

    mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  // post 메시지 생성
  @Test
  @DisplayName("POST /api/messages - 메시지 생성 성공 - 201 + Location 헤더")
  void create_success() throws Exception {
    MessageDto dto = sampleMessageDto();
    given(messageService.create(any())).willReturn(dto);

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "",
        "application/json",
        objectMapper.writeValueAsString(
            new MessageCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "안녕하세요", null
            )).getBytes()
    );

    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/messages/" + dto.id()))
        .andExpect(jsonPath("$.content").value("안녕하세요"));
  }

  @Test
  @DisplayName("POST /api/messages - 존재하지 않는 채널 - 404")
  void create_channelNotFound() throws Exception {
    given(messageService.create(any()))
        .willThrow(new DiscodeitException(ErrorCode.CHANNEL_NOT_FOUND, Map.of()));

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "",
        "application/json",
        objectMapper.writeValueAsString(
            new MessageCreateRequest(UUID.randomUUID(), UUID.randomUUID(), "hello", null
            )).getBytes()
    );

    mockMvc.perform(multipart("/api/messages").file(requestPart))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.CHANNEL_NOT_FOUND.getMessage()));
  }

  // patch 채널 수정
  @Test
  @DisplayName("PATCH /api/messages/{messageId} - 메시지 수정 성공 - 200")
  void update_success() throws Exception {
    MessageDto dto = sampleMessageDto();
    given(messageService.update(eq(dto.id()), any())).willReturn(dto);

    mockMvc.perform(patch("/api/messages/{messageId}", dto.id())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MessageUpdateRequest("수정된 내용", null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("안녕하세요"));
  }

  @Test
  @DisplayName("PATCH /api/messages/{messageId} - 존재하지 않는 메시지 수정 - 404")
  void update_notFound() throws Exception {
    UUID unknown = UUID.randomUUID();
    given(messageService.update(eq(unknown), any()))
        .willThrow(new DiscodeitException(ErrorCode.MESSAGE_NOT_FOUND, Map.of()));

    mockMvc.perform(patch("/api/messages/{messageId}", unknown)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new MessageUpdateRequest("수정된 내용", null))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
  }

  // delete 채널 삭제
  @Test
  @DisplayName("DELETE /api/messages/{messageId} - 삭제 성공 - 204 반환")
  void delete_success() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(messageService).delete(id);

    mockMvc.perform(delete("/api/messages/{messageId}", id))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/messages/{messageId} - 존재하지 않는 메시지 삭제 - 404")
  void delete_notFound() throws Exception {
    UUID id = UUID.randomUUID();

    Mockito.doThrow(new DiscodeitException(ErrorCode.MESSAGE_NOT_FOUND, Map.of()))
        .when(messageService).delete(id);

    mockMvc.perform(delete("/api/messages/{messageId}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
  }

}
