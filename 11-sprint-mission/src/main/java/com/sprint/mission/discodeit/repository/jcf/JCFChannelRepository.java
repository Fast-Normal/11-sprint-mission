package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelRepository implements ChannelRepository {
    private final List<Channel> data;

    public JCFChannelRepository() {
        this.data = new ArrayList<>();
    }

    //save
    @Override
    public Channel save(Channel channel) {
        data.removeIf(c -> c.getId().equals(channel.getId()));
        data.add(channel);
        return channel;
    }

    //read
    @Override
    public Channel findById(UUID id){
        for (Channel c : data) {
            if (c.getId().equals(id)) {
                return c;
            }
        } return null;
    }

    //readAll
    @Override
    public List<Channel> findAll(){
        return new ArrayList<>(data);
    }


    //delete
    @Override
    public void delete(UUID id){
        data.removeIf(c-> c.getId().equals(id));
    }
}
