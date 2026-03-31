package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
public class FileReadStatusRepository implements ReadStatusRepository {

  private final Path DIRECTORY;
  private final String EXTENSION = ".ser";
  private final FileLockProvider lockProvider;

  public FileReadStatusRepository(FileLockProvider lockProvider) {
    this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map",
        ReadStatus.class.getSimpleName());
    this.lockProvider = lockProvider;
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
  public ReadStatus save(ReadStatus readStatus) {
    Path path = resolvePath(readStatus.getId());
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try (FileOutputStream fos = new FileOutputStream(path.toFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos)
    ) {
      oos.writeObject(readStatus);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    return readStatus;
  }

  // read
  @Override
  public Optional<ReadStatus> findById(UUID readStatusId) {
    Path path = resolvePath(readStatusId);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();

    try {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
      try (FileInputStream fis = new FileInputStream(path.toFile());
          ObjectInputStream ois = new ObjectInputStream(fis)) {
        return Optional.of((ReadStatus) ois.readObject());
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Optional<ReadStatus> findByUserId(UUID userId) {
    return findAll().stream()
        .filter(rs -> rs.getUserId().equals(userId))
        .findFirst();
  }

  @Override
  public Optional<ReadStatus> findByChannelId(UUID channelId) {
    return findAll().stream()
        .filter(rs -> rs.getChannelId().equals(channelId))
        .findFirst();
  }

  @Override
  public Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId) {
    return findAll().stream()
        .filter(rs -> rs.getUserId().equals(userId) && rs.getChannelId().equals(channelId))
        .findFirst();
  }

  // read all
  @Override
  public List<ReadStatus> findAll() {
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
              return (ReadStatus) ois.readObject();
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
  public List<ReadStatus> findAllByChannelId(UUID channelId) {
    return findAll().stream()
        .filter(rs -> rs.getChannelId().equals(channelId))
        .toList();
  }

  @Override
  public List<ReadStatus> findAllByUserId(UUID userId) {
    return findAll().stream()
        .filter(rs -> rs.getUserId().equals(userId))
        .toList();
  }

  // delete
  @Override
  public void delete(UUID readStatusId) {
    Path path = resolvePath(readStatusId);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try {
      if (Files.notExists(path)) {
        throw new NoSuchElementException("ReadStatus를 찾을 수 없습니다: " + readStatusId);
      }
      Files.delete(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void deleteByUserId(UUID userId) {
    findByUserId(userId)
        .ifPresent(rs -> delete(rs.getId()));
  }

  @Override
  public void deleteByChannelId(UUID channelId) {
    findByChannelId(channelId)
        .ifPresent(rs -> delete(rs.getId()));
  }
}
