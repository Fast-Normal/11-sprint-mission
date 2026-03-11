package com.sprint.mission.discodeit.entity;

import java.io.*;
import java.util.UUID;

public class User extends AbstractEntity{

    private String userName;
    private String userEmail;

    public User(String userName, String userEmail) {
        super();
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public void updateUserName(String userName) {
        this.userName = userName;
        timeUpdated();
    }

    public void updateUserEmail(String userEmail) {
        this.userEmail = userEmail;
        timeUpdated();
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String toString() {
        return "[유저 이름: " + userName + ", 유저 이메일: " + userEmail + "]";
    }
}

