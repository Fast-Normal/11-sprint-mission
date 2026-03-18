//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.ChannelType;
//import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
//import com.sprint.mission.discodeit.service.ChannelService;
//
//import java.util.List;
//import java.util.UUID;
//
//public class JCFChannelService implements ChannelService {
//    private final JCFChannelRepository channelRepository;
//
//    public JCFChannelService(JCFChannelRepository channelRepository) {
//        this.channelRepository = channelRepository;
//    }
//
//    //create
//    @Override
//    public Channel create(ChannelType type, String channelName, String description) {
//        Channel channel = new Channel(type, channelName, description);
//        return channelRepository.save(channel);
//    }
//
//    //read
//    @Override
//    public Channel findById(UUID channelId){
//        return channelRepository.findById(channelId)
//                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
//    }
//    //readAll
//    @Override
//    public List<Channel> findAll(){
//        return channelRepository.findAll();
//    }
//
//    //update
//    @Override
//    public Channel update(UUID channelId, String newChannelName){
//        Channel channel = channelRepository.findById(channelId)
//                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
//
//        channel.updateChannelName(newChannelName);
//        return channelRepository.save(channel);
//    }
//
//    //delete
//    @Override
//    public void delete(UUID channelId){
//        channelRepository.delete(channelId);
//    }
//}
