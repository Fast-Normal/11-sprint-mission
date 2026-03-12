package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public class Channel extends AbstractEntity {

    private String channelName;
    private ChannelType type;
    private String description;

    public Channel(ChannelType type, String channelName, String description) {
        super();
        this.channelName = channelName;
        this.type = type;
        this.description = description;
    }

    public void updateChannelName(String channelName){
        this.channelName = channelName;
        timeUpdated();
    }

    public String toString() {
        return "channelName: " + channelName + ", type: " + type + ", description: " + description;
    }


}
