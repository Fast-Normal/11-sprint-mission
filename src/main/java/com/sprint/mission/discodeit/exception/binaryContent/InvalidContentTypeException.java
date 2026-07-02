package com.sprint.mission.discodeit.exception.binaryContent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class InvalidContentTypeException extends BinaryContentException {

  public InvalidContentTypeException(String contentType) {
    super(ErrorCode.INVALID_CONTENT_TYPE,
        Map.of("ContentType", contentType));
  }

}
