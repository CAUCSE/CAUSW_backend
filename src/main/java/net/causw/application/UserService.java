package net.causw.application;

import net.causw.application.dto.DuplicatedCheckDto;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserPasswordUpdateRequestDto;
import net.causw.application.dto.UserResponseDto;
import net.causw.application.dto.UserSignInRequestDto;
import net.causw.application.dto.UserUpdateRequestDto;
import net.causw.application.dto.UserUpdateRoleRequestDto;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.config.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;
import net.causw.domain.validation.AdmissionYearValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.DuplicatedEmailValidator;
import net.causw.domain.validation.GrantableRoleValidator;
import net.causw.domain.validation.PasswordCorrectValidator;
import net.causw.domain.validation.PasswordFormatValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;

@Service
public class UserService {
    private final UserPort userPort;
    private final CirclePort circlePort;
    private final CircleMemberPort circleMemberPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final Validator validator;

    public UserService(
            UserPort userPort,
            CirclePort circlePort,
            CircleMemberPort circleMemberPort,
            JwtTokenProvider jwtTokenProvider,
            Validator validator
    ) {
        this.userPort = userPort;
        this.circlePort = circlePort;
        this.circleMemberPort = circleMemberPort;
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
    public UserResponseDto findByName(String currentUserId, String name) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel user = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        validatorBucket
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        return UserResponseDto.from(this.userPort.findByName(name).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user name"
                )
        ));
    }

    @Transactional
    public UserResponseDto signUp(UserCreateRequestDto userCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        // Make domain model for generalized data model and validate the format of request parameter
        UserDomainModel userDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                null
        );

        // Validate password format, admission year range, and whether the email is duplicate or not
        validatorBucket
                .consistOf(ConstraintValidator.of(userDomainModel, this.validator))
                .consistOf(PasswordFormatValidator.of(userCreateRequestDto.getPassword()))
                .consistOf(AdmissionYearValidator.of(userCreateRequestDto.getAdmissionYear()))
                .consistOf(DuplicatedEmailValidator.of(this.userPort, userCreateRequestDto.getEmail()))
                .validate();

        return UserResponseDto.from(this.userPort.create(userDomainModel));
    }

    @Transactional(readOnly = true)
    public String signIn(UserSignInRequestDto userSignInRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findByEmail(userSignInRequestDto.getEmail()).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.INVALID_SIGNIN,
                        "Invalid sign in data"
                )
        );

        /* Validate the input password and user state
         * The sign-in process is rejected if the user is in BLOCKED, WAIT, or INACTIVE state.
         */
        validatorBucket
                .consistOf(PasswordCorrectValidator.of(userDomainModel, userSignInRequestDto.getPassword()))
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .validate();

        return this.jwtTokenProvider.createToken(
                userDomainModel.getId(),
                userDomainModel.getRole(),
                userDomainModel.getState()
        );
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckDto isDuplicatedEmail(String email) {
        return DuplicatedCheckDto.of(this.userPort.findByEmail(email).isPresent());
    }

    @Transactional
    public UserResponseDto update(String id, UserUpdateRequestDto userUpdateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        // First, load the user data from input user id
        UserDomainModel userDomainModel = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        /* The user requested changing the email if the request email is different from the original one
         * Then, validate it whether the requested email is duplicated or not
         */
        if (!userDomainModel.getEmail().equals(userUpdateRequestDto.getEmail())) {
            validatorBucket.consistOf(DuplicatedEmailValidator.of(this.userPort, userUpdateRequestDto.getEmail()));
        }

        // Validate the requested parameters format from making the domain model
        userDomainModel = UserDomainModel.of(
                id,
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getName(),
                userDomainModel.getPassword(),
                userUpdateRequestDto.getStudentId(),
                userUpdateRequestDto.getAdmissionYear(),
                userDomainModel.getRole(),
                userDomainModel.getProfileImage(),
                userDomainModel.getState()
        );

        // Validate the admission year range
        validatorBucket
                .consistOf(ConstraintValidator.of(userDomainModel, this.validator))
                .consistOf(AdmissionYearValidator.of(userUpdateRequestDto.getAdmissionYear()))
                .validate();

        return UserResponseDto.from(this.userPort.update(id, userDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public UserResponseDto updateUserRole(
            String grantorId,
            String granteeId,
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        // Load the user data from input grantor and grantee ids.
        UserDomainModel grantor = this.userPort.findById(grantorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid login user id"
                )
        );
        UserDomainModel grantee = this.userPort.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        /* Validate the role
         * 1) Combination of grantor role and the role to be granted must be acceptable
         * 2) Combination of grantor role and the grantee role must be acceptable
         */
        validatorBucket
                .consistOf(GrantableRoleValidator.of(
                        grantor.getRole(),
                        userUpdateRoleRequestDto.getRole(),
                        grantee.getRole()
                ))
                .validate();

        /* Delegate the role
         * 1) Check if the grantor's role is same
         * 2) If yes, it is delegating process (The grantor may lose the role)
         * 3) Then, the DelegationFactory match the instance for the delegation considering the role -> Then processed
         */
        if (grantor.getRole() == userUpdateRoleRequestDto.getRole()) {
            DelegationFactory
                    .create(grantor.getRole(), this.userPort, this.circlePort)
                    .delegate(grantorId, granteeId);
        }

        /* Grant the role
         * The linked updating process is performed on previous delegation process
         * Therefore, the updating for the grantee is performed in this process
         */
        return UserResponseDto.from(this.userPort.updateRole(granteeId, userUpdateRoleRequestDto.getRole()).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public UserResponseDto updatePassword(
            String id,
            UserPasswordUpdateRequestDto userPasswordUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel user = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid login user id"
                )
        );

        validatorBucket
                .consistOf(PasswordCorrectValidator.of(user, userPasswordUpdateRequestDto.getOriginPassword()))
                .consistOf(PasswordFormatValidator.of(userPasswordUpdateRequestDto.getUpdatedPassword()))
                .validate();

        return UserResponseDto.from(this.userPort.updatePassword(id, userPasswordUpdateRequestDto.getUpdatedPassword()).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public UserResponseDto leave(String id) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel user = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid login user id"
                )
        );

        validatorBucket
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.COMMON, Role.PROFESSOR)))
                .validate();

        // TODO: When we use session, should implement delete JWT
        // TODO: Should implement return locker and add log of locker

        // Change user role to NONE
        this.userPort.updateRole(id, Role.NONE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        );

        // Leave from circle where user joined
        this.circleMemberPort.findByUserId(id).forEach(
                circleMemberDomainModel ->
                        this.circleMemberPort.updateStatus(circleMemberDomainModel.getId(), CircleMemberStatus.LEAVE)
        );

        return UserResponseDto.from(this.userPort.updateState(id, UserState.INACTIVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        ));
    }
}
