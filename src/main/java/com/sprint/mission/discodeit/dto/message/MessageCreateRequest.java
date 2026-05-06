package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(
    @NotNull
    UUID authorId,

    @NotNull
    UUID channelId,

    @NotBlank(message = "내용이 비어있을 수 없습니다.")
    String content,

    List<BinaryContentCreateRequest> attachments
) {

}
