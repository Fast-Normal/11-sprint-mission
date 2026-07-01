package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private BasicUserService userService;

  // create

  @Nested
  @DisplayName("create()")
  class CreateUser {

    @Test
    @DisplayName("성공: 중복 없는 이메일/이름으로 유저를 생성한다")
    void create_success() {
      // given
      UserCreateRequest request = new UserCreateRequest("woody", "woody@test.com", "pass1234");

      UserDto expectedDto = new UserDto(UUID.randomUUID(), "woody", "woody@test.com", null, true,
          Role.USER);

      given(userRepository.existsByEmail(request.email())).willReturn(false);
      given(userRepository.existsByUsername(request.username())).willReturn(false);
      given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
      given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

      // when
      UserDto result = userService.create(request, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.username()).isEqualTo("woody");
      then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("실패: 이메일 중복 시 예외 발생")
    void create_fail_duplicateEmail() {
      // given
      UserCreateRequest request = new UserCreateRequest("woody", "dup@test.com", "pass1234");

      given(userRepository.existsByEmail(request.email())).willReturn(true);

      // when & then
      assertThatThrownBy(() -> userService.create(request, null))
          .isInstanceOf(DiscodeitException.class)
          .satisfies(e -> {
            DiscodeitException ex = (DiscodeitException) e;
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
          });
    }
  }

  // update

  @Nested
  @DisplayName("update()")
  class UpdateUser {

    @Test
    @DisplayName("성공: 유효한 userId와 요청으로 유저 정보 수정")
    void update_success() {
      // given
      UUID userId = UUID.randomUUID();
      User existingUser = new User("woody", "woody@test.com", "pass1234", null);
      UserUpdateRequest request = new UserUpdateRequest(
          "woody2", "woody2@test.com", "newpassword1");
      UserDto expectedDto = new UserDto(userId, "woody2", "woody2@test.com", null, true, Role.USER);

      given(userRepository.findByIdWithDetails(userId)).willReturn(Optional.of(existingUser));
      given(userRepository.existsByEmail(request.newEmail())).willReturn(false);
      given(userRepository.existsByUsername(request.newUsername())).willReturn(false);
      given(userMapper.toDto(existingUser)).willReturn(expectedDto);

      // when
      UserDto result = userService.update(userId, request, null);

      // then
      assertThat(result.username()).isEqualTo("woody2");
      assertThat(result.email()).isEqualTo("woody2@test.com");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 userId면 예외 발생")
    void update_fail_userNotFound() {
      // given
      UUID unknownId = UUID.randomUUID();
      UserUpdateRequest request = new UserUpdateRequest(
          "woody2", "woody2@test.com", "newpassword1");

      given(userRepository.findByIdWithDetails(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.update(unknownId, request, null))
          .isInstanceOf(DiscodeitException.class)
          .satisfies(e -> {
            DiscodeitException ex = (DiscodeitException) e;
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("delete()")
  class DeleteUser {

    @Test
    @DisplayName("성공: 존재하는 userId로 유저 삭제")
    void delete_success() {
      // given
      UUID userId = UUID.randomUUID();
      User user = new User("woody", "woody@test.com", "pass1234", null);

      given(userRepository.findByIdWithDetails(userId)).willReturn(Optional.of(user));
      willDoNothing().given(userRepository).deleteById(userId);

      // when
      userService.delete(userId);

      // then
      then(userRepository).should().deleteById(userId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 Id면 예외 발생")
    void delete_fail_userNotFound() {
      // given
      UUID unknownId = UUID.randomUUID();

      given(userRepository.findByIdWithDetails(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.delete(unknownId))
          .isInstanceOf(DiscodeitException.class)
          .satisfies(e -> {
            DiscodeitException ex = (DiscodeitException) e;
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
          });
      then(userRepository).should().findByIdWithDetails(unknownId);
      then(userRepository).shouldHaveNoMoreInteractions();
    }
  }

}
