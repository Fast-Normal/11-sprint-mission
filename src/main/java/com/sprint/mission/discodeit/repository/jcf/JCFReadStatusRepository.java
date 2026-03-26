package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFReadStatusRepository implements ReadStatusRepository {

    private final Map<UUID, ReadStatus> data;

    public JCFReadStatusRepository() {
        this.data = new HashMap<>();
    }

    //save
    @Override
    public ReadStatus save(ReadStatus readStatus){
        data.put(readStatus.getId(), readStatus);
        return readStatus;
    }

    //Read
    @Override
    public Optional<ReadStatus> findById(UUID id){
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Optional<ReadStatus> findByUserId(UUID userId) {
        return findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public Optional<ReadStatus> findByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .findFirst();
    }

    @Override
    public Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId) && rs.getChannelId().equals(channelId))
                .findFirst();
    }

    //Read all
    @Override
    public List<ReadStatus> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return findAll().stream()
                .filter(rs -> rs.getUserId().equals(userId))
                .toList();
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

    @Override
    public void deleteByChannelId(UUID channelId) {
        findByChannelId(channelId)
                .ifPresent(rs -> delete(rs.getId()));
    }
}
