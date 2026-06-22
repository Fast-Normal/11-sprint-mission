package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.InvalidPasswordException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundByUsernameException;
import com.sprint.mission.discodeit.exception.userStatus.UserStatusNotFoundByUserIdException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  @Override
  public UserDto login(LoginRequest request) {
    User user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new UserNotFoundByUsernameException(request.username()));

    if (passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new InvalidPasswordException();
    }

    UserStatus userStatus = userStatusRepository.findByUser_Id(user.getId())
        .orElseThrow(() -> new UserStatusNotFoundByUserIdException(user.getId()));

    userStatus.updateConnection(Instant.now());

    return userMapper.toDto(user);
  }
}
