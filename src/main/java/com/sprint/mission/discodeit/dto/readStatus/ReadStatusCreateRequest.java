package com.sprint.mission.discodeit.dto.readStatus;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReadStatusCreateRequest(
    @NotNull
    UUID userId,

    @NotNull
    UUID channelId
) {

}
