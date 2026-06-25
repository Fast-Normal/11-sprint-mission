package com.sprint.mission.discodeit.security.role;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  @Value("${admin.username:Admin}")
  private String adminUsername;

  @Value("${admin.email:Admin@admin.com}")
  private String adminEmail;

  @Value("${admin.password}")
  private String adminPassword;

  @Transactional
  @Override
  public void run(ApplicationArguments args) {

    if (!userRepository.existsByUsername(adminUsername)) {
      log.debug("Admin 계정 생성");
      User admin = new User(adminUsername,
          adminEmail,
          passwordEncoder.encode(adminPassword),
          null);
      admin.updateRole(Role.ADMIN);
      admin.initUserStatus();
      userRepository.save(admin);
    } else {
      log.debug("이미 Admin 계정이 존재합니다.");
    }
  }
}
