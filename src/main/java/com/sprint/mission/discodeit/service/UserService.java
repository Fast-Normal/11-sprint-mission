package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    //Create
    UserDto create(UserCreateRequest request);

    //Read
    UserDto findById(UUID userId);

    //Read all
    List<UserDto> findAll();

    //Update
    UserDto update(UUID userId, UserUpdateRequest request);

    //Delete
    void delete(UUID userId);

}
