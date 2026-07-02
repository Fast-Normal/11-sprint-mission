package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "channels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends BaseUpdatableEntity {

  @Column
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChannelType type;

  @Column
  private String description;

  @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<Message> messages = new ArrayList<>();

  @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<ReadStatus> readStatuses = new ArrayList<>();

  public Channel(ChannelType type, String name, String description) {
    this.name = name;
    this.type = type;
    this.description = description;
  }

  public void updateChannelName(String name) {
    this.name = name;
  }

  public void updateChannelDescription(String description) {
    this.description = description;
  }

  public String toString() {
    return "channelName: " + name + ", type: " + type + ", description: " + description;
  }


}
