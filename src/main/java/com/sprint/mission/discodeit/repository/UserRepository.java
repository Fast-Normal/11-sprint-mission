package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

  @Query("SELECT u FROM User u " +
      "LEFT JOIN FETCH u.userStatus " +
      "LEFT JOIN FETCH u.profile")
  List<User> findAllWithDetails();

  // 단건 조회도
  @Query("SELECT u FROM User u " +
      "LEFT JOIN FETCH u.userStatus " +
      "LEFT JOIN FETCH u.profile " +
      "WHERE u.id = :id")
  Optional<User> findByIdWithDetails(UUID id);

  Optional<User> findByUsername(String username);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
}
