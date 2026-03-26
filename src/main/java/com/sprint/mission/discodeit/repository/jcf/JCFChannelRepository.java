package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFChannelRepository implements ChannelRepository {
    private final Map<UUID, Channel> data;

    public JCFChannelRepository() {
        this.data = new HashMap<>();
    }

    //save
    @Override
    public Channel save(Channel channel) {
        data.put(channel.getId(), channel);
        return channel;
    }

    //read
    @Override
    public Optional<Channel> findById(UUID id){
        return Optional.ofNullable(data.get(id));
    }

    //readAll
    @Override
    public List<Channel> findAll(){
        return new ArrayList<>(data.values());
    }


    //delete
    @Override
    public void delete(UUID id){
        data.remove(id);
    }

    // existsById
    @Override
    public boolean existsById(UUID id) {
        return data.containsKey(id);
    }
}
