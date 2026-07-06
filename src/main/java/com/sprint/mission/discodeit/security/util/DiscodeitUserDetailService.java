package com.sprint.mission.discodeit.security.util;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscodeitUserDetailService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다. " + username));

    log.info("유저 찾기 성공: {}", user);

    UserDto userDto = userMapper.toDto(user);

    return new DiscodeitUserDetails(userDto, user.getPassword());
  }

  public UserDetails loadUserByUserId(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다. id=" + userId));

    UserDto userDto = userMapper.toDto(user);
    return new DiscodeitUserDetails(userDto, user.getPassword());
  }

}
