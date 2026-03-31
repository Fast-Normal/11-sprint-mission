package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
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
public class FileUserStatusRepository implements UserStatusRepository {

  private final Path DIRECTORY;
  private final String EXTENSION = ".ser";
  private final FileLockProvider lockProvider;

  public FileUserStatusRepository(FileLockProvider lockProvider) {
    this.lockProvider = lockProvider;
    this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map",
        UserStatus.class.getSimpleName());
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
  public UserStatus save(UserStatus userStatus) {
    Path path = resolvePath(userStatus.getId());
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try (FileOutputStream fos = new FileOutputStream(path.toFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos)
    ) {
      oos.writeObject(userStatus);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    return userStatus;
  }

  // read
  @Override
  public Optional<UserStatus> findById(UUID id) {
    Path path = resolvePath(id);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();

    try {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
      try (FileInputStream fis = new FileInputStream(path.toFile());
          ObjectInputStream ois = new ObjectInputStream(fis)
      ) {
        return Optional.of((UserStatus) ois.readObject());
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Optional<UserStatus> findByUserId(UUID userId) {
    return findAll().stream()
        .filter(us -> us.getUserId().equals(userId))
        .findFirst();
  }

  // read all
  @Override
  public List<UserStatus> findAll() {
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
              return (UserStatus) ois.readObject();
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

  // delete
  @Override
  public void delete(UUID id) {
    Path path = resolvePath(id);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try {
      if (Files.notExists(path)) {
        throw new NoSuchElementException("유저 스테이터스를 찾을 수 없습니다: " + id);
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
        .ifPresent(us -> delete(us.getId()));
  }
}
