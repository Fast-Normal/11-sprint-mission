package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelService implements ChannelService {
    private final List<Channel> data;

    public JCFChannelService() {
        this.data = new ArrayList<>();
    }

    //create
    @Override
    public Channel create(ChannelType type, String channelName, String description) {
        Channel channel = new Channel(type, channelName, description);

        data.add(channel);

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
                return c;
            }
        } return null;
    }

    //delete
    @Override
    public void delete(UUID channelId){
        for (int i = 0 ; i < data.size() ; i++) {
            if (data.get(i).getId().equals(channelId)) {
                data.remove(i);
                return;
            }
        }
    }
}
