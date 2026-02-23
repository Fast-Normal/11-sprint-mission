package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;

public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ChannelService channelService;

    public BasicMessageService(MessageRepository messageRepository, UserService userService, ChannelService channelService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.channelService = channelService;
    }

    //create
    @Override
    public Message create(String content, UUID channelId, UUID authorId) {
        User author = userService.findById(authorId);
        Channel channel = channelService.findById(channelId);
        if (author == null || channel == null) {
            throw new IllegalArgumentException("Invalid user/channel");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Invalid content");
        }

        Message message = new Message(author, channel, content);
        return messageRepository.save(message);
    }
    //read
    @Override
    public Message findById(UUID messageId){
        return messageRepository.findById(messageId);
    }
    //readAll
    @Override
    public List<Message> findAll(){
        return messageRepository.findAll();
    }

    //update
    @Override
    public Message update(UUID messageId, String newContent){
        Message msg = messageRepository.findById(messageId);
        if (msg == null) {
            return null;
        }
        msg.updateContent(newContent);
        return messageRepository.save(msg);
    }

    //delete
    @Override
    public void delete(UUID messageId){
        messageRepository.delete(messageId);
    }
}
