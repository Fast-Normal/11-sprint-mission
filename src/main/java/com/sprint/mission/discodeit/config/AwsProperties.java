package com.sprint.mission.discodeit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

  private Credentials credentials = new Credentials();
  private String region;
  private S3 s3 = new S3();


  @Getter
  @Setter
  public static class Credentials {

    private String accessKey;  // env에서 주입
    private String secretKey;  // env에서 주입
  }

  @Getter
  @Setter
  public static class S3 {

    private String bucket;
  }
}