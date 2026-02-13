package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    //create
    Message create(User sender, String content, Channel channel);
    //read
    Message findById(UUID messageId);
    //readAll
    List<Message> findAll();
    //update
    Message update(UUID messageId, String newContent);
    //delete
    void delete(UUID messageId);
}
