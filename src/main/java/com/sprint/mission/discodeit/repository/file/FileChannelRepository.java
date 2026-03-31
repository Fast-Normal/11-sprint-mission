package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
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
public class FileChannelRepository implements ChannelRepository {

  private final Path DIRECTORY;
  private final String EXTENSION = ".ser";
  private final FileLockProvider lockProvider;

  public FileChannelRepository(FileLockProvider lockProvider) {
    this.lockProvider = lockProvider;
    this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map",
        Channel.class.getSimpleName());
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

  //create
  @Override
  public Channel save(Channel channel) {
    Path path = resolvePath(channel.getId());
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try (
        FileOutputStream fos = new FileOutputStream(path.toFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos)
    ) {
      oos.writeObject(channel);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    return channel;
  }

  //read
  @Override
  public Optional<Channel> findById(UUID channelId) {
    Path path = resolvePath(channelId);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try {
        if (!Files.exists(path)) {
            return Optional.empty();
        }
      try (
          FileInputStream fis = new FileInputStream(path.toFile());
          ObjectInputStream ois = new ObjectInputStream(fis)) {
        return Optional.of((Channel) ois.readObject());
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } finally {
      lock.unlock();
    }
  }

  //readAll
  @Override
  public List<Channel> findAll() {
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
              return (Channel) ois.readObject();
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


  //delete
  @Override
  public void delete(UUID channelId) {
    Path path = resolvePath(channelId);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try {
      if (Files.notExists(path)) {
        throw new NoSuchElementException("채널을 찾을 수 없습니다: " + channelId);
      }
      Files.delete(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }


  // existsById
  @Override
  public boolean existsById(UUID id) {
    Path path = resolvePath(id);
    return Files.exists(path);
  }
}
