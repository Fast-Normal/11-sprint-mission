package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.util.UUID;

@Getter
public class User extends AbstractEntity{
    @Serial
    private static final long serialVersionUID = 1L;
    private String userName;
    private String userEmail;
    private String password;
    private UUID profileId;

    public User(String userName, String userEmail, String password, UUID profileId) {
        super();
        this.userName = userName;
        this.userEmail = userEmail;
        this.password = password;
        this.profileId = profileId;

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

    public void updatePassword(String password) {
        this.password = password;
        timeUpdated();
    }

    public String toString() {
        return "userName: " + userName + ", userEmail: " + userEmail;
    }
}

