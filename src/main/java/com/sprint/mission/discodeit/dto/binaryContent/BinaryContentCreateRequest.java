package com.sprint.mission.discodeit.dto.binaryContent;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentCreateRequest(
        String fileName,
        String originalFileName,
        String contentType,
        long size,
        byte[] bytes
) {
}
