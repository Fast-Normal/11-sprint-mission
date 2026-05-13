package com.sprint.mission.discodeit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component("externalApi") // /actuator/health/externalApi로 노출
@ConditionalOnEnabledHealthIndicator("externalApi") // 조건부 활성화
public class ExternalApiHealthIndicator implements HealthIndicator {

  private final RestTemplate restTemplate;
  private final String apiUrl;

  public ExternalApiHealthIndicator(
      RestTemplate restTemplate,
      @Value("${external.api.url:https://httpbin.org}") String apiUrl
  ) {
    this.restTemplate = restTemplate;
    this.apiUrl = apiUrl;
  }

  @Override
  public Health health() {
    try {
      // 외부 API에 /health 엔드포인트 요청
      ResponseEntity<String> response = restTemplate.getForEntity(apiUrl + "/health", String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        // 2xx 응답이면 up 상태로 간주
        return Health.up()
            .withDetail("statusCode", response.getStatusCode().value())
            .withDetail("message", "External API is available")
            .build();
      } else {
        // 비정상 응답 처리
        return Health.down()
            .withDetail("statusCode", response.getStatusCode().value())
            .withDetail("message", "External API returned non-2xx status")
            .build();
      }
    } catch (HttpClientErrorException e) {
      // 4xx = 서버는 살아있음 → UP
      return Health.up()
          .withDetail("statusCode", e.getStatusCode().value())
          .withDetail("message", "External API is reachable (got " + e.getStatusCode() + ")")
          .build();
    } catch (Exception e) {
      // 연결 자체 실패 -> Down
      return Health.down()
          .withDetail("message", "External API is not available")
          .withDetail("error", e.getMessage())
          .build();
    }
  }
}
