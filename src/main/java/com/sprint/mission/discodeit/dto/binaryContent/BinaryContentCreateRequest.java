package com.sprint.mission.discodeit.dto.binaryContent;

public record BinaryContentCreateRequest(
        String fileName,
        String originalFileName,
        String contentType,
        long size,
        byte[] bytes
) {
}
