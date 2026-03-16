package com.sprint.mission.discodeit.dto.user;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
        UUID id,
        String userName,
        String userEmail,
        UUID profileId,
        Instant createdAt,
        Instant updatedAt,
        boolean isOnline
        // password 없음
) {}
