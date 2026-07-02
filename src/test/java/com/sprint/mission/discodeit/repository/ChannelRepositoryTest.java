package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChannelRepositoryTest {

  @Autowired
  ChannelRepository channelRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  ReadStatusRepository readStatusRepository;

  private Channel publicChannel;
  private Channel privateChannel;
  private User participant;

  @BeforeEach
  void setUp() {
    participant = userRepository.save(
        new User("member", "member@test.com", "password1", null));

    publicChannel = channelRepository.save(
        new Channel(ChannelType.PUBLIC, "공개채널", "설명")
    );

    privateChannel = channelRepository.save(
        new Channel(ChannelType.PRIVATE, null, null)
    );

    // participant가 privateChannel에 참여중
    readStatusRepository.save(new ReadStatus(participant, privateChannel));
  }

  // findAllPublicOrIn

  @Test
  @DisplayName("findAllPulbicOrIn - PUBLIC 채널 + 참여중인 PRIVATE 채널 함께 반환")
  void findAllPublicOrIn_includesPrivate() {
    List<UUID> myChannelIds = List.of(privateChannel.getId());

    List<Channel> result = channelRepository.findAllPublicOrIn(myChannelIds);

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Channel::getType)
        .containsExactlyInAnyOrder(ChannelType.PUBLIC, ChannelType.PRIVATE);
  }

  @Test
  @DisplayName("findAllPublicOrIn - 참여 채널 없을 때 Public만 반환")
  void findAllPublicOrIn_onlyPublic() {
    List<Channel> result = channelRepository.findAllPublicOrIn(List.of());

    assertThat(result).extracting(Channel::getType)
        .containsOnly(ChannelType.PUBLIC);
  }

  // findById
  @Test
  @DisplayName("findById - 존재하는 채널 ID 조회 성공")
  void findById_success() {
    var result = channelRepository.findById(publicChannel.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("공개채널");
  }

  @Test
  @DisplayName("findById - 존재하지 않는 채널 id 조회 시 empty")
  void findById_notFound() {
    var result = channelRepository.findById(UUID.randomUUID());

    assertThat(result).isEmpty();
  }

  // update
  @Test
  @DisplayName("채널명 수정 후 저장 시 반영됨")
  void update_channelName() {
    publicChannel.updateChannelName("수정된 채널");
    channelRepository.save(publicChannel);
    channelRepository.flush();

    Channel found = channelRepository.findById(publicChannel.getId()).get();
    assertThat(found.getName()).isEqualTo("수정된 채널");
  }

  // delete
  @Test
  @DisplayName("deleteById 후 조회 시 empty 반환")
  void delete_success() {
    channelRepository.deleteById(publicChannel.getId());

    assertThat(channelRepository.findById(publicChannel.getId())).isEmpty();
  }

}
