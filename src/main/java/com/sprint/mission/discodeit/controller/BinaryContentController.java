package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {
    private final BinaryContentService binaryContentService;

    // 단건 조회
    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentDto> findById(
            @PathVariable UUID binaryContentId) {
        BinaryContentDto content = binaryContentService.findById(binaryContentId);
        return ResponseEntity.ok(content);
    }

    // 다건 조회
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
            @RequestParam List<UUID> binaryContentIds) {
        List<BinaryContentDto> contents = binaryContentService.findAllByIdIn(binaryContentIds);
        return ResponseEntity.ok(contents);
    }

    // 파일 다운로드
    @RequestMapping(value = "/{binaryContentId}/download", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(
            @PathVariable UUID binaryContentId) {
        BinaryContentDto content = binaryContentService.findById(binaryContentId);
        ByteArrayResource resource = new ByteArrayResource(content.bytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + content.originalFileName() + "\"")
                .contentType(MediaType.parseMediaType(content.contentType()))
                .contentLength(content.size())
                .body(resource);
    }
}
