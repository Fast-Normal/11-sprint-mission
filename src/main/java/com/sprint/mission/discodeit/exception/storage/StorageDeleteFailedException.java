package com.sprint.mission.discodeit.exception.storage;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class StorageDeleteFailedException extends StorageException {

  public StorageDeleteFailedException(UUID id) {
    super(ErrorCode.STORAGE_DELETE_FAILED,
        Map.of("id", id));
  }
}
