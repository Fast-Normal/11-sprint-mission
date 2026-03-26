package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFUserStatusRepository implements UserStatusRepository {

    private final Map<UUID, UserStatus> data;

    public JCFUserStatusRepository() {
        this.data = new HashMap<>();
    }

    //save
    @Override
    public UserStatus save(UserStatus userStatus){
        data.put(userStatus.getId(), userStatus);
        return userStatus;
    }

    //Read
    @Override
    public Optional<UserStatus> findById(UUID id){
        return Optional.ofNullable(data.get(id));
    }

    // find by userId
    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return findAll().stream()
                .filter(us -> us.getUserId().equals(userId))
                .findFirst();
    }

    //Read all
    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(data.values());
    }

    //Delete
    @Override
    public void delete(UUID id){
        data.remove(id);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        findByUserId(userId)
                .ifPresent(us -> delete(us.getId()));
    }


}
