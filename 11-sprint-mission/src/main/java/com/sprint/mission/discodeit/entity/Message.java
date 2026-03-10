package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Message extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 2001L;
    private final User sender;
    private final Channel channel;
    private String content;

    public Message(User sender, Channel channel, String content) {
        super();
        this.sender = sender;
        this.channel = channel;
        this.content = content;
    }

    public User getSender() {
        return sender;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getContent() {
        return content;
    }

    public void updateContent(String content) {
        this.content = content;
        timeUpdated();
    }

    public String toString() {
        return sender + "이(가) " + channel + " 채널에서 \"" + content + "\"의 메시지를 보냄";
    }
}
