package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.jwt.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Value("${jwt.max-active-sessions:1}")
  private int maxActiveJwtCount;

  @Bean
  public JwtRegistry jwtRegistry() {
    return new InMemoryJwtRegistry(maxActiveJwtCount);
  }

}
