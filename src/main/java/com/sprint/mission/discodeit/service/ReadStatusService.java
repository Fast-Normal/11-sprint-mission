package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ReadStatusService {

    //Create
    ReadStatusDto create(ReadStatusCreateRequest request);

    //Read
    ReadStatusDto findById(UUID readStatusId);

    //Read all
    List<ReadStatusDto> findAll();

    List<ReadStatusDto> findAllByUserId(UUID userId);

    //Update
    ReadStatusDto update(UUID readStatusId, ReadStatusUpdateRequest request);

    //Delete
    void delete(UUID readStatusId);

}
