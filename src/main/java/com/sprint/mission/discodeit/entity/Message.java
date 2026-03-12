package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends AbstractEntity {

    private final UUID authorId;
    private final UUID channelId;
    private String content;
    private List<UUID> attachmentIds;

    public Message(UUID authorId, UUID channelId, String content, List<UUID> attachmentIds) {
        super();
        this.authorId = authorId;
        this.channelId = channelId;
        this.content = content;
        this.attachmentIds = attachmentIds != null ? attachmentIds : new ArrayList<>();
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
