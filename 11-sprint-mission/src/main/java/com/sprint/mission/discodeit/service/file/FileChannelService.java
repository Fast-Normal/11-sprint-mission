package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {
    private final List<Channel> data;
    private final File file = new File("channels.dat");

    public FileChannelService() {
        this.data = loadFromFile();
    }

    //create
    @Override
    public Channel create(String channelName) {
        Channel channel = new Channel(channelName);
        data.add(channel);
        saveToFile();
        return channel;
    }

    //read
    @Override
    public Channel findById(UUID channelId){
        for (Channel c : data) {
            if (c.getId().equals(channelId)) {
                return c;
            }
        } return null;
    }
    //readAll
    @Override
    public List<Channel> findAll(){
        return new ArrayList<>(data);
    }

    //update
    @Override
    public Channel update(UUID channelId, String newChannelName){
        for (Channel c : data) {
            if (c.getId().equals(channelId)) {
                c.updateChannelName(newChannelName);
                saveToFile();
                return c;
            }
        } return null;
    }

    //delete
    @Override
    public void delete(UUID channelId){
        data.removeIf(c -> c.getId().equals(channelId));
    }


    // 파일 저장
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 파일 로드
    private List<Channel> loadFromFile() {
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
