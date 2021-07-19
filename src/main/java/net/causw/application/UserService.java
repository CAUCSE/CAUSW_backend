package net.causw.application;

import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.dto.UserSignInRequestDto;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserDomainModel;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserPort userPort;

    public UserService(UserPort userPort) {
        this.userPort = userPort;
    }

    public UserDetailDto findById(String id) {
        return this.userPort.findById(id);
    }

    public UserDetailDto create(UserCreateRequestDto user) {
        return this.userPort.create(user);
    }

    public UserDetailDto signIn(UserSignInRequestDto user) {
        UserFullDto userFullDto = this.userPort.findByEmail(user.getEmail());
        UserDomainModel userDomainModel = UserDomainModel.of(
                userFullDto.getId(),
                userFullDto.getEmail(),
                userFullDto.getName(),
                userFullDto.getPassword(),
                userFullDto.getAdmissionYear(),
                userFullDto.getRole().getValue(),
                userFullDto.getProfileImage(),
                userFullDto.getState().getValue()
        );

        if (!userDomainModel.validateSignInPassword(user.getPassword())) {
            throw new UnauthorizedException(
                    ErrorCode.INVALID_SIGNIN,
                    "Invalid sign in data"
            );
        }

        if (userDomainModel.getState().equalsIgnoreCase("blocked")) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "Blocked user"
            );
        }

        if (userDomainModel.getState().equalsIgnoreCase("inactive")) {
            throw new UnauthorizedException(
                    ErrorCode.INACTIVE_USER,
                    "Inactive user"
            );
        }

        return UserDetailDto.from(userDomainModel);
    }
}
