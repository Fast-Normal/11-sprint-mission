package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

  @Id
  private UUID userId;

  @Column(nullable = false, length = 1000)
  private String token;

  @Column(nullable = false)
  private Instant expiresAt;

  public RefreshToken(UUID userId, String token, Instant expiresAt) {
    this.userId = userId;
    this.token = token;
    this.expiresAt = expiresAt;
  }

  public void rotate(String newToken, Instant newExpiresAt) {
    this.token = newToken;
    this.expiresAt = newExpiresAt;
  }

  public boolean matches(String token) {
    return this.token.equals(token);
  }

}
