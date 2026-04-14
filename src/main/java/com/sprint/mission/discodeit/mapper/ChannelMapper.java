package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

  private final ReadStatusRepository readStatusRepository;
  private final MessageRepository messageRepository;
  private final UserMapper userMapper;


  public ChannelDto toDto(Channel channel) {
    // ReadStatus만 channelId, userId를 들고 있으므로 ReadStatus에서 채널아이디로 유저 목록 조회
    // Private면 유저 아이디 리스트 반환, public이면 null 반환
    List<UserDto> participants = null;
    if (channel.getType() == ChannelType.PRIVATE) {
      participants = readStatusRepository.findAllByChannel_Id(channel.getId())
          .stream()
          .map(rs -> userMapper.toDto(rs.getUser()))
          .toList();
    }
    // 가장 최근 메시지 시간 조회
    Instant lastMessageAt = messageRepository
        .findTopByChannel_IdOrderByCreatedAtDesc(channel.getId())
        .map(Message::getCreatedAt)
        .orElse(null);

    return new ChannelDto(
        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        participants,
        lastMessageAt
    );
  }

}
