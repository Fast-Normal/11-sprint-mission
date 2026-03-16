package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;

import java.util.UUID;

public record UserUpdateRequest(
        String newUserName,
        String newUserEmail,
        BinaryContentCreateRequest newProfileImage,
        String newPassword
) {}
