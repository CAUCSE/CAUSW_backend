package net.causw.application;

import net.causw.application.dto.user.SocialSignInRequestDto;
import net.causw.application.dto.user.SocialSignInResponseDto;
import net.causw.application.spi.UserPort;
import net.causw.config.JwtTokenProvider;

import java.util.Optional;

public interface SocialLogin {
    SocialSignInResponseDto returnJwtToken(UserPort userPort, JwtTokenProvider jwtTokenProvider, SocialSignInRequestDto socialSignInRequestDto);
}
