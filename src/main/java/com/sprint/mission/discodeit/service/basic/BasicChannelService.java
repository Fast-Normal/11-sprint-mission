package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final ChannelMapper channelMapper;
  private final UserRepository userRepository;

  //create public channel
  @Transactional
  @Override
  public ChannelDto createPublicChannel(PublicChannelCreateRequest request) {
    // 퍼블릭 채널 생성
    Channel channel = new Channel(ChannelType.PUBLIC, request.name(), request.description());
    return channelMapper.toDto(channelRepository.save(channel));
  }

  //create private channel
  @Transactional
  @Override
  public ChannelDto createPrivateChannel(PrivateChannelCreateRequest request) {
    // 프라이빗 채널 생성
    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    // ReadStatus가 Channel을 FK로 참조하므로 생성 전 먼저 저장
    channelRepository.save(channel);

    // ReadStatus 생성
    request.participantIds().forEach(userId -> {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다." + userId));
      ReadStatus readStatus = new ReadStatus(user, channel);
      readStatusRepository.save(readStatus);
    });

    return toChannelDto(channel);
  }


  //read
  @Override
  @Transactional(readOnly = true)
  public ChannelDto findById(UUID channelId) {
    Channel channel = findChannelOrThrow(channelId);
    return toChannelDto(channel);
  }

  //readAll
  @Override
  @Transactional(readOnly = true)
  public List<ChannelDto> findAll() {
    return channelRepository.findAll().stream()
        .map(this::toChannelDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> myChannelIds = readStatusRepository.findAllByUserIdWithChannel(userId)
        .stream()
        .map(rs -> rs.getChannel().getId())
        .toList();

    List<Channel> channels = myChannelIds.isEmpty()
        ? channelRepository.findAllByType(ChannelType.PUBLIC)
        : channelRepository.findAllPublicOrIn(myChannelIds);

    return toChannelDtos(channels);
  }

  //update
  @Transactional
  @Override
  public ChannelDto update(UUID channelId, ChannelUpdateRequest request) {
    Channel channel = findChannelOrThrow(channelId);

    if (channel.getType() == ChannelType.PRIVATE) {
      throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
    }

    channel.updateChannelName(request.newName());
    channel.updateChannelDescription(request.newDescription());

    return toChannelDto(channel);
  }

  //delete
  @Transactional
  @Override
  public void delete(UUID channelId) {
    findChannelOrThrow(channelId);
    channelRepository.deleteById(channelId); // Message, ReadStatus cascade로 자동 삭제
  }

  //channel 검증 로직
  private Channel findChannelOrThrow(UUID channelId) {
    return channelRepository.findById(channelId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다."));
  }

  //participants 조회 로직
  private List<User> findParticipants(Channel channel) {
    return channel.getType() == ChannelType.PRIVATE
        ? readStatusRepository.findAllByChannel_Id(channel.getId())
        .stream()
        .map(ReadStatus::getUser)
        .toList()
        : null;
  }

  //lastMessageAt 조회 로직
  private Instant findLastMessageAt(Channel channel) {
    return messageRepository
        .findTopByChannel_IdOrderByCreatedAtDesc(channel.getId())
        .map(Message::getCreatedAt)
        .orElse(null);
  }

  // channel, participants, lastMessageAt 단건 변환
  private ChannelDto toChannelDto(Channel channel) {
    return channelMapper.toDto(channel, findParticipants(channel), findLastMessageAt(channel));
  }

  // 다건 변환
  private List<ChannelDto> toChannelDtos(List<Channel> channels) {
    List<UUID> channelIds = channels.stream()
        .map(Channel::getId)
        .toList();

    // 참여자 벌크 조회 → channelId로 그룹핑
    Map<UUID, List<User>> participantsMap = readStatusRepository
        .findAllByChannelIds(channelIds)
        .stream()
        .collect(Collectors.groupingBy(
            rs -> rs.getChannel().getId(),
            Collectors.mapping(ReadStatus::getUser, Collectors.toList())
        ));

    // 마지막 메시지 벌크 조회 → channelId로 그룹핑
    Map<UUID, Instant> lastMessageAtMap = messageRepository
        .findLastMessagesByChannelIds(channelIds)
        .stream()
        .collect(Collectors.toMap(
            m -> m.getChannel().getId(),
            Message::getCreatedAt
        ));

    return channels.stream()
        .map(channel -> {
          List<User> participants = channel.getType() == ChannelType.PRIVATE
              ? participantsMap.getOrDefault(channel.getId(), List.of())
              : null;
          Instant lastMessageAt = lastMessageAtMap.get(channel.getId());
          return channelMapper.toDto(channel, participants, lastMessageAt);
        })
        .toList();
  }
}
