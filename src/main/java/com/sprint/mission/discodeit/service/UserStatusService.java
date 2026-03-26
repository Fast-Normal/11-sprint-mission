package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

    //Create
    UserStatusDto create(UserStatusCreateRequest request);

    //Read
    UserStatusDto findById(UUID userStatusId);

    UserStatusDto findByUserId(UUID userId);

    //Read all
    List<UserStatusDto> findAll();

    //Update
    UserStatusDto update(UUID userStatusId);

    //Update by user id
    UserStatusDto updateByUserId(UUID userId);

    //Delete
    void delete(UUID userStatusId);

}
