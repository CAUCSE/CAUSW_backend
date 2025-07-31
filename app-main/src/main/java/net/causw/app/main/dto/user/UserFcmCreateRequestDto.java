package net.causw.app.main.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFcmCreateRequestDto {
    private String fcmToken;
    private String refreshToken;
}