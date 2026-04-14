package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicBinaryContentService implements BinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
      "image/jpeg", "image/png", "image/gif", "image/webp"
  );

  //create
  @Transactional
  @Override
  public BinaryContentDto create(BinaryContentCreateRequest request) {
    // 용량 제한
    if (request.bytes().length > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("파일 크기 초과: 최대 10MB");
    }
    // 확장자 제한
    if (!ALLOWED_CONTENT_TYPES.contains(request.contentType())) {
      throw new IllegalArgumentException("허용되지 않는 확장자: " + request.contentType());
    }

    BinaryContent binaryContent = new BinaryContent(
        request.contentType(),
        request.bytes().length
    );

    binaryContentRepository.save(binaryContent);
    binaryContentStorage.put(binaryContent.getId(), request.bytes());

    return binaryContentMapper.toDto(binaryContent);
  }

  //Read
  @Override
  public BinaryContentDto findById(UUID binaryContentId) {
    return binaryContentMapper.toDto(binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> new NoSuchElementException("컨텐츠를 찾을 수 없습니다.")));
  }

  //Read all
  @Override
  public List<BinaryContentDto> findAllByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids)
        .stream()
        .map(binaryContentMapper::toDto)
        .toList();
  }

  //Delete
  @Transactional
  @Override
  public void delete(UUID binaryContentId) {
    binaryContentRepository.findById(binaryContentId)
        .orElseThrow(() -> new NoSuchElementException("컨텐츠를 찾을 수 없습니다." + binaryContentId));

    binaryContentStorage.delete(binaryContentId);
    binaryContentRepository.deleteById(binaryContentId);
  }
}
