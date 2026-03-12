package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.util.*;

public class JCFUserRepository implements UserRepository {

    private final Map<UUID, User> data;

    public JCFUserRepository() {
        this.data = new HashMap<>();
    }

    //save
    @Override
    public User save(User user){
        data.put(user.getId(), user);
        return user;
    }

    //Read
    @Override
    public Optional<User> findById(UUID id){
        return Optional.ofNullable(data.get(id));
    }

    //Read all
    @Override
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    //Delete
    @Override
    public void delete(UUID id){
        data.remove(id);
    }

    @Override
    public boolean existByEmail(String userEmail) {
        return data.values().stream()
                .anyMatch(user -> user.getUserEmail().equals(userEmail));
    }

    // existsById
    @Override
    public boolean existsById(UUID id) {
        return data.containsKey(id);
}
}
