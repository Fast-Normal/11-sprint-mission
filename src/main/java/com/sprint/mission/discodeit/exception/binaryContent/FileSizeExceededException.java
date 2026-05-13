package com.sprint.mission.discodeit.exception.binaryContent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class FileSizeExceededException extends BinaryContentException {

  public FileSizeExceededException(long size) {
    super(ErrorCode.FILE_SIZE_EXCEEDED,
        Map.of("size", size));
  }

}
