package net.causw.infra.port;

import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.spi.UserAuthPort;
import net.causw.infra.UserAuth;
import net.causw.infra.UserAuthRepository;

public class UserAuthPortImpl implements UserAuthPort {
    private final UserAuthRepository userAuthRepository;

    public UserAuthPortImpl(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public UserAuth findById(String id) {
        return this.userAuthRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user auth id"
                )
        );
    }
}
