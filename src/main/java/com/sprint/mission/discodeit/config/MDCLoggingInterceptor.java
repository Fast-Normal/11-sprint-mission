package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {

  // 컨트롤러 실행 전 호출(preHandle), false 반환 시 이후 처리 중단
  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response, Object handler) {
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    MDC.put("requestId", requestId);
    MDC.put("method", request.getMethod());
    MDC.put("url", request.getRequestURI());

    response.setHeader("Discodeit-Request-ID", requestId);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request,
      HttpServletResponse response, Object handler, Exception ex) {
    MDC.clear();   // 스레드 풀 재사용 시 이전 MDC 잔류 방지
  }

}
