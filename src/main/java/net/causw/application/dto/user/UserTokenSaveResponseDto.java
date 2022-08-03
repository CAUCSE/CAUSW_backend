package net.causw.application.dto.user;

import lombok.Getter;
import lombok.Setter;
import net.causw.domain.model.DeviceTokenDomainModel;
import net.causw.domain.model.UserDomainModel;

@Getter
@Setter
public class UserTokenSaveResponseDto {
    private UserResponseDto user;
    private String deviceToken;
    private String os;
    private String deviceName;

    private UserTokenSaveResponseDto(
            UserResponseDto user,
            String deviceToken,
            String os,
            String deviceName
    ) {
        this.user = user;
        this.deviceToken = deviceToken;
        this.os = os;
        this.deviceName = deviceName;
    }

    public static UserTokenSaveResponseDto from(
            UserDomainModel user,
            DeviceTokenDomainModel device
    ) {
        return new UserTokenSaveResponseDto(
                UserResponseDto.from(user),
                device.getDeviceToken(),
                device.getOs(),
                device.getDeviceName()
        );
    }
}
