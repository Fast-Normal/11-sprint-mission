package com.sprint.mission.discodeit.event.binaryContent;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentEventListener {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentService binaryContentService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(BinaryContentCreatedEvent event) {
    try {
      binaryContentStorage.put(event.binaryContentId(), event.bytes());
      binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.SUCCESS);
      log.info("바이너리 데이터 저장 완료 - id: {}", event.binaryContentId());
    } catch (Exception e) {
      log.error("바이너리 데이터 저장 실패 - id: {}", event.binaryContentId(), e);
      try {
        binaryContentService.updateStatus(event.binaryContentId(), BinaryContentStatus.FAIL);
      } catch (Exception ex) {
        log.error("상태 FAIL 업데이트 중 추가 예외 발생 - id: {}", event.binaryContentId(), ex);
      }
    }
  }
}
