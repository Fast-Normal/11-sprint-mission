//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.repository.ChannelRepository;
//import com.sprint.mission.discodeit.repository.UserRepository;
//import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
//import com.sprint.mission.discodeit.service.ChannelService;
//import com.sprint.mission.discodeit.service.MessageService;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.util.List;
//import java.util.UUID;
//
//public class JCFMessageService implements MessageService {
//    private final JCFMessageRepository messageRepository;
//    private final UserRepository userRepository;
//    private final ChannelRepository channelRepository;
//
//    public JCFMessageService(JCFMessageRepository messageRepository, UserRepository userRepository, ChannelRepository channelRepository) {
//        this.messageRepository = messageRepository;
//        this.userRepository = userRepository;
//        this.channelRepository = channelRepository;
//    }
//
//    //create
//    @Override
//    public Message create(String content, UUID authorId, UUID channelId) {
//        if (!userRepository.existsById(authorId)) {
//            throw new IllegalArgumentException("Invalid author");
//        }
//
//        if (!channelRepository.existsById(channelId)) {
//            throw new IllegalArgumentException("Invalid channel");
//        }
//
//        if (content == null  || content.trim().isEmpty()) {
//            throw new IllegalArgumentException("Invalid content");
//        }
//
//        Message message = new Message(authorId,channelId, content);
//        return messageRepository.save(message);
//    }
//    //read
//    @Override
//    public Message findById(UUID messageId){
//        return messageRepository.findById(messageId)
//                .orElseThrow(()-> new IllegalArgumentException("메시지를 찾을 수 없습니다."));
//    }
//    //readAll
//    @Override
//    public List<Message> findAll(){
//        return messageRepository.findAll();
//    }
//
//    //update
//    @Override
//    public Message update(UUID messageId, String newContent){
//        if (newContent == null || newContent.trim().isEmpty()) {
//            throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
//        }
//
//        Message msg = messageRepository.findById(messageId)
//                .orElseThrow(()-> new IllegalArgumentException("메시지를 찾을 수 없습니다"));
//
//        msg.updateContent(newContent);
//        return messageRepository.save(msg);
//    }
//
//    //delete
//    @Override
//    public void delete(UUID messageId){
//        messageRepository.delete(messageId);
//    }
//}
