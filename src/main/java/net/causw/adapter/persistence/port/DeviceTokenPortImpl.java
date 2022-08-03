package net.causw.adapter.persistence.port;

import net.causw.adapter.persistence.DeviceToken;
import net.causw.adapter.persistence.DeviceTokenRepository;
import net.causw.adapter.persistence.UserRepository;
import net.causw.application.spi.DeviceTokenPort;
import net.causw.domain.model.DeviceTokenDomainModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeviceTokenPortImpl extends DomainModelMapper implements DeviceTokenPort {
    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenPortImpl(
            UserRepository userRepository,
            DeviceTokenRepository deviceTokenRepository
    ) {
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @Override
    public DeviceTokenDomainModel create(DeviceTokenDomainModel deviceTokenDomainModel) {
        return this.entityToDomainModel(this.deviceTokenRepository.save(DeviceToken.from(deviceTokenDomainModel)));
    }

    @Override
    public List<DeviceTokenDomainModel> findByUserId(String userId) {
        return this.deviceTokenRepository.findByUser_Id(userId)
                .stream()
                .map(this::entityToDomainModel)
                .collect(Collectors.toList());
    }
}
