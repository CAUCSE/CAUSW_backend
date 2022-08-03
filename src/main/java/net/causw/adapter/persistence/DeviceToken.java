package net.causw.adapter.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.DeviceTokenDomainModel;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TB_DEVICE_TOKEN")
public class DeviceToken extends BaseEntity {
    @Column(name = "device_token", unique = true, nullable = false)
    private String deviceToken;

    @Column(name = "os", nullable = false)
    private String os;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private DeviceToken(
        String id,
        String deviceToken,
        String os,
        String deviceName,
        User user
    ) {
        super(id);
        this.deviceToken = deviceToken;
        this.os = os;
        this.deviceName = deviceName;
        this.user = user;
    }

    private DeviceToken(
            String deviceToken,
            String os,
            String deviceName,
            User user
    ) {
        this.deviceToken = deviceToken;
        this.os = os;
        this.deviceName = deviceName;
        this.user = user;
    }

    public static DeviceToken of(
            String id,
            String deviceToken,
            String os,
            String deviceName,
            User user
    ) {
        return new DeviceToken(
                id,
                deviceToken,
                os,
                deviceName,
                user
        );
    }

    public static DeviceToken of(
            String deviceToken,
            String os,
            String deviceName,
            User user
    ) {
        return new DeviceToken(
                deviceToken,
                os,
                deviceName,
                user
        );
    }

    public static DeviceToken from(DeviceTokenDomainModel deviceTokenDomainModel) {
        return new DeviceToken(
                deviceTokenDomainModel.getId(),
                deviceTokenDomainModel.getDeviceToken(),
                deviceTokenDomainModel.getOs(),
                deviceTokenDomainModel.getDeviceName(),
                User.from(deviceTokenDomainModel.getUser())
        );
    }
}
