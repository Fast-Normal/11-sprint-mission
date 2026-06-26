package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.csrf.SpaCsrfTokenRequestHandler;
import com.sprint.mission.discodeit.security.exception.DiscodeitAccessDeniedHandler;
import com.sprint.mission.discodeit.security.exception.DiscodeitAuthenticationEntryPoint;
import com.sprint.mission.discodeit.security.login.LoginFailureHandler;
import com.sprint.mission.discodeit.security.login.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final LoginSuccessHandler loginSuccessHandler;
  private final LoginFailureHandler loginFailureHandler;
  private final DiscodeitAuthenticationEntryPoint authenticationEntryPoint;
  private final DiscodeitAccessDeniedHandler accessDeniedHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, SessionRegistry sessionRegistry)
      throws Exception {
    http.csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
    );
    http.formLogin(login -> login
        .loginProcessingUrl("/api/auth/login")
        .successHandler(loginSuccessHandler)
        .failureHandler(loginFailureHandler));

    http.logout(logout -> logout
        .logoutUrl("/api/auth/logout")
        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)));

    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**", "/favicon.ico")
        .permitAll()
        .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
        .requestMatchers("/docs/**", "/api-docs/**", "/swagger-ui/**", "/actuator/**").permitAll()
        .anyRequest().authenticated());

    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(authenticationEntryPoint)
        .accessDeniedHandler(accessDeniedHandler));

    http.sessionManagement(management -> management
        .sessionConcurrency(concurrency -> concurrency
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true)
            .sessionRegistry(sessionRegistry)));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy(
        "ROLE_ADMIN > ROLE_CHANNEL_MANAGER\n" +
            "ROLE_CHANNEL_MANAGER > ROLE_USER"
    );
  }

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }
}
