package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChannelRepository extends JpaRepository<Channel, UUID> {

  // findAllByUserId용 - PUBLIC 전체 + 내가 참여한 PRIVATE만
  @Query("""
      SELECT c FROM Channel c
      WHERE c.type = 'PUBLIC'
      OR c.id IN :myChannelIds
      """)
  List<Channel> findAllPublicOrIn(List<UUID> myChannelIds);

}
