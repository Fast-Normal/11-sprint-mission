package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

    //Create
    BinaryContentDto create(BinaryContentCreateRequest request);

    //Read
    BinaryContentDto findById(UUID binaryContentId);

    //Read all
    List<BinaryContentDto> findAllByIdIn(List<UUID> ids);

    //Delete
    void delete(UUID binaryContentId);

}
