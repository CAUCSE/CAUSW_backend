package net.causw.application;

import net.causw.application.spi.UserPort;
import net.causw.config.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.SocialLoginType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class SocialLoginFactory {
    private final Map<SocialLoginType, Supplier<SocialLogin>> map;

    public SocialLoginFactory() {
        this.map = new HashMap<>();

        this.map.put(SocialLoginType.KAKAO, KakaoLogin::new);
    }

    public SocialLogin getSocialLogin(SocialLoginType provider) {
        Supplier<SocialLogin> socialLoginSupplier = map.get(provider);
        if(socialLoginSupplier == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    "Invalid action parameter"
            );
        }
        return socialLoginSupplier.get();
    }
}
