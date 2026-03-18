package com.sprint.mission.discodeit.dto.binaryContent;

public record BinaryContentCreateRequest(
        String originalFileName,
        String contentType,
        byte[] bytes
) {
}
