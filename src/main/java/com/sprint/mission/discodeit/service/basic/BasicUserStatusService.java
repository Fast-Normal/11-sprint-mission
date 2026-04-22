package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final UserStatusMapper userStatusMapper;

  //create
  @Transactional
  @Override
  public UserStatusDto create(UserStatusCreateRequest request) {
    // 유저 검증
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다." + request.userId()));

    // 같은 유저의 스테이터스가 이미 존재하면 예외
    userStatusRepository.findByUser_Id(request.userId())
        .ifPresent(us -> {
          throw new IllegalArgumentException("이미 존재하는 UserStatus입니다.");
        });

    UserStatus userStatus = new UserStatus(user);
    userStatusRepository.save(userStatus);
    return userStatusMapper.toDto(userStatus);
  }

  //Read
  @Override
  public UserStatusDto findById(UUID userStatusId) {
    UserStatus userStatus = findUserStatusOrThrow(userStatusId);

    return userStatusMapper.toDto(userStatus);
  }

  @Override
  public UserStatusDto findByUserId(UUID userId) {
    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> new NoSuchElementException(("유저 스테이터스가 없습니다: " + userId)));
    return userStatusMapper.toDto(userStatus);
  }

  //Read all
  @Override
  public List<UserStatusDto> findAll() {
    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }

  //Update
  @Transactional
  @Override
  public UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request) {
    UserStatus userStatus = findUserStatusOrThrow(userStatusId);
    userStatus.updateConnection(request.newLastActiveAt());
    return userStatusMapper.toDto(userStatus);
  }

  // 유저 아이디로 객체 업데이트
  @Transactional
  @Override
  public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    UserStatus userStatus = findUserStatusByUserIdOrThrow(userId);
    userStatus.updateConnection(request.newLastActiveAt());
    return userStatusMapper.toDto(userStatus);
  }

  //Delete
  @Transactional
  @Override
  public void delete(UUID userStatusId) {
    findUserStatusOrThrow(userStatusId);
    //스테이터스 삭제
    userStatusRepository.deleteById(userStatusId);
  }

  // 유저 스테이터스 검증 로직
  private UserStatus findUserStatusOrThrow(UUID userStatusId) {
    return userStatusRepository.findById(userStatusId)
        .orElseThrow(() -> new NoSuchElementException("유저 스테이터스 정보가 없습니다." + userStatusId));
  }

  //스테이터스를 유저의 아이디로 검증하는 로직
  private UserStatus findUserStatusByUserIdOrThrow(UUID userId) {
    return userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> new NoSuchElementException(("유저 스테이터스 정보가 없습니다." + userId)));
  }
}
