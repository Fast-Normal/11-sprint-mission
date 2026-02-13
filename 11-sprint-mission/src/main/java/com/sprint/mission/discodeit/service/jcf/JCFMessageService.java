package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final List<Message> data;

    public JCFMessageService() {
        this.data = new ArrayList<>();
    }

    //create
    @Override
    public Message create(User sender, String content, Channel channel) {
        Message message = new Message(sender, channel, content);

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

    //update
    @Override
    public Message update(UUID messageId, String newContent){
        for (Message m : data) {
            if (m.getId().equals(messageId)) {
                m.updateContent(newContent);
                return m;
            }
        } return null;
    }

    //delete
    @Override
    public void delete(UUID messageId){
        for (int i = 0 ; i < data.size() ; i++) {
            if (data.get(i).getId().equals(messageId)) {
                data.remove(i);
                return;
            }
        }
    }
}
