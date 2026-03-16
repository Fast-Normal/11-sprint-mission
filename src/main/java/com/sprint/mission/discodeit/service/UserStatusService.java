package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserStatusService {

    //Create
    UserStatusDto create(UserStatusCreateDto request);

    //Read
    UserStatusDto findById(UUID userStatusId);

    //Read all
    List<UserStatusDto> findAll();

    //Update
    UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request);

    //Update by user id
    UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request);
    //Delete
    void delete(UUID userStatusId);

}
