package net.causw.application;

import net.causw.application.dto.EmailDuplicatedCheckDto;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.dto.UserSignInRequestDto;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserPort userPort;

    public UserService(UserPort userPort) {
        this.userPort = userPort;
    }

    public UserDetailDto findById(String id) {
        return this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );
    }

    public UserDetailDto findByName(String name) {
        return this.userPort.findByName(name).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user name"
                )
        );
    }

    public UserDetailDto create(UserCreateRequestDto user) {
        return this.userPort.create(user);
    }

    public UserDetailDto signIn(UserSignInRequestDto user) {
        UserFullDto userFullDto = this.userPort.findByEmail(user.getEmail()).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.INVALID_SIGNIN,
                        "Invalid sign in data"
                )
        );

        UserDomainModel userDomainModel = UserDomainModel.of(
                userFullDto.getId(),
                userFullDto.getEmail(),
                userFullDto.getName(),
                userFullDto.getPassword(),
                userFullDto.getStudentId(),
                userFullDto.getAdmissionYear(),
                userFullDto.getRole(),
                userFullDto.getProfileImage(),
                userFullDto.getState()
        );

        if (!userDomainModel.validateSignInPassword(user.getPassword())) {
            throw new UnauthorizedException(
                    ErrorCode.INVALID_SIGNIN,
                    "Invalid sign in data"
            );
        }

        if (userDomainModel.getState() == UserState.BLOCKED) {
            throw new UnauthorizedException(
                    ErrorCode.BLOCKED_USER,
                    "Blocked user"
            );
        }

        if (userDomainModel.getState() == UserState.INACTIVE) {
            throw new UnauthorizedException(
                    ErrorCode.INACTIVE_USER,
                    "Inactive user"
            );
        }

        return UserDetailDto.from(userDomainModel);
    }

    public EmailDuplicatedCheckDto isDuplicatedEmail(String email) {
        return EmailDuplicatedCheckDto.of(this.userPort.findByEmail(email).isPresent());
    }
}
