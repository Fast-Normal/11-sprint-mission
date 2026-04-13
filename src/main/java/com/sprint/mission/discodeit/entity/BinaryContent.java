package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity {

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private long size;


  public BinaryContent(String contentType, long size) {
    super();
    this.fileName = UUID.randomUUID().toString();
    this.contentType = contentType;
    this.size = size;
  }


  public String toString() {
    return "fileName: " + fileName
        + ", contentType: " + contentType
        + ", size: " + size;
  }
}

