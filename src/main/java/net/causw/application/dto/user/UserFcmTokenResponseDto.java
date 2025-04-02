package net.causw.application.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserFcmTokenResponseDto {
    private List<String> fcmToken;
}
