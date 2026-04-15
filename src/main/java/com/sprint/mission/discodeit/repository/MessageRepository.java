package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, UUID> {


  Slice<Message> findAllByChannel_IdOrderByCreatedAtDesc(UUID channelId, Pageable pageable);

  @Query("SELECT m FROM Message m " +
      "LEFT JOIN FETCH m.author " +
      "LEFT JOIN FETCH m.attachments " +
      "WHERE m.channel.id = :channelId " +
      "ORDER BY m.createdAt DESC")
  Slice<Message> findAllByChannel_IdWithDetails(UUID channelId, Pageable pageable);

  @Query("SELECT m FROM Message m " +
      "LEFT JOIN FETCH m.author " +
      "LEFT JOIN FETCH m.attachments " +
      "WHERE m.channel.id = :channelId " +
      "AND (:cursor IS NULL OR m.createdAt < :cursor) " +
      "ORDER BY m.createdAt DESC")
  Slice<Message> findALLByChannelIdWithCursor(UUID channelId, Instant cursor, Pageable pageable);

  Optional<Message> findTopByChannel_IdOrderByCreatedAtDesc(UUID channelId);

  List<Message> findAllByChannel_Id(UUID channelId);

  void deleteAllByChannel_Id(UUID channelId);

}
