package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Channel extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 3001L;
    private String channelName;

    public Channel(String channelName) {
        super();
        this.channelName = channelName;
    }

    public void updateChannelName(String channelName){
        this.channelName = channelName;
        timeUpdated();
    }

    public String getChannelName() {
        return channelName;
    }

    public String toString() {
        return "[채널 이름: " + channelName + "]";
    }


}
