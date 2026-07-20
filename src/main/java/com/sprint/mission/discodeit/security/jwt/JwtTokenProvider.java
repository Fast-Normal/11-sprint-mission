package com.sprint.mission.discodeit.security.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.exception.auth.JwtExpiredException;
import com.sprint.mission.discodeit.exception.auth.JwtSignatureException;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetails;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
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
  public boolean isValidToken(String token) {
    try {
      validateTokenOrThrow(token);
      return true;
    } catch (JwtExpiredException | JwtSignatureException e) {
      return false;
    }
  }

  public void validateTokenOrThrow(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));

      if (!signedJWT.verify(verifier)) {
        throw new JwtSignatureException(Map.of("reason", "서명 검증 실패"));
      }

      Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
      if (expiration == null || expiration.before(new Date())) {
        throw new JwtExpiredException(Map.of("reason", "토큰 만료", "expiredAt", expiration));
      }
    } catch (ParseException | JOSEException e) {
      throw new JwtSignatureException(Map.of("reason", "JWT 파싱/서명 오류"), e);
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

}
