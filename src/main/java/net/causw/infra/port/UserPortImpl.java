package net.causw.infra.port;

import net.causw.domain.model.UserDomainModel;
import net.causw.domain.spi.UserPort;
import net.causw.infra.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserPortImpl implements UserPort {
    private final UserRepository userRepository;

    public UserPortImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDomainModel findById(String id) {
        // TODO: Throw specific exception
        return UserDomainModel.of(userRepository.findById(id).orElseThrow());
    }
}
