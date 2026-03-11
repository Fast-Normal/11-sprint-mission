package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;

import com.sprint.mission.discodeit.repository.MessageRepository;


import java.util.*;

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
    //readAll
    @Override
    public List<Message> findAll(){
        return new ArrayList<>(data.values());
    }

    //delete
    @Override
    public void delete(UUID messageId){
        data.remove(messageId);
    }
}
