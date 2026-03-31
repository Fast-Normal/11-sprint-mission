package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
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
public class FileMessageRepository implements MessageRepository {

  private final Path DIRECTORY;
  private final String EXTENSION = ".ser";
  private final FileLockProvider lockProvider;

  public FileMessageRepository(FileLockProvider lockProvider) {
    this.lockProvider = lockProvider;
    this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map",
        Message.class.getSimpleName());
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
  public Message save(Message message) {

    Path path = resolvePath(message.getId());
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try (
        FileOutputStream fos = new FileOutputStream(path.toFile());
        ObjectOutputStream oos = new ObjectOutputStream(fos)
    ) {
      oos.writeObject(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
    return message;
  }

  //read
  @Override
  public Optional<Message> findById(UUID messageId) {
    Path path = resolvePath(messageId);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();

    try {
      if (!Files.exists(path)) {
        return Optional.empty();
      }
      try (FileInputStream fis = new FileInputStream(path.toFile());
          ObjectInputStream ois = new ObjectInputStream(fis)) {
        return Optional.of((Message) ois.readObject());
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Optional<Message> findByChannelId(UUID channelId) {
    return findAll().stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .findFirst();
  }

  //readAll
  @Override
  public List<Message> findAll() {
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
              return (Message) ois.readObject();
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
  public List<Message> findAllByChannelId(UUID channelId) {
    return findAll().stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .toList();
  }


  //delete
  @Override
  public void delete(UUID messageId) {
    Path path = resolvePath(messageId);
    ReentrantLock lock = lockProvider.getLock(path);
    lock.lock();
    try {
      if (Files.notExists(path)) {
        throw new NoSuchElementException("메시지를 찾을 수 없습니다: " + messageId);
      }
      Files.delete(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  //delete by ChannelId
  @Override
  public void deleteByChannelId(UUID channelId) {
    findAllByChannelId(channelId)
        .forEach(msg -> delete(msg.getId()));
  }
}
