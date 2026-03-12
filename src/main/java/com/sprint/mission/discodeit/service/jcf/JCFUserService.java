package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.List;
import java.util.UUID;

public class JCFUserService implements UserService {
    private final JCFUserRepository userRepository;

    public JCFUserService(JCFUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //create
    @Override
    public User create(String userName, String userEmail){
        if (userRepository.existByEmail(userEmail)) {
            throw new IllegalArgumentException("중복된 이메일입니다");
        }
        User user = new User(userName, userEmail);
        return userRepository.save(user);
    }

    //Read
    @Override
    public User findById(UUID userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    }

    //Read all
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    //Update
    @Override
    public User update(UUID userId, String newUserName, String newUserEmail){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));;

        user.updateUserName(newUserName);
        user.updateUserEmail(newUserEmail);
        return userRepository.save(user);
    }

    //Delete
    @Override
    public void delete(UUID userId){
        userRepository.delete(userId);
    }
}
