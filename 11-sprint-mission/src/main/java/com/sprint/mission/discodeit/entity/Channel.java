package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Channel extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 3001L;
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

    public String getChannelName() {
        return channelName;
    }

    public ChannelType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "[채널 이름: " + channelName + "]";
    }


}
