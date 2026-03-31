package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFUserRepository implements UserRepository {

  private final Map<UUID, User> data;

  public JCFUserRepository() {
    this.data = new HashMap<>();
  }

  //save
  @Override
  public User save(User user) {
    data.put(user.getId(), user);
    return user;
  }

  //Read
  @Override
  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(data.get(id));
  }

  // find by userName
  @Override
  public Optional<User> findByUserName(String userName) {
    return findAll().stream()
        .filter(u -> u.getUserName().equals(userName))
        .findFirst();
  }

  //Read all
  @Override
  public List<User> findAll() {
    return new ArrayList<>(data.values());
  }

  //Delete
  @Override
  public void delete(UUID id) {
    data.remove(id);
  }

  @Override
  public boolean existByEmail(String userEmail) {
    return data.values().stream()
        .anyMatch(user -> user.getUserEmail().equals(userEmail));
  }

  // existsById
  @Override
  public boolean existsById(UUID id) {
    return data.containsKey(id);
  }

  // existsByUsername
  @Override
  public boolean existsByUsername(String userName) {
    return findAll().stream()
        .anyMatch(user -> user.getUserName().equals(userName));
  }
}
