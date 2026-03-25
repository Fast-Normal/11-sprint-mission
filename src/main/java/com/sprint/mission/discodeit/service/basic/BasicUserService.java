package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentService binaryContentService;

    private UserDto toDto(User user, UserStatus userStatus) {
        return new UserDto(
                user.getId(),
                user.getUserName(),
                user.getUserEmail(),
                user.getProfileId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                userStatus.isOnline()
                // 패스워드 반환 안 함
        );
    }

    //create
    @Override
    public UserDto create(UserCreateRequest request){
        // 중복 이메일 검증
        if (userRepository.existByEmail(request.userEmail())) {
            throw new IllegalArgumentException(("중복된 이메일입니다." + request.userEmail()));
        }
        // 중복 이름 검증
        if (userRepository.existsByUsername(request.userName())) {
            throw new IllegalArgumentException(("중복된 이름입니다." + request.userName()));
        }
        // 프로필 이미지 선택 생성
        UUID profileId = null;
        if (request.profileImage() != null) {
            BinaryContentDto profile = binaryContentService.create(request.profileImage());
            profileId = profile.id();
        }
        // User 생성
        User user = new User(request.userName(), request.userEmail(), request.password(), profileId) ;
        userRepository.save(user);

        // UserStatus 자동 생성
        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);
        return toDto(user, userStatus);
    }

    //Read
    @Override
    public UserDto findById(UUID userId) {
        User user = findUserOrThrow(userId);
        UserStatus userStatus = findUserStatusOrThrow(userId);

        return toDto(user, userStatus);
    }

    //Read all
    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserStatus userStatus = findUserStatusOrThrow(user.getId());

                    return toDto(user, userStatus);
                })
                .toList();
    }

    //Update
    @Override
    public UserDto update(UUID userId, UserUpdateRequest request){
        User user = findUserOrThrow(userId);
        UserStatus userStatus = findUserStatusOrThrow(userId);

        // 선택적 프로필 이미지 교체. 이미지가 바뀌지 않을 때를 고려해 newProfileId에 기존 id값 저장
        UUID newProfileId = user.getProfileId();

        if (request.newProfileImage() != null) {
            if (user.getProfileId() != null) {
                binaryContentService.delete(user.getProfileId());
            }
            BinaryContentDto newProfile = binaryContentService.create(request.newProfileImage());
            newProfileId = newProfile.id();
        }
        // if문을 거쳐 새 이미지가 들어오면 newProfileId에 새 id를 넣고, 아니면 이전 id 그대로

        user.updateUserName(request.newUserName());
        user.updateUserEmail(request.newUserEmail());
        user.updatePassword(request.newPassword());
        user.updateUserProfile(newProfileId);

        return toDto(userRepository.save(user), userStatus);
    }

    //Delete
    @Override
    public void delete(UUID userId){
        User user = findUserOrThrow(userId);

        //프로필 이미지 삭제
        if (user.getProfileId() != null) {
            binaryContentService.delete(user.getProfileId());
        }

        //스테이터스 삭제
        userStatusRepository.deleteByUserId(userId);

        userRepository.delete(userId);
    }

    // 유저 아이디 검증 로직
    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당하는 유저가 없습니다." + userId));
    }

    private UserStatus findUserStatusOrThrow(UUID userId) {
        return userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException(("유저 스테이터스 정보가 없습니다." + userId)));
    }
}
