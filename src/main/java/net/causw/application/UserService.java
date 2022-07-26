package net.causw.application;

import net.causw.application.dto.DuplicatedCheckResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.comment.CommentsOfUserResponseDto;
import net.causw.application.dto.user.SocialSignInRequestDto;
import net.causw.application.dto.user.SocialSignInResponseDto;
import net.causw.application.dto.user.UserAdmissionCreateRequestDto;
import net.causw.application.dto.user.UserAdmissionResponseDto;
import net.causw.application.dto.user.UserAdmissionsResponseDto;
import net.causw.application.dto.user.UserCommentsResponseDto;
import net.causw.application.dto.user.UserCreateRequestDto;
import net.causw.application.dto.user.UserFindEmailRequestDto;
import net.causw.application.dto.user.UserPostResponseDto;
import net.causw.application.dto.user.UserPostsResponseDto;
import net.causw.application.dto.user.UserPrivilegedResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.user.UserSignInRequestDto;
import net.causw.application.dto.user.UserUpdatePasswordRequestDto;
import net.causw.application.dto.user.UserUpdateRequestDto;
import net.causw.application.dto.user.UserUpdateRoleRequestDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.FavoriteBoardPort;
import net.causw.application.spi.LockerLogPort;
import net.causw.application.spi.LockerPort;
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
import net.causw.domain.model.ImageLocation;
import net.causw.domain.model.LockerLogAction;
import net.causw.domain.model.PostDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.SocialLoginType;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserAdmissionDomainModel;
import net.causw.domain.model.UserAdmissionLogAction;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.model.UserState;
import net.causw.domain.validation.AdmissionYearValidator;
import net.causw.domain.validation.CircleMemberStatusValidator;
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
import net.causw.infrastructure.GcpFileUploader;
import net.causw.infrastructure.GoogleMailSender;
import net.causw.infrastructure.PasswordGenerator;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
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
    private final LockerPort lockerPort;
    private final LockerLogPort lockerLogPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final GcpFileUploader gcpFileUploader;
    private final GoogleMailSender googleMailSender;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    private final SocialLoginFactory socialLoginFactory;

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
            LockerPort lockerPort,
            LockerLogPort lockerLogPort,
            JwtTokenProvider jwtTokenProvider,
            GcpFileUploader gcpFileUploader,
            GoogleMailSender googleMailSender,
            PasswordGenerator passwordGenerator,
            PasswordEncoder passwordEncoder,
            Validator validator,
            SocialLoginFactory socialLoginFactory
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
        this.lockerPort = lockerPort;
        this.lockerLogPort = lockerLogPort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.gcpFileUploader = gcpFileUploader;
        this.googleMailSender = googleMailSender;
        this.passwordGenerator = passwordGenerator;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.socialLoginFactory = socialLoginFactory;
    }

    @Transactional(readOnly = true)
    public String findEmail(
            UserFindEmailRequestDto userFindEmailRequestDto
    ) {
        return this.userPort.findEmail(userFindEmailRequestDto.getEmail(), userFindEmailRequestDto.getStudentId())
                .map(UserDomainModel::getEmail)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "가입정보가 존재하지 않습니다."
                ));
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

        String newPassword = requestUser.updatePassword(this.passwordGenerator.generate());

        this.googleMailSender.sendNewPasswordMail(requestUser.getEmail(), newPassword);
        this.userPort.updatePassword(requestUser.getId(), passwordEncoder.encode(newPassword)).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        );

        return UserResponseDto.from(requestUser);
    }

    // Find process of another user
    @Transactional(readOnly = true)
    public UserResponseDto findById(String targetUserId, String requestUserId) {
        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT, Role.LEADER_CIRCLE)))
                .validate();

        if (requestUser.getRole().equals(Role.LEADER_CIRCLE)) {
            CircleDomainModel ownCircle = this.circlePort.findByLeaderId(requestUserId).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            "소모임장이 아닙니다."
                    )
            );

            this.circleMemberPort.findByUserIdAndCircleId(targetUserId, ownCircle.getId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.NOT_MEMBER,
                            "해당 유저는 소모임 회원이 아닙니다."
                    )
            );
        }

        return this.userPort.findById(targetUserId)
                .map(UserResponseDto::from)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "해당 사용자를 찾을 수 없습니다."
                ));
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
    public UserPostsResponseDto findPosts(String requestUserId, Integer pageNum) {
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

        return UserPostsResponseDto.from(
                requestUser,
                this.postPort.findByUserId(requestUserId, pageNum).map(postDomainModel -> UserPostResponseDto.from(
                        postDomainModel,
                        postDomainModel.getBoard().getId(),
                        postDomainModel.getBoard().getName(),
                        postDomainModel.getBoard().getCircle().map(CircleDomainModel::getId).orElse(null),
                        postDomainModel.getBoard().getCircle().map(CircleDomainModel::getName).orElse(null),
                        this.commentPort.countByPostId(postDomainModel.getId())
                ))
        );
    }

    @Transactional(readOnly = true)
    public UserCommentsResponseDto findComments(String requestUserId, Integer pageNum) {
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

        return UserCommentsResponseDto.from(
                requestUser,
                this.commentPort.findByUserId(requestUserId, pageNum).map(comment -> {
                    PostDomainModel post = this.postPort.findById(comment.getPostId()).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    "게시글을 찾을 수 없습니다."
                            )
                    );

                    return CommentsOfUserResponseDto.from(
                            comment,
                            post.getBoard().getId(),
                            post.getBoard().getName(),
                            post.getId(),
                            post.getTitle(),
                            post.getBoard().getCircle().map(CircleDomainModel::getId).orElse(null),
                            post.getBoard().getCircle().map(CircleDomainModel::getName).orElse(null)
                    );
                })
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
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.PRESIDENT, Role.LEADER_CIRCLE)))
                .validate();

        if (user.getRole().equals(Role.LEADER_CIRCLE)) {
            CircleDomainModel ownCircle = this.circlePort.findByLeaderId(currentUserId).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            "소모임장이 아닙니다"
                    )
            );

            return this.userPort.findByName(name)
                    .stream()
                    .filter(userDomainModel -> userDomainModel.getState().equals(UserState.ACTIVE))
                    .filter(userDomainModel ->
                            this.circleMemberPort.findByUserIdAndCircleId(userDomainModel.getId(), ownCircle.getId())
                                    .map(circleMemberDomainModel ->
                                            circleMemberDomainModel.getStatus() == CircleMemberStatus.MEMBER)
                                    .orElse(Boolean.FALSE))
                    .map(userDomainModel -> UserResponseDto.from(
                            userDomainModel,
                            ownCircle.getId(),
                            ownCircle.getName()))
                    .collect(Collectors.toList());
        }

        return this.userPort.findByName(name)
                .stream()
                .filter(userDomainModel -> userDomainModel.getState().equals(UserState.ACTIVE))
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserPrivilegedResponseDto findPrivilegedUsers(String currentUserId) {
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

        return UserPrivilegedResponseDto.from(
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
                        .map(userDomainModel -> {
                            CircleDomainModel ownCircle = this.circlePort.findByLeaderId(userDomainModel.getId()).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            "소모임장이 아닙니다"
                                    )
                            );

                            return UserResponseDto.from(
                                    userDomainModel,
                                    ownCircle.getId(),
                                    ownCircle.getName()
                            );
                        })
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
                .map(userDomainModel -> {
                    if (userDomainModel.getRole().equals(Role.LEADER_CIRCLE)) {
                        CircleDomainModel ownCircle = this.circlePort.findByLeaderId(userDomainModel.getId()).orElseThrow(
                                () -> new InternalServerException(
                                        ErrorCode.INTERNAL_SERVER,
                                        "소모임장이 아닙니다"
                                )
                        );

                        return UserResponseDto.from(
                                userDomainModel,
                                ownCircle.getId(),
                                ownCircle.getName()
                        );
                    } else {
                        return UserResponseDto.from(userDomainModel);
                    }
                });
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

        if (user.getRole().equals(Role.ADMIN)) {
            return this.circlePort.findAll()
                    .stream()
                    .map(CircleResponseDto::from)
                    .collect(Collectors.toList());
        }

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
                passwordEncoder.encode(userCreateRequestDto.getPassword()),
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
        UserDomainModel userDomainModel = this.userPort.findByEmail(userSignInRequestDto.getEmail()).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.INVALID_SIGNIN,
                        "잘못된 이메일 입니다."
                )
        );

        /* Validate the input password and user state
         * The sign-in process is rejected if the user is in BLOCKED, WAIT, or INACTIVE state.
         */
        ValidatorBucket.of()
                .consistOf(PasswordCorrectValidator.of(
                        this.passwordEncoder,
                        userDomainModel.getPassword(),
                        userSignInRequestDto.getPassword()))
                .validate();

        if (userDomainModel.getState() == UserState.AWAIT) {
            this.userAdmissionPort.findByUserId(userDomainModel.getId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.NO_APPLICATION,
                            "신청서를 작성하지 않았습니다."
                    )
            );
        }

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .validate();

        return this.jwtTokenProvider.createToken(
                userDomainModel.getId(),
                userDomainModel.getRole(),
                userDomainModel.getState()
        );
    }

    @Transactional
    public SocialSignInResponseDto socialLogin(SocialSignInRequestDto socialSignInRequestDto){
        return this.socialLoginFactory
                .getSocialLogin(SocialLoginType.of(socialSignInRequestDto.getProvider()))
                .returnJwtToken(userAdmissionPort, userPort, jwtTokenProvider, socialSignInRequestDto);
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedEmail(String email) {
        Optional<UserDomainModel> userFoundByEmail = this.userPort.findByEmail(email);
        if (userFoundByEmail.isPresent()) {
            UserState state = userFoundByEmail.get().getState();
            if (state.equals(UserState.INACTIVE) || state.equals(UserState.DROP)) {
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        "탈퇴한 계정의 재가입은 관리자에게 문의해주세요."
                );
            }
        }
        return DuplicatedCheckResponseDto.of(userFoundByEmail.isPresent());
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
        /* Delegate the Circle Leader
         * 1) Check if the grantor's role is Admin or President
         * 2) Check if the role to update is Circle Leader
         */
        else if ((grantor.getRole() == Role.PRESIDENT || grantor.getRole() == Role.ADMIN)
                && userUpdateRoleRequestDto.getRole() == Role.LEADER_CIRCLE
        ) {
            String circleId = userUpdateRoleRequestDto.getCircleId()
                    .orElseThrow(() -> new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            "소모임장을 위임할 소모임 입력이 필요합니다."
                    ));

            this.circleMemberPort.findByUserIdAndCircleId(granteeId, circleId)
                    .ifPresentOrElse(
                            circleMember ->
                                    ValidatorBucket.of()
                                            .consistOf(CircleMemberStatusValidator.of(
                                                    circleMember.getStatus(),
                                                    List.of(CircleMemberStatus.MEMBER)
                                            )).validate(),
                            () -> {
                                throw new UnauthorizedException(
                                        ErrorCode.NOT_MEMBER,
                                        "사용자가 가입 신청한 소모임이 아닙니다."
                                );
                            });

            this.circlePort.findById(circleId)
                    .ifPresentOrElse(circle -> {
                        this.circlePort.updateLeader(circle.getId(), grantee);
                        circle.getLeader().ifPresent(leader -> this.userPort.updateRole(leader.getId(), Role.COMMON));
                    }, () -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                "소모임을 찾을 수 없습니다."
                        );
                    });
        }
        /* Delegate the Leader Alumni
         * 1) Check if the grantor's role is Admin or President
         * 2) Check if the role to update is Leader Alumni
         */
        else if ((grantor.getRole() == Role.PRESIDENT || grantor.getRole() == Role.ADMIN)
                && userUpdateRoleRequestDto.getRole() == Role.LEADER_ALUMNI
        ) {
            UserDomainModel previousLeaderAlumni = this.userPort.findByRole(Role.LEADER_ALUMNI)
                    .stream().findFirst()
                    .orElseThrow(
                            () -> new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    "동문회장이 존재하지 않습니다."
                            ));

            this.userPort.updateRole(previousLeaderAlumni.getId(), Role.COMMON).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            "User id checked, but exception occurred"
                    )
            );
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
            UserUpdatePasswordRequestDto userUpdatePasswordRequestDto
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
                .consistOf(PasswordCorrectValidator.of(
                        this.passwordEncoder,
                        user.getPassword(),
                        userUpdatePasswordRequestDto.getOriginPassword())
                )
                .consistOf(PasswordFormatValidator.of(userUpdatePasswordRequestDto.getUpdatedPassword()))
                .validate();

        return UserResponseDto.from(this.userPort.updatePassword(
                        id,
                        this.passwordEncoder.encode(userUpdatePasswordRequestDto.getUpdatedPassword())
                )
                .orElseThrow(
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

        this.lockerPort.findByUserId(id)
                .ifPresent(lockerDomainModel -> {
                    lockerDomainModel.returnLocker();
                    this.lockerPort.update(lockerDomainModel.getId(), lockerDomainModel);

                    this.lockerLogPort.create(
                            lockerDomainModel.getLockerNumber(),
                            lockerDomainModel.getLockerLocation().getName(),
                            user,
                            LockerLogAction.RETURN,
                            "사용자 탈퇴"
                    );
                });

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

        this.lockerPort.findByUserId(userId)
                .ifPresent(lockerDomainModel -> {
                    lockerDomainModel.returnLocker();
                    this.lockerPort.update(lockerDomainModel.getId(), lockerDomainModel);

                    this.lockerLogPort.create(
                            lockerDomainModel.getLockerNumber(),
                            lockerDomainModel.getLockerLocation().getName(),
                            requestUser,
                            LockerLogAction.RETURN,
                            "사용자 추방"
                    );
                });

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
    public Page<UserAdmissionsResponseDto> findAllAdmissions(
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
                .map(UserAdmissionsResponseDto::from);
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

        String attachImage = userAdmissionCreateRequestDto.getAttachImage()
                .map(image ->
                        this.gcpFileUploader.uploadImageToGcp(image, ImageLocation.USER_ADMISSION))
                .orElse(null);

        UserAdmissionDomainModel userAdmissionDomainModel = UserAdmissionDomainModel.of(
                requestUser,
                attachImage,
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

        // Update user role to COMMON
        this.userPort.updateRole(userAdmissionDomainModel.getUser().getId(), Role.COMMON).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id of the admission checked, but exception occurred"
                )
        );

        // Add admission log
        this.userAdmissionLogPort.create(
                userAdmissionDomainModel.getUser().getEmail(),
                userAdmissionDomainModel.getUser().getName(),
                requestUser.getEmail(),
                requestUser.getName(),
                UserAdmissionLogAction.ACCEPT,
                userAdmissionDomainModel.getAttachImage(),
                userAdmissionDomainModel.getDescription()
        );

        // Remove the admission
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
