package net.causw.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserTokenSaveRequestDto {
    private String deviceToken;
    private String os;
    private String deviceName;
}
