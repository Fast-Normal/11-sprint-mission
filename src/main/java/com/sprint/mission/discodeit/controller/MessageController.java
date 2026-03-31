package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Tag(name = "Message", description = "Message API")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  // 메시지 생성
  @Operation(summary = "Message 생성")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Message가 성공적으로 생성됨",
          content = @Content(schema = @Schema(implementation = MessageDto.class))),
      @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음",
          content = @Content(schema = @Schema(example = "Channel | Author with id {channelId} | {authorId} not found")))
  })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageDto> create(
      @RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) @Parameter(description = "Message 첨부 파일들") List<MultipartFile> attachments)
      throws IOException {

    // MultipartFile List를 BinaryContentCreateRequest List로 변환
    List<BinaryContentCreateRequest> attachmentRequests = new ArrayList<>();
    if (attachments != null && !attachments.isEmpty()) {
      for (MultipartFile file : attachments) {
        attachmentRequests.add(new BinaryContentCreateRequest(
            file.getOriginalFilename(),
            file.getContentType(),
            file.getBytes()
        ));
      }
    }

    MessageCreateRequest serviceRequest = new MessageCreateRequest(
        request.authorId(),
        request.channelId(),
        request.content(),
        attachmentRequests.isEmpty() ? null : attachmentRequests
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(messageService.create(serviceRequest));
  }

  // 특정 채널의 메시지 목록 조회
  @Operation(summary = "Channel의 Message 목록 조회")
  @ApiResponse(responseCode = "200", description = "Message 목록 조회 성공",
      content = @Content(schema = @Schema(implementation = MessageDto.class)))
  @GetMapping
  public ResponseEntity<List<MessageDto>> findAllByChannelId(
      @Parameter(description = "조회할 Channel ID") @RequestParam UUID channelId) {
    List<MessageDto> messages = messageService.findAllByChannelId(channelId);
    return ResponseEntity.ok(messages);
  }

  // 메시지 수정
  // 첨부파일 수정이 가능하게 구현해왔으나 명세서에 맞춰서 첨부파일 수정은 지원하지 않게 함
  @Operation(summary = "Message 내용 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Message가 성공적으로 수정됨",
          content = @Content(schema = @Schema(implementation = MessageDto.class))),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음",
          content = @Content(schema = @Schema(example = "Message with id {messageId} not found")))
  })
  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageDto> update(
      @Parameter(description = "수정할 Message ID") @PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest request) {

    return ResponseEntity.ok(messageService.update(messageId, request));
  }

  // 메시지 삭제
  @Operation(summary = "Message 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Message가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음",
          content = @Content(schema = @Schema(example = "Message with id {messageId} not found")))
  })
  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(
      @Parameter(description = "삭제할 Message ID") @PathVariable UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity.noContent().build();
  }
}
