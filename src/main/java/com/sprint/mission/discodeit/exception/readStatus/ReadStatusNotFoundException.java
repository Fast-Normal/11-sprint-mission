package com.sprint.mission.discodeit.exception.readStatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.message.MessageException;
import java.util.Map;
import java.util.UUID;

public class ReadStatusNotFoundException extends ReadStatusException {

  public ReadStatusNotFoundException(UUID authorId, UUID channelId) {
    super(ErrorCode.READ_STATUS_NOT_FOUND,
        Map.of("authorId", authorId, "channelId", channelId));
  }
}
