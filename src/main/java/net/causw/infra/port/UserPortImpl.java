package net.causw.infra.port;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.infra.Role;
import net.causw.infra.User;
import net.causw.infra.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserPortImpl implements UserPort {
    private final UserRepository userRepository;

    public UserPortImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetailDto findById(String id) {
        return UserDetailDto.from(this.userRepository.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        ));
    }

    @Override
    public UserFullDto findByEmail(String email) {
        return UserFullDto.from(this.userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.INVALID_SIGNIN,
                        "Invalid sign in data"
                )
        ));
    }

    @Override
    public UserDetailDto create(UserCreateRequestDto user) {
        return UserDetailDto.from(this.userRepository.save(User.of(
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getAdmissionYear(),
                Role.VISITOR,
                false
        )));
    }
}
