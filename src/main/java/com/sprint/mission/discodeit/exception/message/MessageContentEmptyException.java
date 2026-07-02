package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class MessageContentEmptyException extends MessageException {

  public MessageContentEmptyException() {
    super(ErrorCode.MESSAGE_CONTENT_EMPTY,
        Map.of());
  }

}
