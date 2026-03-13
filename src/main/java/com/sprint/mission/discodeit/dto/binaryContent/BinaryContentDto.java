package com.sprint.mission.discodeit.dto.binaryContent;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentDto(
        UUID id,
        Instant createdAt,
        String fileName,
        String originalFileName,
        String contentType,
        long size,
        byte[] bytes
) {
}
