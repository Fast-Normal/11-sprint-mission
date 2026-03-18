//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.Message;
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.repository.ChannelRepository;
//import com.sprint.mission.discodeit.repository.UserRepository;
//import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
//import com.sprint.mission.discodeit.service.ChannelService;
//import com.sprint.mission.discodeit.service.MessageService;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.io.*;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//public class FileMessageService implements MessageService {
//
//    private final FileMessageRepository messageRepository;
//    private final UserRepository userRepository;
//    private final ChannelRepository channelRepository;
//
//   public FileMessageService(FileMessageRepository messageRepository, UserRepository userRepository, ChannelRepository channelRepository) {
//       this.userRepository = userRepository;
//       this.channelRepository = channelRepository;
//       this.messageRepository = messageRepository;
//   }
//
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
//        Message message = new Message(authorId,channelId, content);
//
//        return messageRepository.save(message);
//    }
//
//    //read
//    @Override
//    public Message findById(UUID messageId){
//        return messageRepository.findById(messageId)
//                .orElseThrow(() -> new IllegalArgumentException(("메시지를 찾을 수 없습니다")));
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
//                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다"));
//
//        msg.updateContent(newContent);
//        return messageRepository.save(msg);
//    }
//
//    //delete
//    @Override
//    public void delete(UUID messageId) {
//        messageRepository.delete(messageId);
//    }
//
//
//}
