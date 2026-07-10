package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadStatusRepository extends JpaRepository<ReadStatus, UUID> {

  Optional<ReadStatus> findByUser_IdAndChannel_Id(UUID userId, UUID channelId);

  List<ReadStatus> findAllByChannel_Id(UUID channelId);

  List<ReadStatus> findAllByUser_Id(UUID userId);

  @Query("SELECT rs FROM ReadStatus rs " +
      "JOIN FETCH rs.channel " +
      "WHERE rs.user.id = :userId")
  List<ReadStatus> findAllByUserIdWithChannel(UUID userId);

  // 채널 ID 목록으로 참여자 한번에 조회
  @Query("""
      SELECT rs FROM ReadStatus rs
      JOIN FETCH rs.user
      WHERE rs.channel.id IN :channelIds
      """)
  List<ReadStatus> findAllByChannelIds(List<UUID> channelIds);

  // 메시지가 등록된 채널에서 알림을 켜둔 + 작성자 본인은 제외한 사용자만 골라서 알림 보내야함
  @Query("""
         SELECT rs.user.id FROM ReadStatus rs
          WHERE rs.channel.id = :channelId
           AND rs.notificationEnabled = true
            AND rs.user.id <> :authorId
      """)
  List<UUID> findReceiverIdsByChannelIdAndNotificationEnabledTrueExcludingAuthor(
      @Param("channelId") UUID channelId,
      @Param("authorId") UUID authorId
  );

  void deleteAllByChannel_Id(UUID channelId);

}
