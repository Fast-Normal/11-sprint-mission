package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    //Create
    User create(String userName, String userEmail);

    //Read
    User findById(UUID userId);

    //Read all
    List<User> findAll();

    //Update
    User update(UUID userId, String newUserName, String newUserEmail);

    //Delete
    void delete(UUID userId);

}
