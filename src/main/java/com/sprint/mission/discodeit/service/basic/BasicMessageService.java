package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.nio.ByteBuffer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final MessageMapper messageMapper;
  private final PageResponseMapper pageResponseMapper;

  //create
  @Transactional
  @Override
  public MessageDto create(MessageCreateRequest request) {
    // 유저 검증
    User user = userRepository.findById(request.authorId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다: " + request.authorId()));
    // 채널 검증
    Channel channel = channelRepository.findById(request.channelId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 채널입니다: " + request.channelId()));

    Message message = new Message(user, channel, request.content());

    if (request.attachments() != null && !request.attachments().isEmpty()) {
      request.attachments().forEach(attachmentRequest -> {
        BinaryContent binaryContent = new BinaryContent(
            attachmentRequest.contentType(),
            attachmentRequest.bytes().length
        );
        binaryContentRepository.save(binaryContent);
        binaryContentStorage.put(binaryContent.getId(), attachmentRequest.bytes());
        message.getAttachments().add(binaryContent);
      });
    }

    return messageMapper.toDto(messageRepository.save(message));
  }

  //read
  @Override
  public MessageDto findById(UUID messageId) {
    return messageMapper.toDto(findMessageOrThrow(messageId));
  }

  //readAll
  @Override
  public PageResponse<MessageDto> findAllByChannelId(UUID channelId, Pageable pageable) {
    Slice<Message> slice = messageRepository
        .findAllByChannel_IdOrderByCreatedAtDesc(channelId, pageable);
    return pageResponseMapper.fromSlice(slice.map(messageMapper::toDto));
  }

  //update
  @Transactional
  @Override
  public MessageDto update(UUID messageId, MessageUpdateRequest request) {
    Message message = findMessageOrThrow(messageId);
    if (request.newContent() == null || request.newContent().trim().isEmpty()) {
      throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
    }
    message.updateContent(request.newContent());

    //첨부파일 수정
    if (request.attachments() != null) {
      // 기존 파일 삭제 (binaryContent는 불변객체)
      message.getAttachments().forEach(attachment ->
          binaryContentStorage.delete(attachment.getId()));
      //db에서 삭제
      binaryContentRepository.deleteAll(message.getAttachments());
      //컬렉션 비우기
      message.getAttachments().clear();
      ;

      // 새파일 등록
      request.attachments().forEach(attachmentRequest -> {
        BinaryContent binaryContent = new BinaryContent(
            attachmentRequest.contentType(),
            attachmentRequest.bytes().length
        );
        binaryContentRepository.save(binaryContent);
        binaryContentStorage.put(binaryContent.getId(), attachmentRequest.bytes());
        message.getAttachments().add(binaryContent);
      });
    }

    return messageMapper.toDto(message);
  }

  //delete
  @Transactional
  @Override
  public void delete(UUID messageId) {
    Message message = findMessageOrThrow(messageId);

    //binaryContent 삭제
    message.getAttachments().forEach(attachment ->
        binaryContentStorage.delete(attachment.getId()));
    binaryContentRepository.deleteAll(message.getAttachments());
    messageRepository.deleteById(messageId);
  }

  private Message findMessageOrThrow(UUID messageId) {
    return messageRepository.findById(messageId)
        .orElseThrow(() -> new NoSuchElementException("메시지가 없습니다." + messageId));
  }
}
