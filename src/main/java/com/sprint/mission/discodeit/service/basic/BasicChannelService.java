package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;
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

    return channelMapper.toDto(channel);
  }


  //read
  @Override
  public ChannelDto findById(UUID channelId) {
    return channelMapper.toDto(findChannelOrThrow(channelId));
  }

  //readAll
  @Override
  public List<ChannelDto> findAll() {
    return channelRepository.findAll().stream()
        .map(channelMapper::toDto)
        .toList();
  }

  @Override
  public List<ChannelDto> findAllByUserId(UUID userId) {
    List<UUID> myChannelIds = readStatusRepository.findAllByUserIdWithChannel(userId)
        .stream()
        .map(rs -> rs.getChannel().getId())
        .toList();

    return channelRepository.findAll().stream()
        .filter(channel ->
            channel.getType() == ChannelType.PUBLIC //public은 전체
                || myChannelIds.contains(channel.getId()) //private는 참여한것만
        )
        .map(channelMapper::toDto)
        .toList();
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

    return channelMapper.toDto(channel);
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
}
