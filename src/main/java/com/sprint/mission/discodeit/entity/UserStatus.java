package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends AbstractEntity{
    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID userId;
    private Instant lastActiveAt;

    public UserStatus(UUID userId) {
        super();
        this.userId = userId;
        this.lastActiveAt = Instant.now();
    }

    public void updateConnection() {
        this.lastActiveAt = Instant.now();
        timeUpdated();
    }


    public boolean isOnline() {
        return Instant.now().isAfter(lastActiveAt.plus(Duration.ofMinutes(5)));
    }

    public String toString() {
        return "userId: " + userId
                + ", lastConnectedAt: " + lastActiveAt;
    }
}

