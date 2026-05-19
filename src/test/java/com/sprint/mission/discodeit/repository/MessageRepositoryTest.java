package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

  @Autowired
  MessageRepository messageRepository;

  @Autowired
  UserRepository userRepository;

  @Autowired
  ChannelRepository channelRepository;

  private User author;
  private Channel channel;
  private Message msg1;
  private Message msg2;

  @BeforeEach
  void setUp() throws InterruptedException {
    author = userRepository.save(
        new User("author", "author@email.com", "password1", null)
    );

    channel = channelRepository.save(
        new Channel(ChannelType.PUBLIC, "테스트채널", "설명")
    );

    msg1 = messageRepository.save(new Message(author, channel, "첫 번째 메시지"));
    // createdAt이 동일해지지 않도록 짧게 대기
    Thread.sleep(100);
    msg2 = messageRepository.save(new Message(author, channel, "두 번째 메시지"));

    // flush 후 재조회로 DB 실제값 동기화
    msg2 = messageRepository.findById(msg2.getId()).get();
  }

  // findAllByChannel_IdOrderByCreatedAtDesc

  @Test
  @DisplayName("findAllByChannel_IdOrderByCreatedAtDesc - 채널 메시지를 최신순 페이징 반환")
  void findAllByChannel_paging_success() {
    Pageable pageable = PageRequest.of(0, 10);

    Slice<Message> slice = messageRepository
        .findAllByChannel_IdOrderByCreatedAtDesc(channel.getId(), pageable);

    assertThat(slice.getContent()).hasSize(2);
    assertThat(slice.getContent().get(0).getContent()).isEqualTo("두 번째 메시지");
  }

  @Test
  @DisplayName("findAllByChannel_IdOrderByCreatedAtDesc - 다른 채널 ID로 조회 시 빈 Slice 반환")
  void findAllByChannel_paging_wrongChannel() {
    Slice<Message> slice = messageRepository
        .findAllByChannel_IdOrderByCreatedAtDesc(UUID.randomUUID(), PageRequest.of(0, 10));

    assertThat(slice.getContent()).isEmpty();
  }

  // findAllByChannelIdWithCursor

  @Test
  @DisplayName("findAllByChannelIdWithCursor - cursor 없으면 전체 반환")
  void findWithCursor_noCursor() {
    Slice<Message> slice = messageRepository
        .findALLByChannelIdWithCursor(channel.getId(), null, PageRequest.of(0, 10));

    assertThat(slice.getContent()).hasSize(2);
  }

  @Test
  @DisplayName("findAllByChannelIdWithCursor - cursor 이전 메시지만 반환")
  void findWithCursor_withCursor() {
    Instant cursor = msg2.getCreatedAt();

    Slice<Message> slice = messageRepository
        .findALLByChannelIdWithCursor(channel.getId(), cursor, PageRequest.of(0, 10));

    assertThat(slice.getContent()).hasSize(1);
    assertThat(slice.getContent().get(0).getContent()).isEqualTo("첫 번째 메시지");
  }

  // findTopByChannel_IdOrderByCreatedAtDesc
  @Test
  @DisplayName("findTopByChannel_IdOrderByCreatedAtDesc - 채널의 가장 최신 메시지 반환")
  void findTop_success() {
    Optional<Message> top = messageRepository
        .findTopByChannel_IdOrderByCreatedAtDesc(channel.getId());

    assertThat(top).isPresent();
    assertThat(top.get().getContent()).isEqualTo("두 번째 메시지");
  }

  @Test
  @DisplayName("findTopByChannel_IdOrderByCreatedAtDesc - 메시지 없는 채널은 empty 반환")
  void findTop_emptyChannel() {
    Channel emptyChannel = channelRepository.save(
        new Channel(ChannelType.PUBLIC, "빈 채널", null)
    );

    Optional<Message> top = messageRepository
        .findTopByChannel_IdOrderByCreatedAtDesc(emptyChannel.getId());

    assertThat(top).isEmpty();
  }

  //findLastMessagesByChannelIds
  @Test
  @DisplayName("findLastMessagesByChannelIds - 채널별 마지막 메시지 반환")
  void findLastMessages_success() {
    List<Message> last = messageRepository
        .findLastMessagesByChannelIds(List.of(channel.getId()));

    assertThat(last).hasSize(1);
    assertThat(last.get(0).getContent()).isEqualTo("두 번째 메시지");
  }

  @Test
  @DisplayName("findLastMessagesByChannelIds - 존재하지 않는 채널 id면 빈 리스트 반환")
  void findLastMessages_notFound() {
    List<Message> last = messageRepository
        .findLastMessagesByChannelIds(List.of(UUID.randomUUID()));

    assertThat(last).isEmpty();
  }

  // deleteAllByChannel_Id
  @Test
  @DisplayName("deleteAllByChannel_Id - 채널 메시지 전체 삭제")
  void deleteAllByChannel_success() {
    messageRepository.deleteAllByChannel_Id(channel.getId());

    assertThat(messageRepository.findAllByChannel_Id(channel.getId())).isEmpty();
  }

  @Test
  @DisplayName("deleteAllByChannel_Id - 다른 채널 메시지는 삭제되지 않음")
  void deleteAllByChannel_otherChannelUnaffected() {
    Channel other = channelRepository.save(
        new Channel(ChannelType.PUBLIC, "다른 채널", null)
    );
    messageRepository.save(new Message(author, other, "다른채널 메시지"));

    messageRepository.deleteAllByChannel_Id(channel.getId());

    assertThat(messageRepository.findAllByChannel_Id(other.getId())).hasSize(1);
  }
}
