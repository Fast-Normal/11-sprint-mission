package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Message extends AbstractEntity {

    private final UUID authorId;
    private final UUID channelId;
    private String content;

    public Message(UUID authorId, UUID channelId, String content) {
        super();
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getChannelId() {
        return channelId;
    }

    public String getContent() {
        return content;
    }

    public void updateContent(String content) {
        this.content = content;
        timeUpdated();
    }

    public String toString() {
        return "authorId: " + authorId + ", channelId:" + channelId + ", content: " + content  ;
    }
}
