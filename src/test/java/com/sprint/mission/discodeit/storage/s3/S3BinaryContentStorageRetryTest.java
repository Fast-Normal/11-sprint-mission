package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.event.s3.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.storage.StorageSaveFailedException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.mockito.Mockito;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "discodeit.storage.type=s3")
@RecordApplicationEvents
class S3BinaryContentStorageRetryTest {

  @Autowired
  private S3BinaryContentStorage storage;

  @Autowired
  private ApplicationEvents applicationEvents;

  @MockitoBean
  private S3Client s3Client;

  // S3Presigner는 생성자 의존성이라 컨텍스트 로딩을 위해 목으로 채워둠
  @TestConfiguration
  static class PresignerTestConfig {

    @Bean
    S3Presigner s3Presigner() {
      return Mockito.mock(S3Presigner.class);
    }
  }

  @Test
  @DisplayName("일시적 실패 후 재시도로 성공하면 정상 결과를 반환한다")
  void put_retriesOnTransientFailure_thenSucceeds() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = "test".getBytes();

    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willThrow(S3Exception.builder().message("일시적 오류").statusCode(500).build())
        .willThrow(S3Exception.builder().message("일시적 오류").statusCode(500).build())
        .willReturn(PutObjectResponse.builder().build()); // 3번째 시도에 성공

    // when
    UUID result = storage.put(id, bytes);

    // then
    assertThat(result).isEqualTo(id);
    verify(s3Client, times(3)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  @DisplayName("모든 재시도가 실패하면 Recover가 호출되고 실패 이벤트가 발행된다")
  void put_allRetriesFail_triggersRecoverAndPublishesEvent() {
    // given
    UUID id = UUID.randomUUID();
    byte[] bytes = "test".getBytes();
    String errorMessage =
        "The AWS Access Key Id you provided does not exist in our records.";

    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willThrow(S3Exception.builder().message(errorMessage).statusCode(403).build());

    // when & then
    assertThatThrownBy(() -> storage.put(id, bytes))
        .isInstanceOf(StorageSaveFailedException.class);

    // maxAttempts=3 만큼 호출됐는지
    verify(s3Client, times(3)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

    // Recover에서 발행한 이벤트 검증
    assertThat(applicationEvents.stream(S3UploadFailedEvent.class))
        .anySatisfy(event -> {
          assertThat(event.binaryContentId()).isEqualTo(id);
          assertThat(event.errorMessage()).contains("AWS Access Key Id");
        });
  }
}
