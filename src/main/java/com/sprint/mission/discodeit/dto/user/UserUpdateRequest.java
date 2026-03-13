package com.sprint.mission.discodeit.dto.user;

import java.time.Instant;
import java.util.UUID;

public record UserUpdateRequest(
        String newUserName,
        String newUserEmail,
        UUID newProfileId,
        String newPassword
) {}
