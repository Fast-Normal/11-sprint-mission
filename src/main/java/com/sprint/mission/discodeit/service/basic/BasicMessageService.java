package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;


    //create
    @Override
    public Message create(String content, UUID channelId, UUID authorId) {
        if (!userRepository.existsById(authorId)) {
            throw new IllegalArgumentException("Invalid author");
        }

        if (!channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException("Invalid channel");
        }
        Message message = new Message(authorId, channelId, content);
        return messageRepository.save(message);
    }
    //read
    @Override
    public Message findById(UUID messageId){
        return messageRepository.findById(messageId)
                .orElseThrow(()-> new IllegalArgumentException("메시지를 찾을 수 없습니다"));
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

        Message msg = messageRepository.findById(messageId)
                .orElseThrow(()-> new IllegalArgumentException("메시지를 찾을 수 없습니다"));
        msg.updateContent(newContent);
        return messageRepository.save(msg);
    }

    //delete
    @Override
    public void delete(UUID messageId){
        messageRepository.delete(messageId);
    }
}
