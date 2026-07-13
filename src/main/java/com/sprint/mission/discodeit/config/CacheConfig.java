package com.sprint.mission.discodeit.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  public static final String CHANNELS = "channels";
  public static final String NOTIFICATIONS = "notifications";
  public static final String USERS = "users";

}
