package com.sprint.mission.discodeit.exception.storage;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class StorageSaveFailedException extends StorageException {

  public StorageSaveFailedException(UUID id) {
    super(ErrorCode.STORAGE_SAVE_FAILED,
        Map.of("id", id));
  }
}
