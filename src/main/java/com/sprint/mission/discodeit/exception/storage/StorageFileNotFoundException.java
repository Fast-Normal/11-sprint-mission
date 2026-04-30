package com.sprint.mission.discodeit.exception.storage;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class StorageFileNotFoundException extends StorageException {

  public StorageFileNotFoundException(UUID id) {
    super(ErrorCode.STORAGE_FILE_NOT_FOUND,
        Map.of("id", id));
  }
}
