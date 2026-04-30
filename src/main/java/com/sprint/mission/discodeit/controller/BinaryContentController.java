package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "BinaryContent", description = "첨부 파일 API")
@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  // 단건 조회
  @Operation(summary = "첨부 파일 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "첨부 파일 조회 성공",
          content = @Content(schema = @Schema(implementation = BinaryContentDto.class))),
      @ApiResponse(responseCode = "404", description = "첨부 파일을 찾을 수 없음",
          content = @Content(schema = @Schema(example = "BinaryContent with id {binaryContentId} not found")))
  })
  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentDto> findById(
      @Parameter(description = "조회할 첨부 파일 ID") @PathVariable UUID binaryContentId) {
    BinaryContentDto content = binaryContentService.findById(binaryContentId);
    return ResponseEntity.ok(content);
  }

  // 다건 조회
  @Operation(summary = "여러 첨부 파일 조회")
  @ApiResponse(responseCode = "200", description = "첨부 파일 목록 조회 성공",
      content = @Content(schema = @Schema(implementation = BinaryContentDto.class)))
  @GetMapping
  public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
      @Parameter(description = "조회할 첨부 파일 ID 목록") @RequestParam List<UUID> binaryContentIds) {
    List<BinaryContentDto> contents = binaryContentService.findAllByIdIn(binaryContentIds);
    return ResponseEntity.ok(contents);
  }

  // 파일 다운로드
  @Operation(summary = "첨부 파일 다운로드", description = "명세서 외 추가 기능")
  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(
      @Parameter(description = "다운로드할 첨부 파일 ID") @PathVariable UUID binaryContentId) {
    Resource resource = binaryContentService.download(binaryContentId);
    BinaryContentDto dto = binaryContentService.findById(binaryContentId);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + dto.fileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, dto.contentType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(dto.size()))
        .body(resource);
  }
}
