package net.causw.application;

import net.causw.application.dto.EmailDuplicatedCheckDto;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.dto.UserResponseDto;
import net.causw.application.dto.UserSignInRequestDto;
import net.causw.application.dto.UserUpdateRequestDto;
import net.causw.application.dto.UserUpdateRoleRequestDto;
import net.causw.application.spi.CirclePort;
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
import net.causw.domain.validation.UpdatableGrantedRoleValidator;
import net.causw.domain.validation.UpdatableGranteeRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;

@Service
public class UserService {
    private final UserPort userPort;
    private final CirclePort circlePort;
    private final JwtTokenProvider jwtTokenProvider;
    private final Validator validator;

    public UserService(
            UserPort userPort,
            CirclePort circlePort,
            JwtTokenProvider jwtTokenProvider,
            Validator validator
    ) {
        this.userPort = userPort;
        this.circlePort = circlePort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(String id) {
        return UserResponseDto.from(this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        ));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByName(String name) {
        return UserResponseDto.from(this.userPort.findByName(name).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user name"
                )
        ));
    }

    @Transactional
    public UserResponseDto signUp(UserCreateRequestDto userCreateRequestDto) {
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
                        .linkWith(AdmissionYearValidator.of(userDomainModel.getAdmissionYear())
                                .linkWith(DuplicatedEmailValidator.of(this.userPort, userCreateRequestDto.getEmail()))))
                .validate();

        return UserResponseDto.from(this.userPort.create(userCreateRequestDto));
    }

    @Transactional(readOnly = true)
    public String signIn(UserSignInRequestDto userSignInRequestDto) {
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

        return this.jwtTokenProvider.createToken(userFullDto.getId());
    }

    @Transactional(readOnly = true)
    public EmailDuplicatedCheckDto isDuplicatedEmail(String email) {
        return EmailDuplicatedCheckDto.of(this.userPort.findByEmail(email).isPresent());
    }

    @Transactional
    public UserResponseDto update(String id, UserUpdateRequestDto userUpdateRequestDto) {
        UserFullDto userFullDto = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        if (!userFullDto.getEmail().equals(userUpdateRequestDto.getEmail())) {
            DuplicatedEmailValidator.of(this.userPort, userUpdateRequestDto.getEmail())
                    .validate();
        }

        UserDomainModel userDomainModel = UserDomainModel.of(
                id,
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getName(),
                userFullDto.getPassword(),
                userUpdateRequestDto.getStudentId(),
                userUpdateRequestDto.getAdmissionYear(),
                userFullDto.getRole(),
                userFullDto.getProfileImage(),
                userFullDto.getState()
        );

        ConstraintValidator.of(userDomainModel, this.validator)
                .linkWith(AdmissionYearValidator.of(userDomainModel.getAdmissionYear()))
                .validate();

        return UserResponseDto.from(this.userPort.update(id, userUpdateRequestDto).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        ));
    }

    @Transactional
    public UserResponseDto updateUserRole(
            String grantorId,
            String granteeId,
            UserUpdateRoleRequestDto userUpdateRoleRequestDto) {
        UserFullDto grantor = this.userPort.findById(grantorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid login user id"
                )
        );
        UserFullDto grantee = this.userPort.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        // Validate
        UpdatableGrantedRoleValidator.of(grantor.getRole(), userUpdateRoleRequestDto.getRole())
                .linkWith(UpdatableGranteeRoleValidator.of(grantor.getRole(), grantee.getRole()))
                .validate();

        // Delegate
        if (grantor.getRole() == userUpdateRoleRequestDto.getRole()) {
            DelegationFactory.create(grantor.getRole(), this.userPort, this.circlePort)
                    .delegate(grantorId, granteeId);
        }

        // Grant
        return UserResponseDto.from(this.userPort.updateRole(granteeId, userUpdateRoleRequestDto.getRole()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        ));
    }
}
