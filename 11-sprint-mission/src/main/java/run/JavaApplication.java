package run;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

public class JavaApplication {
    public static void main(String[] args) {
        UserService userService = new JCFUserService();
        ChannelService channelService = new JCFChannelService();
        MessageService messageService = new JCFMessageService();
        //유저 도메인 테스트
        //유저 등록
        User user1 = userService.create("김바다", "bada@ocean.com");
        User user2 = userService.create("신성민", "seosung@min.com");

        //유저 다건 조회
        System.out.println("=== 등록된 모든 유저 목록 ===");
        for (User user : userService.findAll()) {
            System.out.println(user.getUserName());
        }

        //유저 정보 수정
        System.out.println("=== 유저 정보 수정 ===");
        userService.update(user1.getId(), "정민", "totoro@smile.com");

        //특정 유저 조회
        System.out.println("=== 유저 조회 ===");
        System.out.println(userService.findById(user1.getId()));

        //유저 삭제
        System.out.println("=== 유저 삭제 ===");
        userService.delete(user1.getId());

        System.out.println("=== 삭제 후 모든 유저 목록 ===");
        for (User user : userService.findAll()) {
            System.out.println(user.getUserName());
        }


        //채널 도메인 테스트
        //채널 등록
        Channel channel1 = channelService.create("1:1 대화방");
        Channel channel2 = channelService.create("단체 대화방");

        //채널 다건 조회
        System.out.println("=== 전체 채널 목록 ===");
        for (Channel c : channelService.findAll()) {
            System.out.println(c.getChannelName());
        }

        // 채널 이름 수정
        System.out.println("=== 채널 이름 수정 ===");
        channelService.update(channel2.getId(), "2026 단체방");
        // 바뀐 채널 이름 조회
        System.out.println("=== 채널 조희 ===");
        System.out.println(channelService.findById(channel2.getId()));
        // 채널 삭제
        System.out.println("=== 채널 삭제 ===");
        channelService.delete(channel1.getId());
        //채널 전체 조회
        System.out.println("=== 전체 채널 조회 ===");
        for (Channel c : channelService.findAll()) {
            System.out.println(c.getChannelName());
        }
        //메시지 도메인 테스트
        Message msg1 = messageService.create(user2, "안녕하세요", channel2);
        Message msg2 = messageService.create(user2, "신성민입니다", channel2);

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

    }
}
