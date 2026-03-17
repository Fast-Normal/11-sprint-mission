package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;

public record UserUpdateRequest(
        String newUserName,
        String newUserEmail,
        BinaryContentCreateRequest newProfileImage,
        String newPassword
) {}
