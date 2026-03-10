package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.util.List;
import java.util.UUID;


public class FileUserService implements UserService {

    private final FileUserRepository userRepository;

    public FileUserService(FileUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // create
    @Override
    public User create(String userName, String userEmail) {
        if (userRepository.existByEmail(userEmail)) {
            throw new IllegalArgumentException("중복된 이메일입니다.");
        }
        User user = new User(userName, userEmail);
        return userRepository.save(user);
    }

    // read
    @Override
    public User findById(UUID userId) {
        return userRepository.findById(userId);
    }

    // read all
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // update
    @Override
    public User update(UUID userId, String newUserName, String newUserEmail) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return null;
        }
        user.updateUserName(newUserName);
        user.updateUserEmail(newUserEmail);
        return userRepository.save(user);
    }

    // delete
    @Override
    public void delete(UUID userId) {
        userRepository.delete(userId);
    }

}
