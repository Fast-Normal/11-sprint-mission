package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileMessageRepository implements MessageRepository {

    private final List<Message> data;

    private final File file = new File("messages.dat");

    public FileMessageRepository() {
        this.data = loadFromFile();
    }

    //create
    @Override
    public Message save(Message message) {
        //메시지는 중복되어도 됨
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
