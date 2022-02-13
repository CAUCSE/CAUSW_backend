package net.causw.application;

import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.CircleResponseDto;
import net.causw.application.dto.CommentAllForUserResponseDto;
import net.causw.application.dto.DuplicatedCheckDto;
import net.causw.application.dto.PostAllForUserResponseDto;
import net.causw.application.dto.UserAdmissionAllResponseDto;
import net.causw.application.dto.UserAdmissionCreateRequestDto;
import net.causw.application.dto.UserAdmissionResponseDto;
import net.causw.application.dto.UserCommentResponseDto;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserPasswordUpdateRequestDto;
import net.causw.application.dto.UserPostResponseDto;
import net.causw.application.dto.UserPrivilegedDto;
import net.causw.application.dto.UserResponseDto;
import net.causw.application.dto.UserSignInRequestDto;
import net.causw.application.dto.UserUpdateRequestDto;
import net.causw.application.dto.UserUpdateRoleRequestDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserAdmissionLogPort;
import net.causw.application.spi.UserAdmissionPort;
import net.causw.application.spi.UserPort;
import net.causw.config.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.FavoriteBoardDomainModel;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserAdmissionLogAction;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;
import net.causw.domain.validation.AdmissionYearValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.GrantableRoleValidator;
import net.causw.domain.validation.PasswordCorrectValidator;
import net.causw.domain.validation.PasswordFormatValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserRoleWithoutAdminValidator;
import net.causw.domain.validation.UserStateIsDropValidator;
import net.causw.domain.validation.UserStateIsNotDropAndActiveValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.infrastructure.GoogleMailSender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserPort userPort;
    private final BoardPort boardPort;
    private final PostPort postPort;
    private final UserAdmissionPort userAdmissionPort;
    private final UserAdmissionLogPort userAdmissionLogPort;
    private final CirclePort circlePort;
    private final CircleMemberPort circleMemberPort;
    private final CommentPort commentPort;
    private final FavoriteBoardPort favoriteBoardPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleMailSender googleMailSender;
    private final Validator validator;

    public UserService(
            UserPort userPort,
            BoardPort boardPort,
            PostPort postPort,
            UserAdmissionPort userAdmissionPort,
            UserAdmissionLogPort userAdmissionLogPort,
            CirclePort circlePort,
            CircleMemberPort circleMemberPort,
            CommentPort commentPort,
            FavoriteBoardPort favoriteBoardPort,
            JwtTokenProvider jwtTokenProvider,
            GoogleMailSender googleMailSender,
            Validator validator
    ) {
        this.userPort = userPort;
        this.boardPort = boardPort;
        this.postPort = postPort;
        this.userAdmissionPort = userAdmissionPort;
        this.userAdmissionLogPort = userAdmissionLogPort;
        this.circlePort = circlePort;
        this.circleMemberPort = circleMemberPort;
        this.commentPort = commentPort;
        this.favoriteBoardPort = favoriteBoardPort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleMailSender = googleMailSender;
        this.validator = validator;
    }

    @Transactional
    public UserResponseDto findPassword(
            String email,
            String name,
            String studentId
    ) {
        UserDomainModel requestUser = this.userPort.findForPassword(email, name, studentId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "해당 사용자를 찾을 수 없습니다."
                )
        );

        String newPassword = requestUser.updateRandomPassword();
        this.googleMailSender.sendNewPasswordMail(requestUser.getEmail(), newPassword);
        this.userPort.updatePassword(requestUser.getId(), newPassword).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        );

        return UserResponseDto.from(requestUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findById(String id) {
        UserDomainModel requestUser = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        if (requestUser.getRole().equals(Role.LEADER_CIRCLE)) {
            CircleDomainModel ownCircle = this.circlePort.findByLeaderId(id).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            "소모임장이 아닙니다"
                    )
            );

            return UserResponseDto.from(
                    requestUser,
                    ownCircle.getId(),
                    ownCircle.getName()
            );
        }

        return UserResponseDto.from(requestUser);
    }

    @Transactional(readOnly = true)
    public UserPostResponseDto findPosts(String requestUserId, Integer pageNum) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        return UserPostResponseDto.from(
                requestUser,
                new PageImpl<>(this.postPort.findByUserId(requestUserId, pageNum).getContent()
                        .stream().filter(
                                postDomainModel -> postDomainModel.getBoard().getCircle().map(
                                        circleDomainModel -> this.circleMemberPort.findByUserIdAndCircleId(requestUserId, circleDomainModel.getId()).map(
                                                circleMemberDomainModel -> !circleDomainModel.getIsDeleted() && (circleMemberDomainModel.getStatus() == CircleMemberStatus.MEMBER)
                                        ).orElse(false)
                                ).orElse(true)
                        )
                        .map(postDomainModel -> PostAllForUserResponseDto.from(
                                postDomainModel,
                                postDomainModel.getBoard().getName(),
                                postDomainModel.getBoard().getCircle().map(CircleDomainModel::getName).orElse(null),
                                this.commentPort.countByPostId(postDomainModel.getId())
                        )).collect(Collectors.toList()))
        );
    }

    @Transactional(readOnly = true)
    public UserCommentResponseDto findComments(String requestUserId, Integer pageNum) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        return UserCommentResponseDto.from(
                requestUser,
                new PageImpl<>(this.commentPort.findByUserId(requestUserId, pageNum).getContent()
                        .stream().filter(comment -> {
                            PostDomainModel post = this.postPort.findById(comment.getPostId()).orElseThrow(
                                    () -> new BadRequestException(
                                            ErrorCode.ROW_DOES_NOT_EXIST,
                                            "게시글을 찾을 수 없습니다."
                                    )
                            );

                            return post.getBoard().getCircle().map(
                                    circle -> this.circleMemberPort.findByUserIdAndCircleId(requestUserId, circle.getId()).map(
                                            circleMember -> !circle.getIsDeleted() && (circleMember.getStatus() == CircleMemberStatus.MEMBER)
                                    ).orElse(false)
                            ).orElse(true);
                        })
                        .map(comment -> {
                            PostDomainModel post = this.postPort.findById(comment.getPostId()).orElseThrow(
                                    () -> new BadRequestException(
                                            ErrorCode.ROW_DOES_NOT_EXIST,
                                            "게시글을 찾을 수 없습니다."
                                    )
                            );

                            return CommentAllForUserResponseDto.from(
                                    comment,
                                    post.getBoard().getName(),
                                    post.getTitle(),
                                    post.getBoard().getCircle().map(CircleDomainModel::getName).orElse(null)
                            );
                        }).collect(Collectors.toList()))
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findByName(String currentUserId, String name) {
        UserDomainModel user = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        return this.userPort.findByName(name)
                .stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserPrivilegedDto findPrivilegedUsers(String currentUserId) {
        UserDomainModel user = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        return UserPrivilegedDto.from(
                this.userPort.findByRole(Role.COUNCIL)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole(Role.LEADER_1)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole(Role.LEADER_2)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole(Role.LEADER_3)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole(Role.LEADER_4)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole(Role.LEADER_CIRCLE)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole(Role.LEADER_ALUMNI)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findByState(
            String currentUserId,
            String state,
            Integer pageNum
    ) {
        UserDomainModel user = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        return this.userPort.findByState(UserState.of(state), pageNum)
                .map(UserResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<CircleResponseDto> getCircleList(String currentUserId) {
        UserDomainModel user = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .validate();

        return this.circleMemberPort.getCircleListByUserId(user.getId())
                .stream()
                .filter(circle -> !circle.getIsDeleted())
                .map(CircleResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto signUp(UserCreateRequestDto userCreateRequestDto) {
        // Make domain model for generalized data model and validate the format of request parameter
        UserDomainModel userDomainModel = UserDomainModel.of(
                userCreateRequestDto.getEmail(),
                userCreateRequestDto.getName(),
                userCreateRequestDto.getPassword(),
                userCreateRequestDto.getStudentId(),
                userCreateRequestDto.getAdmissionYear(),
                userCreateRequestDto.getProfileImage()
        );

        this.userPort.findByEmail(userDomainModel.getEmail()).ifPresent(
                email -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            "중복된 이메일 입니다."
                    );
                }
        );

        // Validate password format, admission year range, and whether the email is duplicate or not
        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(userDomainModel, this.validator))
                .consistOf(PasswordFormatValidator.of(userCreateRequestDto.getPassword()))
                .consistOf(AdmissionYearValidator.of(userCreateRequestDto.getAdmissionYear()))
                .validate();

        return UserResponseDto.from(this.userPort.create(userDomainModel));
    }

    @Transactional(readOnly = true)
    public String signIn(UserSignInRequestDto userSignInRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findByEmail(userSignInRequestDto.getEmail()).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.INVALID_SIGNIN,
                        "잘못된 이메일 입니다."
                )
        );

        /* Validate the input password and user state
         * The sign-in process is rejected if the user is in BLOCKED, WAIT, or INACTIVE state.
         */
        validatorBucket
                .consistOf(PasswordCorrectValidator.of(userDomainModel, userSignInRequestDto.getPassword()));

        if (userDomainModel.getState() == UserState.AWAIT) {
            this.userAdmissionPort.findByUserId(userDomainModel.getId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.NO_APPLICATION,
                            "신청서를 작성하지 않았습니다."
                    )
            );
        }

        validatorBucket
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
        // First, load the user data from input user id
        UserDomainModel userDomainModel = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        /* The user requested changing the email if the request email is different from the original one
         * Then, validate it whether the requested email is duplicated or not
         */
        if (!userDomainModel.getEmail().equals(userUpdateRequestDto.getEmail())) {
            this.userPort.findByEmail(userUpdateRequestDto.getEmail()).ifPresent(
                    email -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                "이미 사용중인 이메일입니다."
                        );
                    }
            );
        }

        // Validate the requested parameters format from making the domain model
        userDomainModel.update(
                userUpdateRequestDto.getEmail(),
                userUpdateRequestDto.getName(),
                userUpdateRequestDto.getStudentId(),
                userUpdateRequestDto.getAdmissionYear(),
                userUpdateRequestDto.getProfileImage()
        );

        // Validate the admission year range
        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(ConstraintValidator.of(userDomainModel, this.validator))
                .consistOf(AdmissionYearValidator.of(userUpdateRequestDto.getAdmissionYear()))
                .validate();

        return UserResponseDto.from(this.userPort.update(id, userDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
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
        UserDomainModel grantor = this.userPort.findById(grantorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );
        UserDomainModel grantee = this.userPort.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "권한을 받을 사용자를 찾을 수 없습니다."
                )
        );

        /* Validate the role
         * 1) Combination of grantor role and the role to be granted must be acceptable
         * 2) Combination of grantor role and the grantee role must be acceptable
         */
        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(grantor.getState()))
                .consistOf(UserRoleIsNoneValidator.of(grantor.getRole()))
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
                    .create(grantor.getRole(), this.userPort, this.circlePort, this.circleMemberPort)
                    .delegate(grantorId, granteeId);
        }

        /* Grant the role
         * The linked updating process is performed on previous delegation process
         * Therefore, the updating for the grantee is performed in this process
         */
        return UserResponseDto.from(this.userPort.updateRole(granteeId, userUpdateRoleRequestDto.getRole()).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public UserResponseDto updatePassword(
            String id,
            UserPasswordUpdateRequestDto userPasswordUpdateRequestDto
    ) {
        UserDomainModel user = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(PasswordCorrectValidator.of(user, userPasswordUpdateRequestDto.getOriginPassword()))
                .consistOf(PasswordFormatValidator.of(userPasswordUpdateRequestDto.getUpdatedPassword()))
                .validate();

        return UserResponseDto.from(this.userPort.updatePassword(id, userPasswordUpdateRequestDto.getUpdatedPassword()).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public UserResponseDto leave(String id) {
        UserDomainModel user = this.userPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleWithoutAdminValidator.of(user.getRole(), List.of(Role.COMMON, Role.PROFESSOR)))
                .validate();

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

    @Transactional
    public UserResponseDto dropUser(String requestUserId, String userId) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        UserDomainModel droppedUser = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "내보낼 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(UserRoleWithoutAdminValidator.of(droppedUser.getRole(), List.of(Role.COMMON, Role.PROFESSOR)))
                .validate();

        this.userPort.updateRole(userId, Role.NONE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        );

        return UserResponseDto.from(this.userPort.updateState(userId, UserState.DROP).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        ));
    }

    @Transactional(readOnly = true)
    public UserAdmissionResponseDto findAdmissionById(String requestUserId, String admissionId) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        return UserAdmissionResponseDto.from(this.userAdmissionPort.findById(admissionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "사용자의 가입 신청을 찾을 수 없습니다."
                )
        ));
    }

    @Transactional(readOnly = true)
    public Page<UserAdmissionAllResponseDto> findAllAdmissions(
            String requestUserId,
            Integer pageNum
    ) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        return this.userAdmissionPort.findAll(UserState.AWAIT, pageNum)
                .map(UserAdmissionAllResponseDto::from);
    }

    @Transactional
    public UserAdmissionResponseDto createAdmission(UserAdmissionCreateRequestDto userAdmissionCreateRequestDto) {
        UserDomainModel requestUser = this.userPort.findByEmail(userAdmissionCreateRequestDto.getEmail()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        if (this.userAdmissionPort.existsByUserId(requestUser.getId())) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    "이미 신청한 사용자 입니다."
            );
        }

        UserAdmissionDomainModel userAdmissionDomainModel = UserAdmissionDomainModel.of(
                requestUser,
                userAdmissionCreateRequestDto.getAttachImage(),
                userAdmissionCreateRequestDto.getDescription()
        );

        ValidatorBucket.of()
                .consistOf(UserStateIsNotDropAndActiveValidator.of(requestUser.getState()))
                .consistOf(ConstraintValidator.of(userAdmissionDomainModel, this.validator))
                .validate();

        return UserAdmissionResponseDto.from(this.userAdmissionPort.create(userAdmissionDomainModel));
    }

    @Transactional
    public UserAdmissionResponseDto accept(
            String requestUserId,
            String admissionId
    ) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        UserAdmissionDomainModel userAdmissionDomainModel = this.userAdmissionPort.findById(admissionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "사용자의 가입 신청을 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        // Create default favorite board
        this.boardPort.findOldest3Boards()
                .forEach(boardDomainModel ->
                        this.favoriteBoardPort.create(FavoriteBoardDomainModel.of(
                                userAdmissionDomainModel.getUser(),
                                boardDomainModel
                        ))
                );

        this.userPort.updateRole(userAdmissionDomainModel.getUser().getId(), Role.COMMON).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id of the admission checked, but exception occurred"
                )
        );

        this.userAdmissionLogPort.create(
                userAdmissionDomainModel.getUser().getEmail(),
                userAdmissionDomainModel.getUser().getName(),
                requestUser.getEmail(),
                requestUser.getName(),
                UserAdmissionLogAction.ACCEPT,
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription()
        );

        this.userAdmissionPort.delete(userAdmissionDomainModel);

        return UserAdmissionResponseDto.from(
                userAdmissionDomainModel,
                this.userPort.updateState(userAdmissionDomainModel.getUser().getId(), UserState.ACTIVE).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "User id of the admission checked, but exception occurred"
                        )
                )
        );
    }

    @Transactional
    public UserAdmissionResponseDto reject(
            String requestUserId,
            String admissionId
    ) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        UserAdmissionDomainModel userAdmissionDomainModel = this.userAdmissionPort.findById(admissionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "사용자의 가입 신청을 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        this.userAdmissionLogPort.create(
                userAdmissionDomainModel.getUser().getEmail(),
                userAdmissionDomainModel.getUser().getName(),
                requestUser.getEmail(),
                requestUser.getName(),
                UserAdmissionLogAction.REJECT,
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription()
        );

        this.userAdmissionPort.delete(userAdmissionDomainModel);

        return UserAdmissionResponseDto.from(
                userAdmissionDomainModel,
                this.userPort.updateState(userAdmissionDomainModel.getUser().getId(), UserState.REJECT).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "User id of the admission checked, but exception occurred"
                        )
                ));
    }

    @Transactional
    public BoardResponseDto createFavoriteBoard(
            String userId,
            String boardId
    ) {
        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        BoardDomainModel board = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "게시판을 찾을 수 없습니다."
                )
        );

        FavoriteBoardDomainModel favoriteBoardDomainModel = FavoriteBoardDomainModel.of(
                user,
                board
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD))
                .consistOf(ConstraintValidator.of(favoriteBoardDomainModel, this.validator))
                .validate();

        return BoardResponseDto.from(
                this.favoriteBoardPort.create(favoriteBoardDomainModel).getBoardDomainModel(),
                user.getRole()
        );
    }

    @Transactional
    public UserResponseDto restore(
            String requestUserId,
            String userId
    ) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        UserDomainModel restoredUser = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "복구할 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(UserStateIsDropValidator.of(restoredUser.getState()))
                .validate();

        this.userPort.updateRole(restoredUser.getId(), Role.COMMON).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        );

        return UserResponseDto.from(this.userPort.updateState(restoredUser.getId(), UserState.ACTIVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        ));
    }
}
