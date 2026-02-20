package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFUserRepository implements UserRepository {

    private final List<User> data;

    public JCFUserRepository() {
        this.data = new ArrayList<>();
    }

    //save
    @Override
    public User save(User user){
        data.add(user);
        return user;
    }

    //Read
    @Override
    public User findById(UUID id){
        for (User user : data) {
            if (user.getId().equals(id)) {
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

    //Delete
    @Override
    public void delete(UUID id){
        data.removeIf(u -> u.getId().equals(id));
    }
}
