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
        // Make domain model for generalized data model and validate the format of request parameter
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

        // Validate password format, admission year range, and whether the email is duplicate or not
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

        /* Validate the input password and user state
         * The sign-in process is rejected if the user is in BLOCKED, WAIT, or INACTIVE state.
         */
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
        // First, load the user data from input user id
        UserFullDto userFullDto = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        /* The user requested changing the email if the request email is different from the original one
         * Then, validate it whether the requested email is duplicated or not
         */
        if (!userFullDto.getEmail().equals(userUpdateRequestDto.getEmail())) {
            DuplicatedEmailValidator.of(this.userPort, userUpdateRequestDto.getEmail())
                    .validate();
        }

        // Validate the requested parameters format from making the domain model
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

        // Validate the admission year range
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
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        // Load the user data from input grantor and grantee ids.
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

        /* Validate the role
         * 1) Combination of grantor role and the role to be granted must be acceptable
         * 2) Combination of grantor role and the grantee role must be acceptable
         */
        UpdatableGrantedRoleValidator.of(grantor.getRole(), userUpdateRoleRequestDto.getRole())
                .linkWith(UpdatableGranteeRoleValidator.of(grantor.getRole(), grantee.getRole()))
                .validate();

        /* Delegate the role
         * 1) Check if the grantor's role is same
         * 2) If yes, it is delegating process (The grantor may lose the role)
         * 3) Then, the DelegationFactory match the instance for the delegation considering the role -> Then processed
         */
        if (grantor.getRole() == userUpdateRoleRequestDto.getRole()) {
            DelegationFactory.create(grantor.getRole(), this.userPort, this.circlePort)
                    .delegate(grantorId, granteeId);
        }

        /* Grant the role
         * The linked updating process is performed on previous delegation process
         * Therefore, the updating for the grantee is performed in this process
         */
        return UserResponseDto.from(this.userPort.updateRole(granteeId, userUpdateRoleRequestDto.getRole()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        ));
    }
}
