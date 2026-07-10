package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;

  private static final int CONTENT_PREVIEW_LENGTH = 100;

  @Transactional
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(MessageCreatedEvent event) {
    log.debug("메시지 알림 생성 시작 - channelId: {}", event.channelId());

    List<UUID> receiverIds = readStatusRepository
        .findReceiverIdsByChannelIdAndNotificationEnabledTrueExcludingAuthor(
            event.channelId(), event.authorId());
    if (receiverIds.isEmpty()) {
      log.debug("알림 대상 없음 - channelId: {}", event.channelId());
      return;
    }

    User author = userRepository.findById(event.authorId())
        .orElseThrow(() -> new UserNotFoundException(event.authorId()));

    List<User> receivers = userRepository.findAllById(receiverIds);
    String title = String.format("%s (#%s)", author.getUsername(), event.channelName());
    String content = truncate(event.content());

    List<Notification> notifications = receivers.stream()
        .map(receiver -> new Notification(receiver, title, content))
        .toList();

    notificationRepository.saveAll(notifications);
    log.info("메시지 알림 생성 완료 - channelId: {}, 수신자 수: {}",
        event.channelId(), notifications.size());
  }

  @Transactional
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(RoleUpdatedEvent event) {
    log.debug("권한 변경 알림 생성 시작 - userId: {}", event.userId());

    User user = userRepository.findById(event.userId())
        .orElseThrow(() -> new UserNotFoundException(event.userId()));

    String title = "권한이 변경되었습니다.";
    String content = String.format("%s -> %s", event.oldRole(), event.newRole());

    notificationRepository.save(new Notification(user, title, content));
    log.info("권한 변경 알림 생성 완료 - userId: {}", event.userId());
  }

  private String truncate(String content) {
    if (content == null) {
      return "";
    }
    return content.length() > CONTENT_PREVIEW_LENGTH
        ? content.substring(0, CONTENT_PREVIEW_LENGTH) + "..."
        : content;
  }

}
