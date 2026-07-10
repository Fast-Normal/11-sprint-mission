package com.sprint.mission.discodeit.security.util;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationAuthGuard {

  private final NotificationRepository notificationRepository;

  // 알림 없으면 NotificationNotFoundException -> 404
  // 알림은 있는데 내것이 아니면 false 반환 -> 403
  // 알림이 내것이면 true -> 컨트롤러 메소드 실행
  public boolean isOwner(UUID notificationId, UUID userId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    return notification.getReceiver().getId().equals(userId);
  }
}
