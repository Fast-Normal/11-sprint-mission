package com.sprint.mission.discodeit.entity;

import java.util.UUID;

public abstract class AbstractEntity {
    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    protected AbstractEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = null;
    }

    protected void timeUpdated() {
        this.updatedAt = System.currentTimeMillis();
    }

    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
