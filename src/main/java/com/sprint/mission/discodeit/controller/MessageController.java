package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    // 메시지 생성
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageDto> create(
            @RequestPart("messageInfo") MessageCreateRequest request,
            @RequestPart(value = "attachments", required = false)List<MultipartFile> attachments) throws IOException {

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
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MessageDto>> findAllByChannelId(
            @RequestParam UUID channelId) {
        List<MessageDto> messages = messageService.findAllByChannelId(channelId);
        return ResponseEntity.ok(messages);
    }

    // 메시지 수정
    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<MessageDto> update(
            @PathVariable UUID messageId,
            @RequestPart("messageInfo") MessageUpdateRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) throws IOException {

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
        MessageUpdateRequest serviceRequest = new MessageUpdateRequest(
                request.newContent(),
                attachmentRequests.isEmpty() ? null : attachmentRequests
        );

        return ResponseEntity.ok(messageService.update(messageId, serviceRequest));
    }

    // 메시지 삭제
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }
}
