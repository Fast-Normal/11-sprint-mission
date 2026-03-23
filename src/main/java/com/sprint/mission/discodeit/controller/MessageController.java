package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam MessageCreateRequest request) {
        MessageDto message = messageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
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
            @RequestParam MessageUpdateRequest request) {
        MessageDto updated = messageService.update(messageId, request);
        return ResponseEntity.ok(updated);
    }

    // 메시지 삭제
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }
}
