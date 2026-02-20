package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {
    private final List<Message> data;

    public JCFMessageRepository() {
        this.data = new ArrayList<>();
    }

    //save
    @Override
    public Message save(Message message) {
        data.add(message);
        return message;
    }

    //read
    @Override
    public Message findById(UUID messageId){
        for (Message m : data) {
            if (m.getId().equals(messageId)) {
                return m;
            }
        } return null;
    }
    //readAll
    @Override
    public List<Message> findAll(){
        return new ArrayList<>(data);
    }

    //delete
    @Override
    public void delete(UUID messageId){
        data.removeIf(m -> m.getId().equals(messageId));
    }
}
