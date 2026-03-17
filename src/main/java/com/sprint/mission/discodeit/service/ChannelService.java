package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    //create public channel
    ChannelDto createPublicChannel(PublicChannelCreateRequest request);

    //create private channel
    ChannelDto createPrivateChannel(PrivateChannelCreateRequest request);

    //read
    ChannelDto findById(UUID channelId);

    List<ChannelDto> findAllByUserId(UUID userId);

    //readAll
    List<ChannelDto> findAll();

    //update
    ChannelDto update(UUID channelId, ChannelUpdateRequest request);

    //delete
    void delete(UUID channelId);
}
