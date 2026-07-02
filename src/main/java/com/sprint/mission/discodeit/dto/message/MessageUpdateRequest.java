package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record MessageUpdateRequest(
    @NotBlank(message = "내용을 입력해주세요")
    String newContent,
        
    List<BinaryContentCreateRequest> attachments
) {

}
