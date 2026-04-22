package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

  void deleteAllByChannel_Id(UUID channelId);

}
