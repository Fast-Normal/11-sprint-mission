package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusDto;
import com.sprint.mission.discodeit.dto.readStatus.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userStatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.service.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {
	static UserDto setupUser(UserService userService, String userName, String userEmail, String pw) {
		UserCreateRequest request = new UserCreateRequest(userName, userEmail, pw, null);
		return userService.create(request);
	}
	// 프로필 이미지 포함 유저 셋업
	static UserDto setupUserWithProfile(UserService userService, String userName, String userEmail, String pw) throws IOException {
		Path imagePath = Paths.get("src/main/resources/static/testImg.png");
		byte[] imageBytes = Files.readAllBytes(imagePath);

		BinaryContentCreateRequest profileImage = new BinaryContentCreateRequest(
				"testImg.png",
				"image/png",
							imageBytes
		);

		UserCreateRequest request = new UserCreateRequest(
				userName,
				userEmail,
				pw,
				profileImage
		);
		return userService.create(request);
	}

	// 채널 프라이빗 퍼블릭 생성 코드 분리
	static ChannelDto setupPublicChannel(ChannelService channelService) {
		PublicChannelCreateRequest request = new PublicChannelCreateRequest("공지방", "공개  채팅방입니다.");
		return channelService.createPublicChannel(request);
	}

	static ChannelDto setupPrivateChannel(ChannelService channelService, List<UUID> participantIds) {
		PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantIds);
		return channelService.createPrivateChannel(request);
	}

	static MessageDto messageCreateTest(MessageService messageService, UUID authorId, UUID channelId) {
		MessageDto message = messageService.create(new MessageCreateRequest(authorId, channelId, "메시지테스트1", null));
		System.out.println("메시지 생성: " + message.id());
		return message;
	}

	static MessageDto messageAttachmentCreateTest(MessageService messageService, UUID authorId, UUID channelId) throws IOException {
		Path filePath = Paths.get("src/main/resources/static/test2.jpeg");
		byte[] fileBytes = Files.readAllBytes(filePath);

		BinaryContentCreateRequest attachment1 = new BinaryContentCreateRequest(
				"test2.jepg",
				"image/jepg",
				fileBytes
		);

		BinaryContentCreateRequest attachment2 = new BinaryContentCreateRequest(
				"test3.jepg",
				"image/jepg",
				fileBytes
		);

		MessageCreateRequest request = new MessageCreateRequest(
				authorId,
				channelId,
				"첨부파일 테스트",
				List.of(attachment1, attachment2)
		);
		MessageDto message = messageService.create(request);
		System.out.println("메시지 생성: " + message.content());
		System.out.println("첨부파일 수: " + message.attachmentIds().size());
		System.out.println("첨부파일 ids: " + message.attachmentIds());
		return message;
	}

	public static void main(String[] args) throws IOException {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);

		UserService userService = context.getBean(UserService.class); // Spring이 조립해둔 Service Bean을 가져다 테스트
		ChannelService channelService = context.getBean(ChannelService.class);
		MessageService messageService = context.getBean(MessageService.class);
		BinaryContentService binaryContentService = context.getBean(BinaryContentService.class);
		ReadStatusService readStatusService = context.getBean(ReadStatusService.class);
		UserStatusService userStatusService = context.getBean(UserStatusService.class);

		// 생성 테스트
		// 유저 생성
		System.out.println("=== 유저 생성 ===");
		UserDto user1 = setupUser(userService, "박준형", "bakjunhyung@woobin.com", "password1");
		UserDto user2 = setupUser(userService, "김바다", "bada@sea.com", "password2");
		System.out.println("유저1 생성: " + user1.userName());
		System.out.println("유저2 생성: " + user2.userName());

		// 프로필 이미지 포함 유저 생성
		System.out.println("\n=== 프로필 이미지 포함 유저 생성 ===");
		UserDto userWithProfile = setupUserWithProfile(userService, "신성민", "seosung@min.com", "password3");
		System.out.println("프로필 유저 생성: " + userWithProfile.userName());
		System.out.println("프로필 이미지 id: " + userWithProfile.profileId());

		// 퍼블릭 채널 생성
		ChannelDto publicChannel = setupPublicChannel(channelService);
		System.out.println("\nPublic 채널 생성: " + publicChannel.channelName());
		System.out.println("참여자 목록(비어야함): " + publicChannel.participantsIds());

		// Private 채널 생성
		System.out.println("\n=== Private 채널 생성 ===");
		ChannelDto privateChannel = setupPrivateChannel(
				channelService,
				List.of(user1.id(), user2.id())
		);
		System.out.println("Private 채널 생성: " + privateChannel.id());
		System.out.println("참여자 목록: " + privateChannel.participantsIds());

		// 첨부파일 없는 메시지 생성
		System.out.println("\n=== 첨부파일 없는 메시지 생성 ===");
		MessageDto message = messageCreateTest(messageService, user2.id(), privateChannel.id());

		// 첨부파일 포함 메시지 생성
		System.out.println("\n=== 첨부파일 포함 메시지 생성 ===");
		MessageDto messageWithFiles = messageAttachmentCreateTest(messageService, user1.id(), publicChannel.id());

		// 조회 테스트
		// 유저 다건 조회
		System.out.println("=== 등록된 모든 유저 목록 ===");
		userService.findAll().forEach(u -> System.out.println(u.userName()));

		// 채널 다건 조회
		System.out.println("=== 전체 채널 목록 ===");
		channelService.findAll().forEach(c -> System.out.println(c.channelName()));

		// 첨부파일 조회
		System.out.println("\n=== 첨부파일 조회 ===");
		List<BinaryContentDto> attachments = binaryContentService
				.findAllByIdIn(messageWithFiles.attachmentIds());
		attachments.forEach(a -> System.out.println("파일명: " + a.originalFileName() + ", 크기: " + a.size()));

		// 채널별 메시지 조회
		System.out.println("\n=== 퍼블릭 채널별 메시지 조회 ===");
		messageService.findAllByChannelId(publicChannel.id())
				.forEach(m -> System.out.println("메시지: " + m.content() + ", 첨부파일 수: " + m.attachmentIds().size()));

		System.out.println("\n=== private 채널별 메시지 조회 ===");
		messageService.findAllByChannelId(privateChannel.id())
				.forEach(m -> System.out.println("메시지: " + m.content() + ", 첨부파일 수: " + m.attachmentIds().size()));

		// findAllByUserId - 유저가 볼 수 있는 채널 목록
		System.out.println("\n=== user1이 볼 수 있는 채널 목록 ===");
		channelService.findAllByUserId(user1.id())
				.forEach(c -> System.out.println("채널: " + c.channelName() + ", 타입: " + c.type()));

		System.out.println("\n=== user2가 볼 수 있는 채널 목록 ===");
		channelService.findAllByUserId(user2.id())
				.forEach(c -> System.out.println("채널: " + c.channelName() + ", 타입: " + c.type()));

		//User status 확인
		System.out.println("\n=== UserStatus 조회===");
		UserStatusDto userStatus = userStatusService.findByUserId(user1.id());
		System.out.println("UserStatus 조회: " + userStatus.id());
		System.out.println("userId: " + userStatus.userId());
		System.out.println("lastActiveAt: " + userStatus.lastActiveAt());

		System.out.println("\n=== " + user1.userName() + "의 온라인 상태 확인 ===");
		UserDto userDto = userService.findById(user1.id());
		System.out.println("온라인 여부: " + userDto.isOnline());

		//User status 업데이트
		System.out.println("\n=== UserStatus 업데이트 ===");
		UserStatusDto updatedUs = userStatusService.updateByUserId(
				userStatus.id(),
				new UserStatusUpdateRequest(Instant.now())
		);
		System.out.println("업데이트된 lastActiveAt: " + updatedUs.lastActiveAt());

		// readStatus 테스트
//		System.out.println("\n=== ReadStatus 생성 ===");
//		ReadStatusDto readStatus = readStatusService.create(
//				new ReadStatusCreateRequest(user2.id(), privateChannel.id())
//		);
//		System.out.println("ReadStatus 생성: " + readStatus.id());
//		System.out.println("userId: " + readStatus.userId());
//		System.out.println("channelId: " + readStatus.channelId());
//		System.out.println("lastReadAt: " + readStatus.lastReadAt());

//		System.out.println("\n=== ReadStatus 조회 ===");
//		ReadStatusDto found = readStatusService.findById(readStatus.id());
//		System.out.println("ReadStatus 조회: " + found.id());

		System.out.println("\n=== userId로 ReadStatus 전체 조회 ===");
		List<ReadStatusDto> user2ReadStatus = readStatusService.findAllByUserId(user2.id());
		user2ReadStatus.forEach(rs -> System.out.println("채널: " + rs.channelId() + ", 마지막 읽은 시간: " + rs.lastReadAt()));

		System.out.println("\n=== ReadStatus 업데이트 ===");
		ReadStatusDto readStatus = user2ReadStatus.get(0);
		ReadStatusDto updatedRs = readStatusService.update(
				readStatus.id(),
				new ReadStatusUpdateRequest(Instant.now())
		);
		System.out.println("업데이트된 lastReadAt: " + updatedRs.lastReadAt());

		System.out.println("\n=== ReadStatus 삭제 ===");
		readStatusService.delete(readStatus.id());
		System.out.println("ReadStatus 삭제 완료");

		// 수정 테스트
		// 메시지 수정 (첨부파일 교체)
		System.out.println("\n=== 메시지 수정 (첨부파일 교체) ===");
		byte[] dummyBytes = "dummy image data".getBytes();
		BinaryContentCreateRequest newAttachment = new BinaryContentCreateRequest(
				"test.png",
				"image/png",
				dummyBytes
		);

		MessageDto updatedMessage = messageService.update(
				messageWithFiles.id(),
				new MessageUpdateRequest("수정된 메시지", List.of(newAttachment))
		);

		System.out.println("수정된 메시지: " + updatedMessage.content());
		System.out.println("새 첨부파일 ids: " + updatedMessage.attachmentIds());

		// 유저 정보 수정
		System.out.println("\n=== 유저 정보 수정 ===");
		userService.update(user1.id(), new UserUpdateRequest("정민", "totoro@smile.com", null, "newPassword"));

		// 수정된 유저 단건 조회
		System.out.println("\n=== 수정된 유저 조회 ===");
		System.out.println(userService.findById(user1.id()));

		// 채널 이름 수정
		System.out.println("\n=== 채널 이름 수정 ===");
		channelService.update(publicChannel.id(), new ChannelUpdateRequest("새 채널 이름", "전체 채팅(수정)방"));

		// 바뀐 채널 이름 조회
		System.out.println("\n=== 채널 조희 ===");
		System.out.println(channelService.findById(publicChannel.id()));

		// 삭제 테스트
		// 메시지 삭제
		System.out.println("=== 메시지 삭제 (첨부파일 포함) ===");
		messageService.delete(messageWithFiles.id());
		System.out.println("메시지 삭제 완료");

		// private 채널 삭제 (메시지, readStatus 연쇄 삭제)
		System.out.println("\n=== 채널 삭제 ===");
		channelService.delete(privateChannel.id());
		System.out.println("Private 채널 삭제 완료");

		//유저 삭제 (프로필 이미지, UserStatus 연쇄 삭제)
		System.out.println("\n=== 유저 삭제 ===");
		userService.delete(user1.id());
		userService.delete(userWithProfile.id());
		System.out.println("유저 삭제 완료");

		System.out.println("=== 삭제 후 모든 유저 목록 ===");
		userService.findAll().forEach(u -> System.out.println(u.userName()));
	}



}
