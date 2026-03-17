package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;

import java.util.List;
import java.util.UUID;

public record MessageUpdateRequest(
        String newContent,
        List<BinaryContentCreateRequest> attachments
) {}
