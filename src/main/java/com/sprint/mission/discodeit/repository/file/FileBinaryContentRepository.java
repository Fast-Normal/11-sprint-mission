package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {

  private final Path DIRECTORY;
  private final String EXTENSION = ".ser";
  private final FileLockProvider lockProvider;

  public FileBinaryContentRepository(FileLockProvider lockProvider) {
    this.lockProvider = lockProvider;
    this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map",
        BinaryContent.class.getSimpleName());
    if (Files.notExists(DIRECTORY)) {
      try {
        Files.createDirectories(DIRECTORY);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Path resolvePath(UUID id) {
    return DIRECTORY.resolve(id + EXTENSION);
  }

  // save
  @Override
  public BinaryContent save(BinaryContent binaryContent) {
    Path path = resolvePath(binaryContent.getId());
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try (FileOutputStream fos = new FileOutputStream(path.toFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos)
    ) {
      oos.writeObject(binaryContent);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    return binaryContent;
  }

  // read
  @Override
  public Optional<BinaryContent> findById(UUID binaryContentId) {
    Path path = resolvePath(binaryContentId);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();

    try {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
      try (FileInputStream fis = new FileInputStream(path.toFile());
          ObjectInputStream ois = new ObjectInputStream(fis)
      ) {
        return Optional.of((BinaryContent) ois.readObject());
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } finally {
      lock.unlock();
    }
  }

  // read all
  @Override
  public List<BinaryContent> findAll() {
    ReentrantLock lock = lockProvider.getLock(DIRECTORY);
    lock.lock();
    try (Stream<Path> paths = Files.list(DIRECTORY)) {
      return paths
          .filter(path -> path.toString().endsWith(EXTENSION))
          .map(path -> {
            try (
                FileInputStream fis = new FileInputStream(path.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
              return (BinaryContent) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
              throw new RuntimeException(e);
            }
          })
          .toList();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
    return findAll().stream()
        .filter(bc -> ids.contains(bc.getId()))
        .toList();
  }

  // delete
  @Override
  public void delete(UUID binaryContentId) {
    Path path = resolvePath(binaryContentId);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try {
      if (Files.notExists(path)) {
        throw new NoSuchElementException("컨텐츠를 찾을 수 없습니다: " + binaryContentId);
      }
      Files.delete(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void deleteAllByIdIn(List<UUID> ids) {
    ids.forEach(this::delete);
  }
}
