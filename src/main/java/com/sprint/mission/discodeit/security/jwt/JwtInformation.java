package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.user.UserDto;
import java.time.Instant;

public record JwtInformation(
    UserDto userDto,
    String accessToken,
    String refreshToken,
    Instant expiration
) {

}
