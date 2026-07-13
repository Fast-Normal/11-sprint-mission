package com.sprint.mission.discodeit.storage.local;

import com.sprint.mission.discodeit.exception.storage.StorageDeleteFailedException;
import com.sprint.mission.discodeit.exception.storage.StorageFileNotFoundException;
import com.sprint.mission.discodeit.exception.storage.StorageSaveFailedException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(
      @Value("${discodeit.storage.local.root-path}") String rootPath
  ) {
    this.root = Paths.get(rootPath);
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new RuntimeException("스토리지 루트 디렉토리 생성 실패", e);
    }
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    // 테스트용 지연 추가
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Thread interrupted while simulating delay", e);
    }

    Path path = resolvePath(id);
    try (OutputStream os = Files.newOutputStream(path)) {
      os.write(bytes);
    } catch (IOException e) {
      throw new StorageSaveFailedException(id);
    }
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    try {
      return Files.newInputStream(resolvePath(id));
    } catch (IOException e) {
      throw new StorageFileNotFoundException(id);
    }
  }

  @Override
  public Resource download(UUID id) {
    log.debug("스토리지에서 파일 읽기 시작 - id: {}", id);
    Resource resource = new InputStreamResource(get(id));
    log.debug("스토리지에서 파일 읽기 완료 - id: {}", id);
    return resource;
  }

  @Override
  public void delete(UUID id) {
    try {
      Files.deleteIfExists(resolvePath(id));
    } catch (IOException e) {
      throw new StorageDeleteFailedException(id);
    }
  }

}
