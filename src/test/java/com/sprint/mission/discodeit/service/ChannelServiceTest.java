package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private ChannelMapper channelMapper;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private BasicChannelService channelService;

  // createPublicChannel
  @Nested
  @DisplayName("createPublicChannel()")
  class CreatePublicChannel {

    @Test
    @DisplayName("성공: Public 채널을 생성")
    void createPublicChannel_success() {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest("공지사항", "서버 공지 채널");

      Channel channel = new Channel(ChannelType.PUBLIC, "공지사항", "서버 공지 채널");
      ChannelDto expectedDto = new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "공지사항",
          "서버 공지 채널", null, null);

      given(channelRepository.save(any(Channel.class))).willReturn(channel);
      given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

      // when
      ChannelDto result = channelService.createPublicChannel(request);

      // then
      assertThat(result).isNotNull();
      assertThat(result.type()).isEqualTo(ChannelType.PUBLIC);
      assertThat(result.name()).isEqualTo("공지사항");
      then(channelRepository).should().save(any(Channel.class));
    }

  }

  // createPrivateChannel
  @Nested
  @DisplayName("createPrivateChannel()")
  class CreatePrivateChannel {

    @Test
    @DisplayName("성공: 유효한 참여자 ID로 Private 채널을 생성")
    void createPrivateChannel_success() {
      // given
      UUID userId1 = UUID.randomUUID();
      UUID userId2 = UUID.randomUUID();

      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
          List.of(userId1, userId2));
      Channel channel = new Channel(ChannelType.PRIVATE, null, null);
      User user1 = new User("woody", "woody@test.com", "password1", null);
      User user2 = new User("buzz", "buzz@test.com", "password2", null);
      ChannelDto expectedDto = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE, null, null,
          List.of(), null);

      given(channelRepository.save(any(Channel.class))).willReturn(channel);
      given(userRepository.findById(userId1)).willReturn(Optional.of(user1));
      given(userRepository.findById(userId2)).willReturn(Optional.of(user2));
      given(readStatusRepository.save(any(ReadStatus.class))).willReturn(null);

      given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

      // when
      ChannelDto result = channelService.createPrivateChannel(request);

      // then
      assertThat(result).isNotNull();
      assertThat(result.type()).isEqualTo(ChannelType.PRIVATE);
      then(readStatusRepository).should(times(2)).save(any(ReadStatus.class));
    }
  }

  // update
  @Nested
  @DisplayName("update()")
  class UpdateChannel {

    @Test
    @DisplayName("성공: Public 채널의 이름과 설명 수정")
    void update_success() {
      // given
      UUID channelId = UUID.randomUUID();
      Channel channel = new Channel(ChannelType.PUBLIC, "구 이름", "구 설명");
      ChannelUpdateRequest request = new ChannelUpdateRequest("새 이름", "새 설명");
      ChannelDto expectedDto = new ChannelDto(
          channelId, ChannelType.PUBLIC, "새 이름", "새 설명", null, null);

      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

      // when
      ChannelDto result = channelService.update(channelId, request);

      // then
      assertThat(result.name()).isEqualTo("새 이름");
      assertThat(result.description()).isEqualTo("새 설명");
    }

    @Test
    @DisplayName("실패: Private 채널 수정 시도")
    void update_fail_privateChannel() {
      // given
      UUID channelId = UUID.randomUUID();
      Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
      ChannelUpdateRequest request = new ChannelUpdateRequest("새 이름", "새 설명");

      given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

      // when & then
      assertThatThrownBy(() -> channelService.update(channelId, request))
          .isInstanceOf(DiscodeitException.class)
          .satisfies(e -> {
            DiscodeitException ex = (DiscodeitException) e;
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PRIVATE_CHANNEL_UPDATE_DENIED);
          });
    }
  }

  @Nested
  @DisplayName("findAllByUserId()")
  class FindAllByUserId {

    @Test
    @DisplayName("결과 있음: 참여 채널이 있으면 PUBLIC + 내가 속한 PRIVATE 채널 목록 반환")
    void findAllByUserId_hasResult() {
      // given
      UUID userId = UUID.randomUUID();
      Channel publicChannel = new Channel(ChannelType.PUBLIC, "공개 채널", "설명");
      Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);

      ReadStatus rs = Mockito.mock(ReadStatus.class);
      given(rs.getChannel()).willReturn(privateChannel);
      given(readStatusRepository.findAllByUserIdWithChannel(userId)).willReturn(List.of(rs));
      given(channelRepository.findAllPublicOrIn(anyList())).willReturn(
          List.of(publicChannel, privateChannel));

      given(readStatusRepository.findAllByChannelIds(anyList())).willReturn(List.of());
      given(messageRepository.findLastMessagesByChannelIds(anyList())).willReturn(List.of());

      ChannelDto publicDto = new ChannelDto(
          UUID.randomUUID(), ChannelType.PUBLIC, "공개 채널", "설명", null, null);
      ChannelDto privateDto = new ChannelDto(UUID.randomUUID(), ChannelType.PRIVATE, null, null,
          List.of(), null);

      given(channelMapper.toDto(any(Channel.class), any(), any()))
          .willReturn(publicDto)
          .willReturn(privateDto);

      // when
      List<ChannelDto> result = channelService.findAllByUserId(userId);

      // then
      assertThat(result).hasSize(2);
      assertThat(result).extracting(ChannelDto::type)
          .containsExactlyInAnyOrder(ChannelType.PUBLIC, ChannelType.PRIVATE);
    }

    @Test
    @DisplayName("결과 없음")
    void findAllByUserId_noResult() {
      // given
      UUID userId = UUID.randomUUID();

      given(readStatusRepository.findAllByUserIdWithChannel(userId))
          .willReturn(Collections.emptyList());

      // when
      List<ChannelDto> result = channelService.findAllByUserId(userId);

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("delete()")
  class DeleteChannel {

    @Test
    @DisplayName("성공: 존재하는 channelId로 유저 삭제")
    void delete_success() {
      // given
      UUID channelId = UUID.randomUUID();
      Channel channel = new Channel(ChannelType.PUBLIC, "공개 채널", "설명");
      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      willDoNothing().given(channelRepository).deleteById(channelId);

      // when
      channelService.delete(channelId);

      // then
      then(channelRepository).should().deleteById(channelId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 Id면 예외 발생")
    void delete_fail_channelNotFound() {
      // given
      UUID unknownId = UUID.randomUUID();

      given(channelRepository.findById(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.delete(unknownId))
          .isInstanceOf(DiscodeitException.class)
          .satisfies(e -> {
            DiscodeitException ex = (DiscodeitException) e;
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CHANNEL_NOT_FOUND);
          });

      then(channelRepository).shouldHaveNoMoreInteractions();
    }
  }

}
