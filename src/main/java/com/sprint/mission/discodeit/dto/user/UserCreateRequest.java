package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;

import java.util.UUID;

public record UserCreateRequest(
        String userName,
        String userEmail,
        String password,
        BinaryContentCreateRequest profileImage
) {}
