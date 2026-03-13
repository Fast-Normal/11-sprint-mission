package com.sprint.mission.discodeit.dto.user;

import java.time.Instant;
import java.util.UUID;

public record UserCreateRequest(
        String userName,
        String userEmail,
        UUID profileId,
        String password
) {}
