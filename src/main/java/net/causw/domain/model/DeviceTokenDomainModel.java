package net.causw.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class DeviceTokenDomainModel {
    private String id;

    @NotNull(message = "토큰이 입력되지 않았습니다.")
    private String deviceToken;

    @NotNull(message = "운영체제가 입력되지 않았습니다.")
    private String os;

    private String deviceName;

    private UserDomainModel user;

    private DeviceTokenDomainModel(
            String id,
            String deviceToken,
            String os,
            String deviceName,
            UserDomainModel user
    ) {
        this.id = id;
        this.deviceToken = deviceToken;
        this.os = os;
        this.deviceName = deviceName;
        this.user = user;
    }

    public static DeviceTokenDomainModel of(
            String id,
            String deviceToken,
            String os,
            String deviceName,
            UserDomainModel user
    ) {
        return new DeviceTokenDomainModel(
                id,
                deviceToken,
                os,
                deviceName,
                user
        );
    }

    public static DeviceTokenDomainModel of(
            String deviceToken,
            String os,
            String deviceName,
            UserDomainModel user
    ) {
        return new DeviceTokenDomainModel(
                null,
                deviceToken,
                os,
                deviceName,
                user
        );
    }
}
