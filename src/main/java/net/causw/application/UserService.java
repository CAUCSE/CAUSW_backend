package net.causw.application;

import net.causw.application.dto.EmailDuplicatedCheckDto;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.dto.UserSignInRequestDto;
import net.causw.application.dto.UserUpdateRequestDto;
import net.causw.application.spi.UserPort;
import net.causw.config.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;
import net.causw.domain.validation.AdmissionYearValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.DuplicatedEmailValidator;
import net.causw.domain.validation.PasswordCorrectValidator;
import net.causw.domain.validation.PasswordFormatValidator;
import net.causw.domain.validation.UserStateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;

@Service
public class UserService {
    private final UserPort userPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final Validator validator;

    public UserService(UserPort userPort, JwtTokenProvider jwtTokenProvider, Validator validator) {
        this.userPort = userPort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public UserDetailDto findById(String id) {
        return this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );
    }

    @Transactional(readOnly = true)
    public UserDetailDto findByName(String name) {
        return this.userPort.findByName(name).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user name"
                )
        );
    }

    @Transactional
    public UserDetailDto signUp(UserCreateRequestDto userCreateRequestDto) {
        DuplicatedEmailValidator.of(this.userPort, userCreateRequestDto.getEmail())
                .validate();

        UserDomainModel userDomainModel = UserDomainModel.of(
                null,
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                Role.NONE,
                null,
                UserState.WAIT
        );

        ConstraintValidator.of(userDomainModel, this.validator)
                .linkWith(PasswordFormatValidator.of(userDomainModel.getPassword())
                        .linkWith(AdmissionYearValidator.of(userDomainModel.getAdmissionYear())))
                .validate();

        return this.userPort.create(userCreateRequestDto);
    }

    @Transactional(readOnly = true)
    public UserDetailDto signIn(UserSignInRequestDto userSignInRequestDto) {
        UserFullDto userFullDto = this.userPort.findByEmail(userSignInRequestDto.getEmail()).orElseThrow(
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

        PasswordCorrectValidator.of(userDomainModel, userSignInRequestDto.getPassword())
                .linkWith(UserStateValidator.of(userDomainModel))
                .validate();

        String jwtToken = this.jwtTokenProvider.createToken(userFullDto.getId(), userFullDto.getRole().getValue());

        return UserDetailDto.from(userDomainModel, jwtToken);
    }

    @Transactional(readOnly = true)
    public EmailDuplicatedCheckDto isDuplicatedEmail(String email) {
        return EmailDuplicatedCheckDto.of(this.userPort.findByEmail(email).isPresent());
    }

    @Transactional
    public UserDetailDto update(String id, UserUpdateRequestDto userUpdateRequestDto) {
        UserDetailDto userDetailDto = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        if (!userDetailDto.getEmail().equals(userUpdateRequestDto.getEmail())) {
            DuplicatedEmailValidator.of(this.userPort, userUpdateRequestDto.getEmail())
                    .validate();
        }

        UserDomainModel userDomainModel = UserDomainModel.of(
                id,
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getName(),
                userUpdateRequestDto.getPassword(),
                userUpdateRequestDto.getStudentId(),
                userUpdateRequestDto.getAdmissionYear(),
                userDetailDto.getRole(),
                userDetailDto.getProfileImage(),
                userDetailDto.getState()
        );

        ConstraintValidator.of(userDomainModel, this.validator)
                .linkWith(PasswordFormatValidator.of(userDomainModel.getPassword())
                        .linkWith(AdmissionYearValidator.of(userDomainModel.getAdmissionYear())))
                .validate();

        return this.userPort.update(id, userUpdateRequestDto).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );
    }
}
