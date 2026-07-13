package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.event.s3.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.storage.StorageDeleteFailedException;
import com.sprint.mission.discodeit.exception.storage.StorageFileNotFoundException;
import com.sprint.mission.discodeit.exception.storage.StorageSaveFailedException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Slf4j
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "s3")
public class S3BinaryContentStorage implements BinaryContentStorage {

  private final S3Client s3Client;
  private final S3Presigner presigner;
  private final String bucket;
  private final long presignedUrlExpiration;
  private final ApplicationEventPublisher applicationEventPublisher;

  public S3BinaryContentStorage(
      @Value("${discodeit.storage.s3.bucket}") String bucket,
      @Value("${discodeit.storage.s3.presigned-url-expiration:600}") long presignedUrlExpiration,
      S3Client s3Client,
      S3Presigner presigner, ApplicationEventPublisher applicationEventPublisher) {
    this.bucket = bucket;
    this.presignedUrlExpiration = presignedUrlExpiration;
    this.s3Client = s3Client;
    this.presigner = presigner;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Retryable(
      retryFor = SdkException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 500, multiplier = 2)
  )
  @Override
  public UUID put(UUID id, byte[] bytes) {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(id.toString())
            .build(),
        RequestBody.fromBytes(bytes)
    );
    log.info("S3 업로드 완료 - id: {}", id);
    return id;
  }

  @Recover
  public UUID recover(Exception e, UUID id, byte[] bytes) {
    String requestId = MDC.get("requestId") != null ? MDC.get("requestId").toString() : null;
    log.error("S3 업로드 재시도 모두 실패 - id: {}, requestId: {}, error: {}", id, requestId, e.getMessage());

    applicationEventPublisher.publishEvent(
        new S3UploadFailedEvent("BinaryContent S3 저장", requestId, id, e.getMessage())
    );
    throw new StorageSaveFailedException(id);
  }

  @Override
  public InputStream get(UUID id) {
    try {
      return s3Client.getObject(
          GetObjectRequest.builder()
              .bucket(bucket)
              .key(id.toString())
              .build()
      );
    } catch (Exception e) {
      throw new StorageFileNotFoundException(id);
    }
  }

  @Override
  public Resource download(UUID id) {
    PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
            .getObjectRequest(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(id.toString())
                    .build()
            ).build()
    );

    String url = presignedRequest.url().toString();
    log.info("PresignedUrl 생성 완료 - id: {}, url: {}", id, url);
    return new UrlResource(presignedRequest.url());
  }

  @Override
  public void delete(UUID id) {
    try {
      s3Client.deleteObject(
          DeleteObjectRequest.builder()
              .bucket(bucket)
              .key(id.toString())
              .build()
      );
      log.info("객체 삭제 완료 - id: {}", id);
    } catch (Exception e) {
      throw new StorageDeleteFailedException(id);
    }
  }


}
