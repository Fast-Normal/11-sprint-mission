package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;

import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;


import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data;

    public JCFMessageRepository() {
        this.data = new HashMap<>();
    }

    //save
    @Override
    public Message save(Message message) {
        data.put(message.getId(), message);
        return message;
    }

    //read
    @Override
    public Optional<Message> findById(UUID messageId){
        return Optional.ofNullable(data.get(messageId));
    }

    @Override
    public Optional<Message> findByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(m-> m.getChannelId().equals(channelId))
                .findFirst();
    }

    //readAll
    @Override
    public List<Message> findAll(){
        return new ArrayList<>(data.values());
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(m -> m.getChannelId().equals(channelId))
                .toList();
    }

    //delete
    @Override
    public void delete(UUID messageId){
        data.remove(messageId);
    }

    @Override
    public void deleteByChannelId(UUID channelId) {
        findByChannelId(channelId)
                .ifPresent(msg -> delete(msg.getId()));
    }
}
