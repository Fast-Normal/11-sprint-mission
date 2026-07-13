package com.sprint.mission.discodeit.event.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class S3UploadFailureNotifier {

  @Async
  @EventListener
  public void on(S3UploadFailedEvent event) {
    log.error("""
        [관리자 알림] 비동기 작업 실패
        작업: {}
        RequestId: {}
        BinaryContentId: {}
        Error: {}
        """, event.taskName(), event.requestId(), event.binaryContentId(), event.errorMessage());
  }

}
