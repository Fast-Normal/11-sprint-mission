package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.redis.RedisJwtRegistry;
import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.security.jwt.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class JwtConfig {

  @Value("${jwt.max-active-sessions:1}")
  private int maxActiveJwtCount;

  @Bean
  @Profile("!redis")
  public JwtRegistry inMemoryJwtRegistry() {
    return new InMemoryJwtRegistry(maxActiveJwtCount);
  }

  @Bean
  @Profile("redis")
  public JwtRegistry redisJwtRegistry(
      JwtTokenProvider jwtTokenProvider,
      @Qualifier("jwtRedisTemplate") RedisTemplate<String, JwtInformation> redisTemplate,
      RedisLockProvider redisLockProvider) {
    return new RedisJwtRegistry(maxActiveJwtCount, jwtTokenProvider, redisTemplate,
        redisLockProvider);

  }

}
