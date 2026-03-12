package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent extends AbstractEntity{

    private final String fileName;
    private final String originalFileName;
    private final String contentType;
    private final long size;
    private final byte[] bytes;


    public BinaryContent(String originalFileName, String contentType, byte[] bytes) {
        super();
        this.fileName = UUID.randomUUID().toString();
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.size = bytes.length;
        this.bytes = bytes;
    }


    public String toString() {
        return "fileName: " + fileName
                + ", contentType: " + contentType
                + ", size: " + size
                + ", bytes: " + bytes;
    }
}

