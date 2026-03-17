package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    //create
    MessageDto create(MessageCreateRequest request);
    //read
    MessageDto findById(UUID messageId);
    //readAll
    List<MessageDto> findAllByChannelId(UUID channelId);
    //update
    MessageDto update(UUID messageId, MessageUpdateRequest request);
    //delete
    void delete(UUID messageId);
}
