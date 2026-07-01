package com.sprint.mission.discodeit.security.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.exception.auth.JwtSignatureException;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  @Value("${jwt.key}")
  private String secretKey;

  @Value("${jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  // 토큰 발급 메서드
  public String generateAccessToken(DiscodeitUserDetails userDetails) {
    UUID userId = userDetails.getUserDto().id();
    String role = userDetails.getUserDto().role().name();

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(userId.toString())
        .issueTime(Date.from(Instant.now()))
        .expirationTime(Date.from(Instant.now().plusSeconds(accessTokenExpiration)))
        .claim("role", role)
        .build();

    return buildToken(claims);
  }

  public String generateRefreshToken(DiscodeitUserDetails userDetails) {
    UUID userId = userDetails.getUserDto().id();

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(userId.toString())
        .issueTime(Date.from(Instant.now()))
        .expirationTime(Date.from(Instant.now().plusSeconds(refreshTokenExpiration)))
        .build();

    return buildToken(claims);
  }

  // 헬퍼 메서드: 토큰 발급
  private String buildToken(JWTClaimsSet claims) {
    try {
      byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
      JWSSigner signer = new MACSigner(keyBytes);

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
      signedJWT.sign(signer);

      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new JwtSignatureException(Map.of("reason", "JWT 서명 실패"), e);
    }
  }

  // 유효성 검사
  public boolean validateToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));

      if (!signedJWT.verify(verifier)) {
        log.warn("JWT 서명 검증 실패");
        return false;
      }

      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expiration == null || expiration.before(new Date())) {
        log.warn("JWT 만료됨: {}", expiration);
        return false;
      }

      return true;
    } catch (ParseException | JOSEException e) {
      log.warn("JWT 검증 중 오류: {}", e.getMessage());
      return false;
    }
  }

  public Instant getExpiration(String token) {
    try {
      return SignedJWT.parse(token)
          .getJWTClaimsSet()
          .getExpirationTime()
          .toInstant();
    } catch (ParseException e) {
      throw new IllegalArgumentException("토큰 파싱 실패", e);
    }
  }

  public String getSubject(String token) {
    try {
      return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new IllegalArgumentException("토큰 파싱 실패", e);
    }
  }

  // 갱신
  public String reissueAccessToken(String refreshToken) {
    if (!validateToken(refreshToken)) {
      throw new RefreshTokenInvalidException(Map.of());
    }

    String subject = getSubject(refreshToken);

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(subject)
        .issueTime(Date.from(Instant.now()))
        .expirationTime(Date.from(Instant.now().plusSeconds(accessTokenExpiration)))
        .build();

    return buildToken(claims);
  }

  // 헬퍼 메서드
  private Date getTokenExpiration(int expirationMinutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, expirationMinutes);
    return calendar.getTime();
  }

}
