package net.causw.app.main.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserFcmTokenResponseDto {
    private List<String> fcmToken;
}
