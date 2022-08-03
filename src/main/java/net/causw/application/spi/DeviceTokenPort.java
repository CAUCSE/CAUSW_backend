package net.causw.application.spi;

import net.causw.domain.model.DeviceTokenDomainModel;

import java.util.List;

public interface DeviceTokenPort {
    DeviceTokenDomainModel create(DeviceTokenDomainModel deviceTokenDomainModel);

    List<DeviceTokenDomainModel> findByUserId(String userId);
}
