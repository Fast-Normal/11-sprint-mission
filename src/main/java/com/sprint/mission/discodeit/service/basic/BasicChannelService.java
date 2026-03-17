package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    public ChannelDto toDto(Channel channel){
        // ReadStatus만 channelId, userId를 들고 있으므로 ReadStatus에서 채널아이디로 유저 목록 조회
        // Private면 유저 아이디 리스트 반환, public이면 null 반환
        List<UUID> participantIds = null;
        if (channel.getType() == ChannelType.PRIVATE) {
            participantIds = readStatusRepository.findAllByChannelId(channel.getId())
                    .stream()
                    .map(ReadStatus::getUserId)
                    .toList();
        }
        // 가장 최근 메시지 시간 조회
        Instant lastMessageAt = messageRepository.findAllByChannelId(channel.getId())
                .stream()
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new ChannelDto(
                channel.getId(),
                channel.getCreatedAt(),
                channel.getUpdatedAt(),
                channel.getChannelName(),
                channel.getType(),
                channel.getDescription(),
                participantIds,
                lastMessageAt
        );
    }

    //create public channel
    @Override
    public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
        // 퍼블릭 채널 생성
        Channel channel = new Channel(ChannelType.PUBLIC, request.channelName(), request.description());
        return toDto(channelRepository.save(channel));
    }

    //create private channel
    @Override
    public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
        // 프라이빗 채널 생성
        Channel channel = new Channel(ChannelType.PRIVATE, null, null);

        // ReadStatus 생성
        request.participantIds().forEach(userId -> {
            ReadStatus readStatus = new ReadStatus(userId, channel.getId());
            readStatusRepository.save(readStatus);
        });

        return toDto(channelRepository.save(channel));
    }


    //read
    @Override
    public ChannelDto findById(UUID channelId){
        return toDto(findChannelOrThrow(channelId));
    }

    //readAll
    @Override
    public List<ChannelDto> findAll(){
        return channelRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<ChannelDto> findAllByUserId(UUID userId) {
        List<UUID> myChannelIds = readStatusRepository.findAllByUserId(userId)
                .stream()
                .map(ReadStatus::getChannelId)
                .toList();

        return channelRepository.findAll().stream()
                .filter(channel ->
                        channel.getType() == ChannelType.PUBLIC //public은 전체
                        || myChannelIds.contains(channel.getId()) //private는 참여한것만
                )
                .map(this::toDto)
                .toList();
    }

    //update
    @Override
    public ChannelDto update(UUID channelId, ChannelUpdateRequest request){
        Channel channel = findChannelOrThrow(channelId);

        if (channel.getType() == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        channel.updateChannelName(request.channelName());
        channel.updateChannelDescription(request.description());

        return toDto(channelRepository.save(channel));
    }

    //delete
    @Override
    public void delete(UUID channelId){
        findChannelOrThrow(channelId);

        messageRepository.deleteByChannelId(channelId);
        readStatusRepository.deleteByChannelId(channelId);
        channelRepository.delete(channelId);
    }

    //channel 검증 로직
    private Channel findChannelOrThrow(UUID channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));
    }
}
