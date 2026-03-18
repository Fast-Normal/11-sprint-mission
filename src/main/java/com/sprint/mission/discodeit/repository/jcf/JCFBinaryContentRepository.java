package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFBinaryContentRepository implements BinaryContentRepository{

    private final Map<UUID, BinaryContent> data;

    public JCFBinaryContentRepository() {
        this.data = new HashMap<>();
    }

    //save
    @Override
    public BinaryContent save(BinaryContent binaryContent){
        data.put(binaryContent.getId(), binaryContent);
        return binaryContent;
    }

    //Read
    @Override
    public Optional<BinaryContent> findById(UUID id){
        return Optional.ofNullable(data.get(id));
    }


    //Read all
    @Override
    public List<BinaryContent> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return findAll().stream()
                .filter(bc -> ids.contains(bc.getId()))
                .toList();
    }

    //Delete
    @Override
    public void delete(UUID id){
        data.remove(id);
    }

    @Override
    public void deleteAllByIdIn(List<UUID> ids) {
        ids.forEach(this::delete);
    }
}
