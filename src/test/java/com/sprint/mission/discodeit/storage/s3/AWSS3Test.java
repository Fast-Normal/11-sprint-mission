package com.sprint.mission.discodeit.storage.s3;

import com.sprint.mission.discodeit.config.AwsProperties;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("CI/로컬 환경에서 AWS 자격증명 없으면 스킵")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AWSS3Test {

  @Autowired
  S3Client s3Client;

  @Autowired
  AwsProperties props;

  private static final UUID TEST_KEY = UUID.randomUUID();

  @Test
  @Order(1)
  @DisplayName("성공: S3 업로드")
  void upload() {
    String content = "test content";

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(props.getS3().getBucket())
            .key(TEST_KEY.toString())
            .contentType("text/plain")
            .build(),
        RequestBody.fromString(content)
    );
  }

  @Test
  @Order(2)
  @DisplayName("성공: S3 다운로드")
  void download() throws IOException {
    ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
        GetObjectRequest.builder()
            .bucket(props.getS3().getBucket())
            .key(TEST_KEY.toString())
            .build()
    );

    String content = new String(response.readAllBytes());
    System.out.println("다운로드 내용: " + content);
  }

  @Test
  @Order(3)
  @DisplayName("성공: PresignedUrl 생성")
  void presignedUrl() {
    try (S3Presigner presigner = S3Presigner.builder()
        .region(Region.of(props.getRegion()))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    props.getCredentials().getAccessKey(),
                    props.getCredentials().getSecretKey()
                )
            )
        ).build()) {

      PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(
          GetObjectPresignRequest.builder()
              .signatureDuration(Duration.ofMinutes(10))
              .getObjectRequest(
                  GetObjectRequest.builder()
                      .bucket(props.getS3().getBucket())
                      .key(TEST_KEY.toString())
                      .build()
              ).build()
      );

      System.out.println("PresignedUrl: " + presignedRequest.url());

    }
  }
}
