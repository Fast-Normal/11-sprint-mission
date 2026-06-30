package com.sprint.mission.discodeit.security.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTClaimsSet.Builder;
import com.nimbusds.jwt.SignedJWT;
import com.sprint.mission.discodeit.exception.auth.JwtExpiredException;
import com.sprint.mission.discodeit.exception.auth.JwtSignatureException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  @Value("${jwt.key}")
  private String secretKey;

  @Value("${jwt.access-token-expiration-minutes}")
  private int accessTokenExpirationMinutes;

  @Value("${jwt.refresh-token-expiration-minutes}")
  private int refreshTokenExpirationMinutes;

  private String base64EncodedSecretKey;

  @PostConstruct
  public void init() {
    this.base64EncodedSecretKey = encodeBase64SecretKey(secretKey);
  }

  public String encodeBase64SecretKey(String secretKey) {
    return Base64.getEncoder()
        .encodeToString(secretKey.getBytes(StandardCharsets.UTF_8));
  }

  // USER ID 기반 토큰 발급 메서드
  public String generateAccessToken(UUID userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId.toString());
    return generateToken(claims, userId.toString(),
        getTokenExpiration(accessTokenExpirationMinutes));
  }

  public String generateRefreshToken(UUID userId) {
    return generateToken(Map.of(), userId.toString(),
        getTokenExpiration(refreshTokenExpirationMinutes));
  }

  // 테스트 전용 메서드 (만료시점 임의 지정)
  public String generateAccessToken(UUID userId, int minutesOffset) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", userId.toString());
    return generateToken(claims, userId.toString(), getTokenExpiration(minutesOffset));
  }

  // 헬퍼 메서드: 토큰 발급
  private String generateToken(Map<String, Object> claims, String subject, Date expiration) {
    try {
      JWSSigner signer = createSinger();

      Builder builder = new JWTClaimsSet.Builder()
          .subject(subject)
          .issueTime(Calendar.getInstance().getTime())
          .expirationTime(expiration);
      claims.forEach(builder::claim);

      SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), builder.build());
      signedJWT.sign(signer);

      return signedJWT.serialize();
    } catch (JOSEException e) {
      throw new JwtSignatureException(Map.of("subject", subject), e);
    }
  }

  // 토큰 검증/갱신
  public Map<String, Object> getClaims(String jws) {
    SignedJWT signedJWT;
    try {
      signedJWT = SignedJWT.parse(jws);
    } catch (ParseException e) {
      throw new JwtSignatureException(Map.of("reason", "malformed"), e);
    }

    boolean verified;
    try {
      JWSVerifier verifier = createVerifier();
      verified = signedJWT.verify(verifier);
    } catch (JOSEException e) {
      throw new JwtSignatureException(Map.of("reason", "verify-error"), e);
    }

    if (!verified) {
      throw new JwtSignatureException(Map.of());
    }

    JWTClaimsSet claimsSet;
    try {
      claimsSet = signedJWT.getJWTClaimsSet();
    } catch (ParseException e) {
      throw new JwtSignatureException(Map.of("reason", "claims-parse-error"), e);
    }

    Date expiration = claimsSet.getExpirationTime();
    if (expiration == null || expiration.before(new Date())) {
      throw new JwtExpiredException(Map.of("expiredAt", expiration));
    }
    return claimsSet.getClaims();
  }

  // 만료 여부와 무관하게 위변조 여부만 확인
  public boolean verifySignatureOnly(String jws) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(jws);
      JWSVerifier verifier = createVerifier();
      return signedJWT.verify(verifier);
    } catch (ParseException | JOSEException e) {
      throw new JwtSignatureException(Map.of(), e);
    }
  }

  public boolean isExpired(String jws) {
    try {
      getClaims(jws);
      return false;
    } catch (JwtExpiredException e) {
      return true;
    }
  }

  // 갱신
  public String reissueAccessToken(String refreshToken) {
    Map<String, Object> claims = getClaims(refreshToken);
    UUID userId = UUID.fromString((String) claims.get("sub") != null
        ? (String) claims.get("sub")
        : extractSubject(refreshToken));
    return generateAccessToken(userId);
  }

  private String extractSubject(String jws) {
    try {
      return SignedJWT.parse(jws).getJWTClaimsSet().getSubject();
    } catch (ParseException e) {
      throw new JwtSignatureException(Map.of("reason", "malformed"), e);
    }
  }

  // 헬퍼 메서드

  private Date getTokenExpiration(int expirationMinutes) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, expirationMinutes);
    return calendar.getTime();
  }

  private JWSSigner createSinger() throws JOSEException {
    byte[] keyBytes = Base64.getDecoder().decode(base64EncodedSecretKey);
    return new MACSigner(keyBytes);
  }

  private JWSVerifier createVerifier() throws JOSEException {
    byte[] keyBytes = Base64.getDecoder().decode(base64EncodedSecretKey);
    return new MACVerifier(keyBytes);
  }


}
