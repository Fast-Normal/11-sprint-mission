package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;

public class JavaApplication {
    static User setupUser(UserService userService) {
        User user = userService.create("박준형", "bakjunhyung@woobin.com");
        return user;
    }

    static Channel setupChannel(ChannelService channelService) {
        Channel channel = channelService.create(ChannelType.PUBLIC, "공지방", "공지 채널입니다.");
        return channel;
    }

    static void messageCreateTest(MessageService messageService, Channel channel, User author) {
        Message message = messageService.create("안녕하세요", channel.getId(), author.getId());
        System.out.println("메시지 생성: " + message.getId());
    }
    public static void main(String[] args) {
        /* JCF 코드
        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService = new JCFMessageService(userService, channelService);
         */

        /* FileIO 코드
        UserService userService = new FileUserService();
        ChannelService channelService = new FileChannelService();
        MessageService messageService = new FileMessageService(userService, channelService);
        */

        /*jcf repository 초기화
        UserRepository userRepository = new JCFUserRepository();
        ChannelRepository channelRepository = new JCFChannelRepository();
        MessageRepository messageRepository = new JCFMessageRepository();
*/
        //FileIO repository 초기화
        UserRepository userRepository = new FileUserRepository();
        ChannelRepository channelRepository = new FileChannelRepository();
        MessageRepository messageRepository = new FileMessageRepository();

        //BasicService 구현체 초기화
        UserService userService = new BasicUserService(userRepository);
        ChannelService channelService = new BasicChannelService(channelRepository);
        MessageService messageService = new BasicMessageService(messageRepository, userRepository, channelRepository);

        // 셋업
        User user = setupUser(userService);
        Channel channel = setupChannel(channelService);


        // 테스트
        Message msg1 = messageService.create("테스트", channel.getId(), user.getId());
        messageCreateTest(messageService, channel, user);

        //유저 다건 조회
        System.out.println("=== 등록된 모든 유저 목록 ===");
        for (User u : userService.findAll()) {
            System.out.println(u.getUserName());
        }

        //유저 정보 수정
        System.out.println("=== 유저 정보 수정 ===");
        userService.update(user.getId(), "정민", "totoro@smile.com");

        //특정 유저 조회
        System.out.println("=== 유저 조회 ===");
        System.out.println(userService.findById(user.getId()));



        //채널 다건 조회
        System.out.println("=== 전체 채널 목록 ===");
        for (Channel c : channelService.findAll()) {
            System.out.println(c.getChannelName());
        }

        // 채널 이름 수정
        System.out.println("=== 채널 이름 수정 ===");
        channelService.update(channel.getId(), "2026 단체방");
        // 바뀐 채널 이름 조회
        System.out.println("=== 채널 조희 ===");
        System.out.println(channelService.findById(channel.getId()));


        //메시지 다건 조회
        System.out.println("=== 전체 보낸 메시지 목록 ===");
        for (Message m : messageService.findAll()) {
            System.out.println(m.getContent());
        }

        // 메시지 수정
        System.out.println("=== 메시지 수정 ===");
        messageService.update(msg1.getId(), "잘못보냈습니다");

        // 바뀐 메시지 확인
        System.out.println("=== 수정된 메시지 조희 ===");
        System.out.println(messageService.findById(msg1.getId()));



        // 메시지 삭제
        System.out.println("=== 메시지 삭제 ===");
        messageService.delete(msg1.getId());
        // 보낸 메시지 전체 조회
        System.out.println("=== 보낸 메시지 조회 ===");
        for (Message m : messageService.findAll()) {
            System.out.println(m.getContent());
        }

        // 채널 삭제
        System.out.println("=== 채널 삭제 ===");
        channelService.delete(channel.getId());
        //채널 전체 조회
        System.out.println("=== 전체 채널 조회 ===");
        for (Channel c : channelService.findAll()) {
            System.out.println(c.getChannelName());
        }

        //유저 삭제
        System.out.println("=== 유저 삭제 ===");
        userService.delete(user.getId());

        System.out.println("=== 삭제 후 모든 유저 목록 ===");
        for (User u : userService.findAll()) {
            System.out.println(u.getUserName());
        }

    }
}
