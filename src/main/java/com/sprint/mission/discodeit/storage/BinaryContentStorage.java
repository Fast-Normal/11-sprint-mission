package com.sprint.mission.discodeit.storage;

import java.io.InputStream;
import java.util.UUID;
import org.springframework.core.io.Resource;

public interface BinaryContentStorage {

  UUID put(UUID id, byte[] bytes);

  InputStream get(UUID id);

  Resource download(UUID id);

  void delete(UUID id);
}
