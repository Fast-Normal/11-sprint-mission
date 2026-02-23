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
    public Message create(String content, UUID authorId, UUID channelId) {
        User author = userService.findById(authorId);
        Channel channel = channelService.findById(channelId);
        if (author == null || channel == null) {
            throw new IllegalArgumentException("Invalid author/channel");
        }

        if (content == null  || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid content");
        }

        Message message = new Message(author,channel, content);

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
