package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFUserService implements UserService {
    private final List<User> data;

    public JCFUserService() {
        this.data = new ArrayList<>();
    }
    //create
    @Override
    public User create(String userName, String userEmail){
        User user = new User(userName, userEmail);

        data.add(user);

        return user;
    }

    //Read
    @Override
    public User findById(UUID userId){
        for (User user : data) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    //Read all
    @Override
    public List<User> findAll() {
        return new ArrayList<>(data);
    }

    //Update
    @Override
    public User update(UUID userId, String newUserName, String newUserEmail){
        for (User user : data) {
            if (user.getId().equals(userId)) {
                user.updateUserName(newUserName);
                user.updateUserEmail(newUserEmail);
                return user;
            }
        }
        return null;
    }

    //Delete
    @Override
    public void delete(UUID userId){
        for (int i = 0 ; i < data.size() ; i++) {
            if (data.get(i).getId().equals(userId)) {
                data.remove(i);
                return;
            }
        }
    }
}
