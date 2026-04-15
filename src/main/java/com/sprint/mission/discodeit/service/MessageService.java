package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface MessageService {

  //create
  MessageDto create(MessageCreateRequest request);

  //read
  MessageDto findById(UUID messageId);

  //readAll
  PageResponse<MessageDto> findAllByChannelId(UUID channelId, Instant cursor, Pageable pageable);

  //update
  MessageDto update(UUID messageId, MessageUpdateRequest request);

  //delete
  void delete(UUID messageId);
}
