package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    //create
    Channel create(ChannelType type, String channelName, String description);
    //read
    Channel findById(UUID channelId);
    //readAll
    List<Channel> findAll();
    //update
    Channel update(UUID channelId, String newChannelName);
    //delete
    void delete(UUID channelId);
}
