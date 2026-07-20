package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private long size;

  @Column(nullable = false)
  private byte[] bytes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BinaryContentStatus status = BinaryContentStatus.PROCESSING;

  public BinaryContent(String contentType, byte[] bytes) {
    this.fileName = UUID.randomUUID().toString();
    this.contentType = contentType;
    this.bytes = bytes;
    this.size = bytes.length;
  }

  public void updateStatus(BinaryContentStatus status) {
    this.status = status;
  }

  public String toString() {
    return "fileName: " + fileName
        + ", contentType: " + contentType
        + ", size: " + size;
  }
}

