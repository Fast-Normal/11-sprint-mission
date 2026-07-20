package com.sprint.mission.discodeit.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@EnableAsync
@Configuration
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public TaskExecutor eventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(30);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("event-");

    executor.setTaskDecorator(task -> {
      // 현재 스레드의 MDC와 SecurityContext를 캡처
      Map<String, String> mdcContext = MDC.getCopyOfContextMap();
      SecurityContext securityContext = SecurityContextHolder.getContext();

      return () -> {
        try {
          if (mdcContext != null) {
            MDC.setContextMap(mdcContext);
          }
          SecurityContextHolder.setContext(securityContext);
          task.run();
        } finally {
          MDC.clear();
          SecurityContextHolder.clearContext();
        }
      };
    });
    executor.initialize();
    return executor;
  }
}
