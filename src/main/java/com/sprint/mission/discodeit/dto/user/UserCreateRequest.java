package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;

public record UserCreateRequest(
        String userName,
        String userEmail,
        String password,
        BinaryContentCreateRequest profileImage
) {}
