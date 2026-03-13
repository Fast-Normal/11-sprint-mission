package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.UUID;

public record ChannelUpdateRequest(
        String channelName,
        ChannelType type,
        String description
) {
}
