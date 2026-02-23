package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final List<Message> data;
    private final UserService userService;
    private final ChannelService channelService;

    public JCFMessageService(UserService userService, ChannelService channelService) {
        this.data = new ArrayList<>();
        this.userService = userService;
        this.channelService = channelService;
    }

    //create
    @Override
    public Message create(String content, UUID authorId, UUID channelId) {
        User author = userService.findById(authorId);
        Channel channel = channelService.findById(channelId);
        if (author == null || channel == null) {
            throw new IllegalArgumentException("Invalid author/channel");
        }

        if (content == null  || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid content");
        }

        Message message = new Message(author,channel, content);

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
