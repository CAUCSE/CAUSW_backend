package net.causw.infra.port;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
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
        return UserDomainModel.of(this.userRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        ));
    }
}
