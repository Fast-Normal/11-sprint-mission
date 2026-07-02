package com.sprint.mission.discodeit.security.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

  private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
  private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      Supplier<CsrfToken> csrfToken) {
    // 응답에 토큰을 내려줄 때는 기존 디폴트(XOR 마스킹) 방식을 그대로 위임
    this.xor.handle(request, response, csrfToken);
    csrfToken.get();
  }

  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    // 헤더로 토큰이 온 경우(SPA의 JS가 쿠키값을 그대로 헤더에 넣어 보낸 경우)
    // 마스킹이 안 된 raw 값이므로 XOR 디코딩 없이 그대로 비교
    String headerValue = request.getHeader(csrfToken.getHeaderName());

    // 헤더가 없는 경우 기존 방식(XOR)으로 위임
    return (StringUtils.hasText(headerValue) ? this.plain : this.xor).resolveCsrfTokenValue(request,
        csrfToken);
  }

}
