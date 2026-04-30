package com.sprint.mission.discodeit.exception.readStatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReadStatusAlreadyExistsException extends ReadStatusException {

  public ReadStatusAlreadyExistsException(UUID authorId, UUID channelId) {
    super(ErrorCode.READ_STATUS_ALREADY_EXISTS,
        Map.of("authorId", authorId, "channelId", channelId));
  }
}
