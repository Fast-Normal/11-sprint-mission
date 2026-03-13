package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends AbstractEntity {

    private final UUID authorId;
    private final UUID channelId;
    private String content;
    private List<UUID> attachmentIds = new ArrayList<>();

    public Message(UUID authorId, UUID channelId, String content) {
        super();
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
    }

    public void updateContent(String content) {
        this.content = content;
        timeUpdated();
    }

    public void attachFile(UUID binaryContentId) {
        attachmentIds.add(binaryContentId);
        timeUpdated();
    }

    public String toString() {
        return "authorId: " + authorId + ", channelId:" + channelId + ", content: " + content  ;
    }
}
