package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {
    private final ChannelService channelService;

    // 공개 채널 생성
    @RequestMapping(params = "type=public", method = RequestMethod.POST)
    public ResponseEntity<ChannelDto> createPublicChannel(
            @RequestBody PublicChannelCreateRequest request) {
        ChannelDto channel = channelService.createPublicChannel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(channel);
    }

    // 비공개 채널 생성
    @RequestMapping(params = "type=private", method = RequestMethod.POST)
    public ResponseEntity<ChannelDto> createPrivateChannel(
            @RequestBody PrivateChannelCreateRequest request) {
        ChannelDto channel = channelService.createPrivateChannel(request);
        return  ResponseEntity.status(HttpStatus.CREATED).body(channel);
    }

    // 특정 사용자가 볼 수 있는 모든 채널 목록을 조회
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ChannelDto>> findAllByUserId(
            @RequestParam UUID userId) {
        List<ChannelDto> channels = channelService.findAllByUserId(userId);
        return ResponseEntity.ok(channels);
    }

    // 공개 채널 정보 수정
    @RequestMapping(value = "/{channelId}", method = RequestMethod.PATCH)
    public ResponseEntity<ChannelDto> update(
            @PathVariable UUID channelId,
            @RequestBody ChannelUpdateRequest request) {
        ChannelDto updated = channelService.update(channelId, request);
        return ResponseEntity.ok(updated);
    }

    // 채널 삭제
    @RequestMapping(value = "/{channelId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID channelId) {
        channelService.delete(channelId);
        return ResponseEntity.noContent().build();
    }
}
