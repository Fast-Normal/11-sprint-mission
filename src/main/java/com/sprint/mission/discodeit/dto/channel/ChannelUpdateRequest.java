package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.Size;

public record ChannelUpdateRequest(
    @Size(min = 2, max = 20, message = "채널 제목은 2~20자여야 합니다.")
    String newName,

    @Size(max = 500, message = "500자 내로 작성해야 합니다.")
    String newDescription
) {

}
