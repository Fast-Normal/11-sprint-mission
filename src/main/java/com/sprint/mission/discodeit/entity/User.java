package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.*;
import java.util.UUID;

@Getter
public class User extends AbstractEntity{

    private String userName;
    private String userEmail;
    private UUID profileId;

    public User(String userName, String userEmail) {
        super();
        this.userName = userName;
        this.userEmail = userEmail;
        this.profileId = null;

    }

    public void updateUserName(String userName) {
        this.userName = userName;
        timeUpdated();
    }

    public void updateUserEmail(String userEmail) {
        this.userEmail = userEmail;
        timeUpdated();
    }

    public void updateUserProfile(UUID profileId) {
        this.profileId = profileId;
        timeUpdated();
    }

    public String toString() {
        return "userName: " + userName + ", userEmail: " + userEmail;
    }
}

