package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.service.ChannelService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChannelService channelService;


  private ChannelDto sampleChannelDto() {
    return new ChannelDto(
        UUID.randomUUID(), ChannelType.PUBLIC, "일반채널", "설명", List.of(), null
    );
  }

  // get 전체 조회
  @Test
  @DisplayName("GET /api/channels?userId= - 유저 채널 목록 조회 성공 - 200")
  void findAllByUserId_success() throws Exception {
    UUID userId = UUID.randomUUID();
    given(channelService.findAllByUserId(userId))
        .willReturn(List.of(sampleChannelDto()));

    mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].type").value("PUBLIC"));
  }

  @Test
  @DisplayName("GET /api/channels?userId= - 빈 목록도 200 + 빈 배열 반환")
  void findAllByUserId_empty() throws Exception {
    UUID userId = UUID.randomUUID();
    given(channelService.findAllByUserId(userId)).willReturn(List.of());

    mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  // post 채널 생성
  @Test
  @DisplayName("POST /api/channels/public - 공개 채널 생성 성공 - 201 + Location 헤더")
  void createPublic_success() throws Exception {
    ChannelDto dto = sampleChannelDto();
    given(channelService.createPublicChannel(any())).willReturn(dto);

    PublicChannelCreateRequest request = new PublicChannelCreateRequest("일반채널", "설명");

    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/channels/public/" + dto.id()))
        .andExpect(jsonPath("$.name").value("일반채널"))
        .andExpect(jsonPath("$.type").value("PUBLIC"));
  }

  // patch 채널 수정
  @Test
  @DisplayName("PATCH /api/channels/{channelId} - 채널 수정 성공 - 200")
  void update_success() throws Exception {
    ChannelDto dto = sampleChannelDto();
    given(channelService.update(eq(dto.id()), any())).willReturn(dto);

    mockMvc.perform(patch("/api/channels/{channelId}", dto.id())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new ChannelUpdateRequest("수정된 채널", "수정된 설명"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("일반채널"));
  }

  @Test
  @DisplayName("PATCH /api/channels/{channelId} - 존재하지 않는 채널 수정 - 404")
  void update_notFound() throws Exception {
    UUID unknown = UUID.randomUUID();
    given(channelService.update(eq(unknown), any()))
        .willThrow(new DiscodeitException(ErrorCode.CHANNEL_NOT_FOUND, Map.of()));

    mockMvc.perform(patch("/api/channels/{channelId}", unknown)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new ChannelUpdateRequest("수정된채널", "수정된설명"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.CHANNEL_NOT_FOUND.getMessage()));
  }

  @Test
  @DisplayName("PATCH /api/channels/{channelId} - Private 채널 수정 시도 - 400")
  void update_privateChannelDenied() throws Exception {
    UUID id = UUID.randomUUID();
    given(channelService.update(eq(id), any()))
        .willThrow(new DiscodeitException(ErrorCode.PRIVATE_CHANNEL_UPDATE_DENIED, Map.of()));

    mockMvc.perform(patch("/api/channels/{channelId}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new ChannelUpdateRequest("수정된채널", "수정된설명"))))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value(ErrorCode.PRIVATE_CHANNEL_UPDATE_DENIED.getMessage()));
  }

  // delete 채널 삭제
  @Test
  @DisplayName("DELETE /api/channels/{channelId} - 삭제 성공 - 204 반환")
  void delete_success() throws Exception {
    UUID id = UUID.randomUUID();
    willDoNothing().given(channelService).delete(id);

    mockMvc.perform(delete("/api/channels/{channelId}", id))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("DELETE /api/channels/{channelId} - 존재하지 않는 채널 삭제 - 404")
  void delete_notFound() throws Exception {
    UUID id = UUID.randomUUID();

    Mockito.doThrow(new DiscodeitException(ErrorCode.CHANNEL_NOT_FOUND, Map.of()))
        .when(channelService).delete(id);

    mockMvc.perform(delete("/api/channels/{channelId}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value(ErrorCode.CHANNEL_NOT_FOUND.getMessage()));
  }

}
