package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;

    public MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getAuthorId(),
                message.getChannelId(),
                message.getContent(),
                message.getAttachmentIds()
        );
    }


    //create
    @Override
    public MessageDto create(MessageCreateRequest request) {
       // 유저 검증
        userRepository.findById(request.authorId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 유저입니다: " + request.authorId()));
        // 채널 검증
        channelRepository.findById(request.channelId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 채널입니다: " + request.channelId()));

        List<UUID> attachmentIds = new ArrayList<>();
        if (request.attachments() != null && !request.attachments().isEmpty()) {
            request.attachments().forEach(attachment -> {
                BinaryContent binaryContent = new BinaryContent(
                        attachment.originalFileName(),
                        attachment.contentType(),
                        attachment.bytes()
                );
                BinaryContent saved = binaryContentRepository.save(binaryContent);
                attachmentIds.add(saved.getId());
            });
        }

        Message message = new Message(request.authorId(), request.channelId(), request.content());
        attachmentIds.forEach(message::attachFile);

        return toDto(messageRepository.save(message));
    }

    //read
    @Override
    public MessageDto findById(UUID messageId){
        return toDto(findMessageOrThrow(messageId));
    }

    //readAll
    @Override
    public List<MessageDto> findAllByChannelId(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    //update
    @Override
    public MessageDto update(UUID messageId, MessageUpdateRequest request){
        Message message = findMessageOrThrow(messageId);
        if (request.newContent() == null || request.newContent().trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
        }
        message.updateContent(request.newContent());

        //첨부파일 수정
        if (request.attachments() != null) {
            // 기존 파일 삭제 (binaryContent는 불변객체)
            binaryContentRepository.deleteAllByIdIn(message.getAttachmentIds());
            // 새파일 등록
            List<UUID> newAttachmentIds = new ArrayList<>();
            request.attachments().forEach(attachment -> {
                    BinaryContent binaryContent = new BinaryContent(
                            attachment.originalFileName(),
                            attachment.contentType(),
                            attachment.bytes()
                    );
                    BinaryContent saved = binaryContentRepository.save(binaryContent);
                    newAttachmentIds.add(saved.getId());
            });
            message.getAttachmentIds().clear();
            newAttachmentIds.forEach(message::attachFile);
        }

        return toDto(messageRepository.save(message));
    }

    //delete
    @Override
    public void delete(UUID messageId){
        Message message = findMessageOrThrow(messageId);

        //binaryContent 삭제
        binaryContentRepository.deleteAllByIdIn(message.getAttachmentIds());

        messageRepository.delete(messageId);
    }

    private Message findMessageOrThrow(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new NoSuchElementException("메시지가 없습니다." + messageId));
    }
}
