package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {
    private final JCFMessageRepository messageRepository;
    private final UserService userService;
    private final ChannelService channelService;

    public JCFMessageService(JCFMessageRepository messageRepository, UserService userService, ChannelService channelService) {
        this.messageRepository = messageRepository;
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
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
        }

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
