package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    User user = new User("woody", "woody@test.com", "password1", null);
    user.initUserStatus();
    savedUser = userRepository.save(user);
  }

  // findByUsername
  @Test
  @DisplayName("findByUsername - 존재하는 username이면 User 반환")
  void findByUsername_success() {
    Optional<User> result = userRepository.findByUsername("woody");

    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("woody@test.com");
  }

  @Test
  @DisplayName("findByUsername - 존재하지 않는 username이면 empty 반환")
  void findByUsername_notFound() {
    Optional<User> result = userRepository.findByUsername("ghost");

    assertThat(result).isEmpty();
  }

  // existsByEmail
  @Test
  @DisplayName("existsByEmail - 존재하는 이메일이면 true 반환")
  void existsByEmail_true() {
    boolean result = userRepository.existsByEmail("woody@test.com");

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("existsByEmail - 존재하지 않는 이메일이면 false 반환")
  void existsByEmail_false() {
    boolean result = userRepository.existsByEmail("none@test.com");

    // then
    assertThat(result).isFalse();
  }

  //existsByUsername
  @Test
  @DisplayName("existsByUsername - 존재하는 username이면 true 반환")
  void existsByUsername_true() {
    boolean result = userRepository.existsByUsername("woody");

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("existsByUsername - 존재하지 않는 username이면 false 반환")
  void existsByUsername_false() {
    boolean result = userRepository.existsByUsername("none");

    // then
    assertThat(result).isFalse();
  }

  // findAllWithDetails (커스텀 쿼리)
  @Test
  @DisplayName("findAllWithDetails - UserStatus를 포함한 User 목록 반환")
  void findAllWithDetails_success() {
    List<User> users = userRepository.findAllWithDetails();

    assertThat(users).hasSize(1);
    assertThat(users.get(0).getUserStatus()).isNotNull();
  }

  @Test
  @DisplayName("findAllWithDetails - 데이터 없으면 빈 리스트 반환")
  void findAllWithDetails_empty() {
    userRepository.deleteAll();

    List<User> users = userRepository.findAllWithDetails();

    assertThat(users).isEmpty();
  }

  // findByIdWithDetails
  @Test
  @DisplayName("findByIdWithDetails - 존재하는 id로 조회 시 User 반환")
  void findByIdWIthDetails_success() {
    Optional<User> result = userRepository.findByIdWithDetails(savedUser.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("woody");
  }

  @Test
  @DisplayName("findByIdWithDetails - 존재하지 않는 id로 조회 시 empty 반환")
  void findByIdWithDetails_notFound() {
    Optional<User> result = userRepository.findByIdWithDetails(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

}
