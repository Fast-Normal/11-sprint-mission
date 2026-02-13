package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    //create
    Channel create(String channelName);
    //read
    Channel findById(UUID channelId);
    //readAll
    List<Channel> findAll();
    //update
    Channel update(UUID channelId, String newChannelName);
    //delete
    void delete(UUID channelId);
}
