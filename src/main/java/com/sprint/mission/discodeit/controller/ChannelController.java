package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@Tag(name = "Channel", description = "Channel API")
@RequiredArgsConstructor
public class ChannelController {

  private final ChannelService channelService;

  // 공개 채널 생성
  @Operation(summary = "Public Channel 생성")
  @ApiResponse(responseCode = "201", description = "Public Channel이 성공적으로 생성됨",
      content = @Content(schema = @Schema(implementation = ChannelDto.class)))
  @PostMapping("/public")
  public ResponseEntity<ChannelDto> createPublicChannel(
      @RequestBody PublicChannelCreateRequest request) {
    ChannelDto channel = channelService.createPublicChannel(request);
    URI location = URI.create("/api/channels/public/" + channel.id());
    return ResponseEntity.created(location).body(channel);
  }

  // 비공개 채널 생성
  @Operation(summary = "Private Channel 생성")
  @ApiResponse(responseCode = "201", description = "Private Channel이 성공적으로 생성됨",
      content = @Content(schema = @Schema(implementation = ChannelDto.class)))
  @PostMapping("/private")
  public ResponseEntity<ChannelDto> createPrivateChannel(
      @RequestBody PrivateChannelCreateRequest request) {
    ChannelDto channel = channelService.createPrivateChannel(request);
    URI location = URI.create("/api/channels/private/" + channel.id());
    return ResponseEntity.created(location).body(channel);
  }

  // 채널 단건 조회
  @Operation(summary = "Channel 단건 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Channel 조회 성공",
          content = @Content(schema = @Schema(implementation = ChannelDto.class))),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음",
          content = @Content(schema = @Schema(example = "Channel with id {channelId} not found")))
  })
  @GetMapping("/{channelId}")
  public ResponseEntity<ChannelDto> findById(
      @Parameter(description = "조회할 Channel ID") @PathVariable UUID channelId) {
    ChannelDto channel = channelService.findById(channelId);
    return ResponseEntity.ok(channel);
  }

  // 특정 사용자가 볼 수 있는 모든 채널 목록을 조회
  @Operation(summary = "User가 참여 중인 Channel 목록 조회")
  @ApiResponse(responseCode = "200", description = "Channel 목록 조회 성공",
      content = @Content(schema = @Schema(implementation = ChannelDto.class)))
  @GetMapping
  public ResponseEntity<List<ChannelDto>> findAllByUserId(
      @Parameter(description = "조회할 User ID") @RequestParam UUID userId) {
    List<ChannelDto> channels = channelService.findAllByUserId(userId);
    return ResponseEntity.ok(channels);
  }

  // 공개 채널 정보 수정
  @Operation(summary = "Channel 정보 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Channel 정보가 성공적으로 수정됨",
          content = @Content(schema = @Schema(implementation = ChannelDto.class))),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음",
          content = @Content(schema = @Schema(example = "Channel with id {channelId} not found"))),
      @ApiResponse(responseCode = "400", description = "Private Channel은 수정할 수 없음",
          content = @Content(schema = @Schema(example = "Private channel cannot be updated")))
  })
  @PatchMapping("/{channelId}")
  public ResponseEntity<ChannelDto> update(
      @Parameter(description = "수정할 Channel ID") @PathVariable UUID channelId,
      @RequestBody ChannelUpdateRequest request) {
    ChannelDto updated = channelService.update(channelId, request);
    return ResponseEntity.ok(updated);
  }

  // 채널 삭제
  @Operation(summary = "Channel 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Channel이 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Channel을 찾을 수 없음",
          content = @Content(schema = @Schema(example = "Channel with id {channelId} not found")))
  })
  @DeleteMapping("/{channelId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Channel ID") @PathVariable UUID channelId) {
    channelService.delete(channelId);
    return ResponseEntity.noContent().build();
  }
}
