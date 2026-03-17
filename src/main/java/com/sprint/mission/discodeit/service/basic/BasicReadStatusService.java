package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;

    private ReadStatusDto toDto(ReadStatus readStatus) {
        return new ReadStatusDto(
                readStatus.getId(),
                readStatus.getCreatedAt(),
                readStatus.getUpdatedAt(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getLastReadAt()
        );
    }

    //create
    @Override
    public ReadStatusDto create(ReadStatusCreateRequest request){
        // 유저 검증
        userRepository.findById(request.userId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 유저입니다." + request.userId()));

        //채널 검증
        channelRepository.findById(request.channelId())
                        .orElseThrow(()-> new NoSuchElementException("존재하지 않는 채널입니다." + request.channelId()));

        // 같은 채널의 스테이터스가 이미 존재하면 예외
        readStatusRepository.findByUserIdAndChannelId(request.userId(), request.channelId())
                .ifPresent(rs -> {
                    throw new IllegalArgumentException("이미 존재하는 ReadStatus 입니다.");
                });

        ReadStatus readStatus = new ReadStatus(request.userId(), request.channelId());

        return toDto(readStatusRepository.save(readStatus));
    }

    //Read
    @Override
    public ReadStatusDto findById(UUID readStatusId) {
        return toDto(findReadStatusOrThrow(readStatusId));
    }

    //Read all
    @Override
    public List<ReadStatusDto> findAll() {
        return readStatusRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<ReadStatusDto> findAllByUserId(UUID userId) {
        return readStatusRepository.findAll().stream()
                .filter(rs-> rs.getUserId().equals(userId))
                .map(this::toDto)
                .toList();
    }

    //Update
    @Override
    public ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request){
        ReadStatus readStatus = findReadStatusOrThrow(readStatusId);
        readStatus.updateLastReadAt(request.newLastReadAt());
        return toDto(readStatusRepository.save(readStatus));
    }

    //Delete
    @Override
    public void delete(UUID readStatusId){
        findReadStatusOrThrow(readStatusId);
        //스테이터스 삭제
        readStatusRepository.delete(readStatusId);
    }

    // read 스테이터스 검증 로직
    private ReadStatus findReadStatusOrThrow(UUID readStatusId) {
        return readStatusRepository.findById(readStatusId)
                .orElseThrow(() -> new NoSuchElementException("ReadStatus 정보가 없습니다." + readStatusId));
    }
}
