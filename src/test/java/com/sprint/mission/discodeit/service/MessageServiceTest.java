package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doReturn;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @Mock
  private MessageMapper messageMapper;

  @Mock
  private PageResponseMapper pageResponseMapper;

  @InjectMocks
  private BasicMessageService messageService;

  // 테스트 용 객체 생성 메서드
  private User makeUser() {
    return new User("woody", "woody@test.com", "pass1234", null);
  }

  private Channel makeChannel() {
    return new Channel(ChannelType.PUBLIC, "공지채널", "설명");
  }

  //create
  @Nested
  @DisplayName("create()")
  class CreateMessage {

    @Test
    @DisplayName("성공: 유효한 authorId, channelId로 메시지 생성")
    void create_success() {
      // given
      UUID authorId = UUID.randomUUID();
      UUID channelId = UUID.randomUUID();
      MessageCreateRequest request = new MessageCreateRequest(authorId, channelId, "안녕하세요", null);

      User author = makeUser();
      Channel channel = makeChannel();
      Message message = new Message(author, channel, "안녕하세요");
      MessageDto expectedDto = new MessageDto(
          UUID.randomUUID(), Instant.now(), Instant.now(), "안녕하세요", channelId, null, List.of());

      given(userRepository.findById(authorId)).willReturn(Optional.of(author));
      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      given(messageRepository.save(any(Message.class))).willReturn(message);
      given(messageMapper.toDto(message)).willReturn(expectedDto);

      // when
      MessageDto result = messageService.create(request);

      // then
      assertThat(result).isNotNull();
      assertThat(result.content()).isEqualTo("안녕하세요");
      then(messageRepository).should().save(any(Message.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 channelId")
    void create_fail_channelNotFound() {
      // given
      UUID authorId = UUID.randomUUID();
      UUID unknownChannelId = UUID.randomUUID();
      MessageCreateRequest request = new MessageCreateRequest(
          authorId, unknownChannelId, "안녕하세요", null);

      given(userRepository.findById(authorId)).willReturn(Optional.of(makeUser()));
      given(channelRepository.findById(unknownChannelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.create(request))
          .isInstanceOf(DiscodeitException.class)
          .satisfies(e -> {
            DiscodeitException ex = (DiscodeitException) e;
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.CHANNEL_NOT_FOUND);
          });

      then(messageRepository).shouldHaveNoInteractions();
    }
  }

  @Nested
  @DisplayName("update()")
  class UpdateMessage {

    @Test
    @DisplayName("성공: 유효한 messageId와 새 내용으로 수정")
    void update_success() {
      // given
      UUID messageId = UUID.randomUUID();
      Message message = new Message(makeUser(), makeChannel(), "기존 내용");
      MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용", null);
      MessageDto expectedDto = new MessageDto(
          messageId, Instant.now(), Instant.now(), "수정된 내용", UUID.randomUUID(), null, List.of());

      given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
      given(messageMapper.toDto(message)).willReturn(expectedDto);

      // when
      MessageDto result = messageService.update(messageId, request);

      // then
      assertThat(result.content()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 messageId")
    void update_fail_messageNotFound() {
      // given
      UUID unknownId = UUID.randomUUID();
      MessageUpdateRequest request = new MessageUpdateRequest("수정 내용", null);

      given(messageRepository.findById(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.update(unknownId, request))
          .isInstanceOf(DiscodeitException.class)
          .satisfies(e -> {
            DiscodeitException ex = (DiscodeitException) e;
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
          });
    }
  }

  @Nested
  @DisplayName("findAllByChannelId()")
  class FindAllByChannelId {

    @Test
    @DisplayName("결과 있음: 채널에 메시지가 있으면 페이지네이션된 목록 반환")
    void findAllByChannelId_hasResult() {
// given
      UUID channelId = UUID.randomUUID();
      Pageable pageable = PageRequest.of(0, 50);

      Message message = new Message(makeUser(), makeChannel(), "채널 메시지");
      SliceImpl<Message> slice = new SliceImpl<>(List.of(message), pageable, false);
      MessageDto dto = new MessageDto(
          UUID.randomUUID(), Instant.now(), Instant.now(),
          "채널 메시지", channelId, null, List.of());
      PageResponse<MessageDto> expectedResponse = new PageResponse<>(
          List.of(dto), null, 1, false, null);

      given(messageRepository.findAllByChannel_IdWithDetails(channelId, pageable))
          .willReturn(slice);
      given(messageMapper.toDto(message)).willReturn(dto);
      doReturn(expectedResponse).when(pageResponseMapper).fromSlice(any());

      // when
      PageResponse<MessageDto> result = messageService.findAllByChannelId(
          channelId, null, pageable);

      // then
      assertThat(result.content()).hasSize(1);
      assertThat(result.content().get(0).content()).isEqualTo("채널 메시지");
    }

    @Test
    @DisplayName("결과 없음: 메시지가 없는 채널 조회 시 빈 목록을 반환한다")
    void findAllByChannelId_noResult() {
      // given
      UUID channelId = UUID.randomUUID();
      Pageable pageable = PageRequest.of(0, 50);

      SliceImpl<Message> emptySlice = new SliceImpl<>(
          Collections.emptyList(), pageable, false);
      PageResponse<MessageDto> emptyResponse = new PageResponse<>(
          Collections.emptyList(), null, 0, false, null);

      given(messageRepository.findAllByChannel_IdWithDetails(channelId, pageable))
          .willReturn(emptySlice);
      doReturn(emptyResponse).when(pageResponseMapper).fromSlice(any());

      // when
      PageResponse<MessageDto> result = messageService.findAllByChannelId(
          channelId, null, pageable);

      // then
      assertThat(result.content()).isEmpty();
      assertThat(result.hasNext()).isFalse();
    }
  }

  @Nested
  @DisplayName("delete()")
  class DeleteMessage {

    @Test
    @DisplayName("성공: 첨부파일 없는 메시지 삭제")
    void delete_success_noAttachments() {
      // given
      UUID messageId = UUID.randomUUID();
      Message message = new Message(makeUser(), makeChannel(), "안녕하새요");
      given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
      willDoNothing().given(messageRepository).deleteById(messageId);

      // when
      messageService.delete(messageId);

      // then
      then(binaryContentRepository).should().deleteAll(Collections.emptyList());
      then(messageRepository).should().deleteById(messageId);
    }

    @Test
    @DisplayName("성공: 첨부파일 있는 메시지 삭제 시 storage, binaryContentRepository도 함께 삭제")
    void delete_success_withAttachments() {
      // given
      UUID messageId = UUID.randomUUID();
      Message message = new Message(makeUser(), makeChannel(), "첨부파일 있는 메시지");
      BinaryContent attachment = new BinaryContent("image/png", new byte[]{1, 2, 3});
      message.attachFile(attachment);

      given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
      willDoNothing().given(binaryContentStorage).delete(any());
      willDoNothing().given(messageRepository).deleteById(messageId);

      // when
      messageService.delete(messageId);

      // then
      then(binaryContentStorage).should().delete(attachment.getId());
      then(binaryContentRepository).should().deleteAll(message.getAttachments());
      then(messageRepository).should().deleteById(messageId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 Id면 예외 발생")
    void delete_fail_messageNotFound() {
      // given
      UUID unknownId = UUID.randomUUID();

      given(messageRepository.findById(unknownId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> messageService.delete(unknownId))
          .isInstanceOf(DiscodeitException.class)
          .satisfies(e -> {
            DiscodeitException ex = (DiscodeitException) e;
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MESSAGE_NOT_FOUND);
          });

      then(messageRepository).should().findById(unknownId);
      then(messageRepository).shouldHaveNoMoreInteractions();
    }
  }
}
