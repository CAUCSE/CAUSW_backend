package net.causw.app.main.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSignOutRequestDto {
    private String refreshToken;
    private String accessToken;
    private String fcmToken;
}
