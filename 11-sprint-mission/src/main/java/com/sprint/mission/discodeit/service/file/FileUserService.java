package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class FileUserService implements UserService {

    private final List<User> data;
    private final File file = new File("users.dat");

    public FileUserService() {
        this.data = loadFromFile();
    }

    // create
    @Override
    public User create(String userName, String userEmail) {
        User user = new User(userName, userEmail);
        data.add(user);
        saveToFile();
        return user;
    }

    // read
    @Override
    public User findById(UUID userId) {
        for (User user : data) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    // read all
    @Override
    public List<User> findAll() {
        return new ArrayList<>(data);
    }

    // update
    @Override
    public User update(UUID userId, String newUserName, String newUserEmail) {
        for (User user : data) {
            if (user.getId().equals(userId)) {
                user.updateUserName(newUserName);
                user.updateUserEmail(newUserEmail);
                saveToFile();
                return user;
            }
        }
        return null;
    }

    // delete
    @Override
    public void delete(UUID userId) {
        data.removeIf(u -> u.getId().equals(userId));
        saveToFile();
    }


    //파일 저장
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //파일 로드
    private List<User> loadFromFile() {
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
