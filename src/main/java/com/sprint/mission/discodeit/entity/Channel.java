package com.sprint.mission.discodeit.entity;

import lombok.Getter;


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

    public void updateChannelType(ChannelType type){
        this.type = type;
        timeUpdated();
    }

    public void updateChannelDescription(String description){
        this.description = description;
        timeUpdated();
    }

    public String toString() {
        return "channelName: " + channelName + ", type: " + type + ", description: " + description;
    }


}
