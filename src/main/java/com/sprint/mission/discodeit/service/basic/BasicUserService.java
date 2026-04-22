package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;

  //create
  @Transactional
  @Override
  public UserDto create(UserCreateRequest request) {
    // 중복 이메일 검증
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException(("중복된 이메일입니다." + request.email()));
    }
    // 중복 이름 검증
    if (userRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException(("중복된 이름입니다." + request.username()));
    }
    // 프로필 이미지 선택 생성
    BinaryContent profile = null;
    if (request.profileImage() != null) {
      // 메타정보만 db 저장
      profile = new BinaryContent(
          request.profileImage().contentType(),
          request.profileImage().bytes()
      );
      binaryContentRepository.save(profile);
      // 실제 파일은 storage에 저장
      binaryContentStorage.put(profile.getId(), request.profileImage().bytes());
    }

    // User 생성
    User user = new User(request.username(), request.email(), request.password(), profile);
    // UserStatus 자동 생성
    user.initUserStatus();

    userRepository.save(user);

    return userMapper.toDto(user); //UserStatus도 cascade로 자동 저장
  }

  //Read
  @Override
  public UserDto findById(UUID userId) {
    User user = findUserOrThrow(userId);

    return userMapper.toDto(user);
  }

  //Read all
  @Override
  public List<UserDto> findAll() {
    return userRepository.findAllWithDetails().stream()
        .map(userMapper::toDto)
        .toList();
  }

  //Update
  @Transactional
  @Override
  public UserDto update(UUID userId, UserUpdateRequest request) {
    User user = findUserOrThrow(userId);

    if (request.newProfileImage() != null) {
      if (user.getProfile() != null) {
        binaryContentRepository.delete(user.getProfile());
      }
      BinaryContent newProfile = new BinaryContent(
          request.newProfileImage().contentType(),
          request.newProfileImage().bytes()
      );
      BinaryContent saved = binaryContentRepository.save(newProfile);
      binaryContentStorage.put(newProfile.getId(), request.newProfileImage().bytes());
      user.updateUserProfile(saved);
    }

    // 중복 이메일 검증
    if (!user.getEmail().equals(request.newEmail()) && userRepository.existsByEmail(
        request.newEmail())) {
      throw new IllegalArgumentException(("사용중인 이메일입니다." + request.newEmail()));
    }
    // 중복 이름 검증
    if (!user.getUsername().equals(request.newUsername())
        && userRepository.existsByUsername(request.newUsername())) {
      throw new IllegalArgumentException(("사용중인 이름입니다." + request.newUsername()));
    }

    user.updateUserName(request.newUsername());
    user.updateUserEmail(request.newEmail());
    user.updatePassword(request.newPassword());

    return userMapper.toDto(user);
  }

  //Delete
  @Transactional
  @Override
  public void delete(UUID userId) {
    User user = findUserOrThrow(userId);

    //프로필 이미지 삭제
    if (user.getProfile() != null) {
      binaryContentStorage.delete(user.getProfile().getId());
      binaryContentRepository.deleteById(user.getProfile().getId());
    }

    userRepository.deleteById(userId); //JPA가 UserStatus도 cascade 삭제
  }

  // 유저 아이디 검증 로직
  private User findUserOrThrow(UUID userId) {
    return userRepository.findByIdWithDetails(userId)
        .orElseThrow(() -> new NoSuchElementException("해당하는 유저가 없습니다." + userId));
  }

}
