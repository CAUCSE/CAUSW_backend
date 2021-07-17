package net.causw.infra.port;

import net.causw.application.dto.UserAuthDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.application.spi.UserAuthPort;
import net.causw.infra.UserAuthRepository;
import org.springframework.stereotype.Component;

@Component
public class UserAuthPortImpl implements UserAuthPort {
    private final UserAuthRepository userAuthRepository;

    public UserAuthPortImpl(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public UserAuthDto findById(String id) {
        return UserAuthDto.from(this.userAuthRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user auth id"
                )
        ));
    }
}
