package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinaryContentMapper {

  private final BinaryContentStorage binaryContentStorage;

  public BinaryContentDto toDto(BinaryContent binaryContent) {

    if (binaryContent == null) {
      return null;
    }

    byte[] bytes;
    try {
      bytes = binaryContentStorage.get(binaryContent.getId()).readAllBytes();
    } catch (IOException e) {
      bytes = new byte[0];
    }

    return new BinaryContentDto(
        binaryContent.getId(),
        binaryContent.getFileName(),
        binaryContent.getSize(),
        binaryContent.getContentType(),
        bytes
    );
  }

}
