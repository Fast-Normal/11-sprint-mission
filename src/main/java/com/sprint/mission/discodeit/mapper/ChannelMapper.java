package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    uses = {UserMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChannelMapper {

  @Mapping(target = "participants", ignore = true)
  @Mapping(target = "lastMessageAt", ignore = true)
  ChannelDto toDto(Channel channels);

  @Mapping(target = "participants", source = "participants")
  @Mapping(target = "lastMessageAt", source = "lastMessageAt")
  ChannelDto toDto(Channel channels, List<User> participants, Instant lastMessageAt);
}
