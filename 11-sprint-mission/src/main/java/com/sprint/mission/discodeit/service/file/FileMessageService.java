package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageService implements MessageService {

    private final List<Message> data;
    private final UserService userService;
    private final ChannelService channelService;

    private final File file = new File("messages.dat");

    public FileMessageService(UserService userService, ChannelService channelService) {
        this.userService = userService;
        this.channelService = channelService;
        this.data = loadFromFile();
    }

    //create
    @Override
    public Message create(User sender, String content, Channel channel) {
        if (sender == null || userService.findById(sender.getId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 사용자");
        }

        if (channel == null || channelService.findById(channel.getId()) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널");
        }

        if (content == null  || content.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
        }

        Message message = new Message(sender, channel, content);

        data.add(message);
        saveToFile();

        return message;
    }

    //read
    @Override
    public Message findById(UUID messageId){
        for (Message m : data) {
            if (m.getId().equals(messageId)) {
                return m;
            }
        } return null;
    }
    //readAll
    @Override
    public List<Message> findAll(){
        return new ArrayList<>(data);
    }

    //update
    @Override
    public Message update(UUID messageId, String newContent){
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("메시지 내용이 비어있습니다.");
        }

        for (Message m : data) {
            if (m.getId().equals(messageId)) {
                m.updateContent(newContent);
                saveToFile();
                return m;
            }
        } return null;
    }

    //delete
    @Override
    public void delete(UUID messageId) {
        data.removeIf(m -> m.getId().equals(messageId));
        saveToFile();
    }

    // 파일 저장
    private void saveToFile() {
        try (ObjectOutputStream oos
                     = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 파일 로드
    private List<Message> loadFromFile() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois
                     = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
