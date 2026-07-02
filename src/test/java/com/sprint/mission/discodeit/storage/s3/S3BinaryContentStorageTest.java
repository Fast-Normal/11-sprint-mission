package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.discodeit.exception.storage.StorageFileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Disabled("CI/로컬 환경에서 AWS 자격증명 없으면 스킵")
@ActiveProfiles("test")
public class S3BinaryContentStorageTest {

  EasyRandom easyRandom = new EasyRandom();

  @Autowired
  S3BinaryContentStorage storage;

  @Test
  @DisplayName("성공: S3 업로드")
  void put() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = easyRandom.nextObject(byte[].class);

    // when
    UUID result = storage.put(id, bytes);

    // then
    assertThat(result).isEqualTo(id);
  }

  @Test
  @DisplayName("성공: S3 다운로드")
  void get() throws IOException {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = "test content".getBytes();
    storage.put(id, bytes);

    // when
    InputStream inputStream = storage.get(id);

    // then
    assertThat(inputStream.readAllBytes()).isEqualTo(bytes);
  }

  @Test
  @DisplayName("성공: PresignedUrl 생성 ")
  void download() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = easyRandom.nextObject(byte[].class);
    storage.put(id, bytes);

    // when
    Resource resource = storage.download(id);

    // then
    assertThat(resource).isNotNull();
    assertThat(resource.getDescription()).contains("http");
  }

  @Test
  @DisplayName("성공: S3 삭제")
  void delete() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = easyRandom.nextObject(byte[].class);
    storage.put(id, bytes);

    // when & then
    assertThatCode(() -> storage.delete(id))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("실패: 존재하지 않는 파일 조회")
  void get_notFound() {
    // given
    UUID id = UUID.randomUUID();

    // when & then
    assertThatThrownBy(() -> storage.get(id))
        .isInstanceOf(StorageFileNotFoundException.class);
  }

}
