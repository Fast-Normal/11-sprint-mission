package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    private UserStatusDto toDto(UserStatus userStatus) {
        return new UserStatusDto(
                userStatus.getId(),
                userStatus.getCreatedAt(),
                userStatus.getUpdatedAt(),
                userStatus.getUserId(),
                userStatus.getLastActiveAt()
        );
    }

    //create
    @Override
    public UserStatusDto create(UserStatusCreateRequest request){
        // 유저 검증
        userRepository.findById(request.userId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 유저입니다." + request.userId()));

        // 같은 유저의 스테이터스가 이미 존재하면 예외
        userStatusRepository.findByUserId(request.userId())
                .ifPresent(us -> {
                    throw new IllegalArgumentException("이미 존재하는 UserStatus입니다.");
                });

        UserStatus userStatus = new UserStatus(request.userId());
        userStatusRepository.save(userStatus);
        return toDto(userStatus);
    }

    //Read
    @Override
    public UserStatusDto findById(UUID userStatusId) {
        UserStatus userStatus = findUserStatusOrThrow(userStatusId);

        return toDto(userStatus);
    }

    @Override
    public UserStatusDto findByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(()-> new NoSuchElementException(("유저 스테이터스가 없습니다: " + userId)));
        return toDto(userStatus);
    }

    //Read all
    @Override
    public List<UserStatusDto> findAll() {
        return userStatusRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    //Update
    @Override
    public UserStatusDto update(UUID userStatusId){
        UserStatus userStatus = findUserStatusOrThrow(userStatusId);
        userStatus.updateConnection();
        return toDto(userStatusRepository.save(userStatus));
    }

    // 유저 아이디로 객체 업데이트
    @Override
    public UserStatusDto updateByUserId(UUID userId) {
        UserStatus userStatus = findUserStatusByUserIdOrThrow(userId);
        userStatus.updateConnection();
        return toDto(userStatusRepository.save(userStatus));
    }

    //Delete
    @Override
    public void delete(UUID userStatusId){
        findUserStatusOrThrow(userStatusId);
        //스테이터스 삭제
        userStatusRepository.delete(userStatusId);
    }

    // 유저 스테이터스 검증 로직
    private UserStatus findUserStatusOrThrow(UUID userStatusId) {
        return userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new NoSuchElementException("유저 스테이터스 정보가 없습니다." + userStatusId));
    }

    //스테이터스를 유저의 아이디로 검증하는 로직
    private UserStatus findUserStatusByUserIdOrThrow(UUID userId) {
        return userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException(("유저 스테이터스 정보가 없습니다." + userId)));
    }
}
