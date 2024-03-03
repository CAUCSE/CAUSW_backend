package net.causw.application.user;

import lombok.RequiredArgsConstructor;
import net.causw.application.delegation.DelegationFactory;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.comment.CommentsOfUserResponseDto;
import net.causw.application.dto.user.*;
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
import net.causw.config.security.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.board.FavoriteBoardDomainModel;
import net.causw.domain.model.enums.LockerLogAction;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserAdmissionDomainModel;
import net.causw.domain.model.enums.UserAdmissionLogAction;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.model.enums.UserState;
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
@RequiredArgsConstructor
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
    //private final GcpFileUploader gcpFileUploader;
    private final GoogleMailSender googleMailSender;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    @Transactional
    public UserResponseDto findPassword(
            UserFindPasswordRequestDto userFindPasswordRequestDto
    ) {
        UserDomainModel requestUser = this.userPort.findForPassword(userFindPasswordRequestDto.getEmail(), userFindPasswordRequestDto.getName(), userFindPasswordRequestDto.getStudentId()).orElseThrow(
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
    public UserResponseDto findByUserId(String targetUserId, String loginUserId) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );


        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(),
                        List.of(Role.LEADER_CIRCLE,
                                Role.VICE_PRESIDENT_N_LEADER_CIRCLE,
                                Role.COUNCIL_N_LEADER_CIRCLE,
                                Role.LEADER_1_N_LEADER_CIRCLE,
                                Role.LEADER_2_N_LEADER_CIRCLE,
                                Role.LEADER_3_N_LEADER_CIRCLE,
                                Role.LEADER_4_N_LEADER_CIRCLE
                        )))
                .validate();

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE") && !requestUser.getRole().getValue().contains("PRESIDENT")) {
            List<CircleDomainModel> ownCircles = this.circlePort.findByLeaderId(loginUserId);
            if (ownCircles.isEmpty()) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "해당 동아리장이 배정된 동아리가 없습니다."
                );
            }
            boolean isMemberOfAnyCircle = ownCircles.stream()
                    .anyMatch(circleDomainModel ->
                            this.circleMemberPort.findByUserIdAndCircleId(targetUserId, circleDomainModel.getId())
                                    .map(circleMemberDomainModel -> circleMemberDomainModel.getStatus() == CircleMemberStatus.MEMBER)
                                    .orElse(false));
            if (!isMemberOfAnyCircle) {
                throw new BadRequestException(ErrorCode.NOT_MEMBER, "해당 유저는 동아리 회원이 아닙니다.");
            }
        }

        return this.userPort.findById(targetUserId)
                .map(UserResponseDto::from)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "해당 사용자를 찾을 수 없습니다."
                ));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findCurrentUser(String loginUserId) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE")) {
            List<CircleDomainModel> ownCircles = this.circlePort.findByLeaderId(loginUserId);
            if (ownCircles.isEmpty()) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "해당 동아리장이 배정된 동아리가 없습니다."
                );
            }

            return UserResponseDto.from(
                    requestUser,
                    ownCircles.stream().map(CircleDomainModel::getId).collect(Collectors.toList()),
                    ownCircles.stream().map(CircleDomainModel::getName).collect(Collectors.toList())

            );
        }

        return UserResponseDto.from(requestUser);
    }

    @Transactional(readOnly = true)
    public UserPostsResponseDto findPosts(String loginUserId, Integer pageNum) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
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
                this.postPort.findPostByUserId(loginUserId, pageNum).map(postDomainModel -> UserPostResponseDto.from(
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
    public UserCommentsResponseDto findComments(String loginUserId, Integer pageNum) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
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
                this.commentPort.findByUserId(loginUserId, pageNum).map(comment -> {
                    PostDomainModel post = this.postPort.findPostById(comment.getPostId()).orElseThrow(
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
    public List<UserResponseDto> findByName(String loginUserId, String name) {
        UserDomainModel user = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleValidator.of(user.getRole(),
                        List.of(Role.LEADER_CIRCLE,
                                Role.VICE_PRESIDENT_N_LEADER_CIRCLE,
                                Role.COUNCIL_N_LEADER_CIRCLE,
                                Role.LEADER_1_N_LEADER_CIRCLE,
                                Role.LEADER_2_N_LEADER_CIRCLE,
                                Role.LEADER_3_N_LEADER_CIRCLE,
                                Role.LEADER_4_N_LEADER_CIRCLE
                        )))
                .validate();

        if (user.getRole().getValue().contains("LEADER_CIRCLE") && !user.getRole().getValue().contains("PRESIDENT")) {
            List<CircleDomainModel> ownCircles = this.circlePort.findByLeaderId(loginUserId);
            if (ownCircles.isEmpty()) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "해당 동아리장이 배정된 동아리가 없습니다."
                );
            }

            return this.userPort.findByName(name)
                    .stream()
                    .filter(userDomainModel -> userDomainModel.getState().equals(UserState.ACTIVE))
                    .filter(userDomainModel ->
                            ownCircles.stream()
                                    .anyMatch(circleDomainModel ->
                                            this.circleMemberPort.findByUserIdAndCircleId(userDomainModel.getId(), circleDomainModel.getId())
                                                    .map(circleMemberDomainModel ->
                                                            circleMemberDomainModel.getStatus() == CircleMemberStatus.MEMBER)
                                    .orElse(false)))
                    .map(userDomainModel -> UserResponseDto.from(
                            userDomainModel,
                            ownCircles.stream().map(CircleDomainModel::getId).collect(Collectors.toList()),
                            ownCircles.stream().map(CircleDomainModel::getName).collect(Collectors.toList())))
                    .collect(Collectors.toList());
        }

        return this.userPort.findByName(name)
                .stream()
                .filter(userDomainModel -> userDomainModel.getState().equals(UserState.ACTIVE))
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserPrivilegedResponseDto findPrivilegedUsers(String loginUserId) {
        UserDomainModel user = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of()))
                .validate();

        return UserPrivilegedResponseDto.from(
                this.userPort.findByRole("PRESIDENT")
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole("COUNCIL")
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole("LEADER_1")
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole("LEADER_2")
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole("LEADER_3")
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole("LEADER_4")
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole("LEADER_CIRCLE")
                        .stream()
                        .map(userDomainModel -> {
                            List<CircleDomainModel> ownCircles = this.circlePort.findByLeaderId(userDomainModel.getId());
                            if (ownCircles.isEmpty()) {
                                throw new InternalServerException(
                                        ErrorCode.INTERNAL_SERVER,
                                        "해당 동아리장이 배정된 동아리가 없습니다."
                                );
                            }
                            return UserResponseDto.from(
                                    userDomainModel,
                                    ownCircles.stream().map(CircleDomainModel::getId).collect(Collectors.toList()),
                                    ownCircles.stream().map(CircleDomainModel::getName).collect(Collectors.toList())
                            );
                        })
                        .collect(Collectors.toList()),
                this.userPort.findByRole("LEADER_ALUMNI")
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userPort.findByRole("VICE_PRESIDENT")
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findByState(
            String loginUserId,
            String state,
            String name,
            Integer pageNum
    ) {
        UserDomainModel user = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of()))
                .validate();

        return this.userPort.findByStateAndName(UserState.of(state), name, pageNum)
                .map(userDomainModel -> {
                    if (userDomainModel.getRole().getValue().contains("LEADER_CIRCLE") && !state.equals("INACTIVE")) {
                        List<CircleDomainModel> ownCircles = this.circlePort.findByLeaderId(userDomainModel.getId());
                        if (ownCircles.isEmpty()) {
                            throw new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    "해당 동아리장이 배정된 동아리가 없습니다."
                            );
                        }

                        return UserResponseDto.from(
                                userDomainModel,
                                ownCircles.stream().map(CircleDomainModel::getId).collect(Collectors.toList()),
                                ownCircles.stream().map(CircleDomainModel::getName).collect(Collectors.toList())
                        );
                    } else {
                        return UserResponseDto.from(userDomainModel);
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<CircleResponseDto> getCircleList(String loginUserId) {
        UserDomainModel user = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .validate();

        if (user.getRole().equals(Role.ADMIN) || user.getRole().getValue().contains("PRESIDENT")) {
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

    /**
     * 회원가입 메소드
     * @param userCreateRequestDto
     * @return UserResponseDto
     */
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

    @Transactional
    public UserSignInResponseDto signIn(UserSignInRequestDto userSignInRequestDto) {
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

        // refreshToken은 user DB에 보관 (추후 redis로 옮기면 좋을듯)
        String refreshToken = jwtTokenProvider.createRefreshToken();
        this.userPort.updateRefreshToken(userDomainModel.getId(), refreshToken);

        return UserSignInResponseDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(userDomainModel.getId(), userDomainModel.getRole(), userDomainModel.getState()))
                .refreshToken(jwtTokenProvider.createRefreshToken())
                .build();
    }

    /**
     * 이메일 중복 확인 메소드
     * @param email
     * @return DuplicatedCheckResponseDto
     */
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

    /**
     * 사용자 정보 업데이트 메소드
     * @param loginUserId
     * @param userUpdateRequestDto
     * @return UserResponseDto
     */
    @Transactional
    public UserResponseDto update(String loginUserId, UserUpdateRequestDto userUpdateRequestDto) {
        // First, load the user data from input user id
        UserDomainModel userDomainModel = this.userPort.findById(loginUserId).orElseThrow(
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

        return UserResponseDto.from(this.userPort.update(loginUserId, userDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        ));
    }

    /**
     * 사용자 권한 업데이트 메소드
     * @param loginUserId
     * @param granteeId
     * @param userUpdateRoleRequestDto
     * @return UserResponseDto
     */
    @Transactional
    public UserResponseDto updateUserRole(
            String loginUserId,
            String granteeId,
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        // Load the user data from input grantor and grantee ids.
        UserDomainModel grantor = this.userPort.findById(loginUserId).orElseThrow(
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
        /* 권한 위임
        * 1. 권한 위임자와 넘겨주는 권한이 같을 경우, 권한을 위임자가 동아리장일 경우 진행
        * 2. 넘겨받을 권한이 동아리장일 경우 넘겨받을 동아리 id 저장
        * 3. DelegationFactory를 통해 권한 위임 진행(동아리장 위임일 경우 circle id를 넘겨주어서 어떤 동아리의 동아리장 권한을 위임하는 것인지 확인)
        * */

        if (grantor.getRole() == userUpdateRoleRequestDto.getRole() || grantor.getRole().getValue().contains("LEADER_CIRCLE")){
            String circleId = "";
            if(userUpdateRoleRequestDto.getRole().equals(Role.LEADER_CIRCLE)){
                circleId = userUpdateRoleRequestDto.getCircleId()
                        .orElseThrow(() -> new BadRequestException(
                                ErrorCode.INVALID_PARAMETER,
                                "소모임장을 위임할 소모임 입력이 필요합니다."
                        ));
            }
            DelegationFactory
                    .create(userUpdateRoleRequestDto.getRole(), this.userPort, this.circlePort, this.circleMemberPort, circleId)
                    .delegate(loginUserId, granteeId);
        }
        /* 권한 위임
        * 1. 권한 위임자가 학생회장이거나 관리자일 경우 이면서 넘겨받을 권한이 동아리장 일때
        * 2. 동아리장 업데이트
        * 3. 기존 동아리장의 동아리장 권한 박탈
        * */

        else if ((grantor.getRole().equals(Role.PRESIDENT) || grantor.getRole().equals(Role.ADMIN))
                && userUpdateRoleRequestDto.getRole().equals(Role.LEADER_CIRCLE)
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

                        circle.getLeader().ifPresent(leader -> {
                            // Check if the leader is the leader of only one circle
                            List<CircleDomainModel> ownCircles = this.circlePort.findByLeaderId(leader.getId());
                            if (ownCircles.size() == 1) {
                                this.userPort.removeRole(leader.getId(), Role.LEADER_CIRCLE);
                            }
                        });

                        this.circlePort.updateLeader(circle.getId(), grantee);
                    }, () -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                "소모임을 찾을 수 없습니다."
                        );
                    });
        }

        else if ((grantor.getRole().equals(Role.PRESIDENT) || grantor.getRole().equals(Role.ADMIN))
                && userUpdateRoleRequestDto.getRole().equals(Role.LEADER_ALUMNI)
        ) {
            UserDomainModel previousLeaderAlumni = this.userPort.findByRole("LEADER_ALUMNI")
                    .stream().findFirst()
                    .orElseThrow(
                            () -> new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    "동문회장이 존재하지 않습니다."
                            ));

            this.userPort.removeRole(previousLeaderAlumni.getId(), Role.LEADER_ALUMNI).orElseThrow(
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
            String loginUserId,
            UserUpdatePasswordRequestDto userUpdatePasswordRequestDto
    ) {
        UserDomainModel user = this.userPort.findById(loginUserId).orElseThrow(
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
                        loginUserId,
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
    public UserResponseDto leave(String loginUserId) {
        UserDomainModel user = this.userPort.findById(loginUserId).orElseThrow(
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

        this.lockerPort.findByUserId(loginUserId)
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
        this.userPort.updateRole(loginUserId, Role.NONE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        );

        // Leave from circle where user joined
        this.circleMemberPort.findByUserId(loginUserId).forEach(
                circleMemberDomainModel ->
                        this.circleMemberPort.updateStatus(circleMemberDomainModel.getId(), CircleMemberStatus.LEAVE)
        );

        return UserResponseDto.from(this.userPort.updateState(loginUserId, UserState.INACTIVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "User id checked, but exception occurred"
                )
        ));
    }

        @Transactional
        public UserResponseDto dropUser(String loginUserId, String userId) {
            UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
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
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
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
    public UserAdmissionResponseDto findAdmissionById(String loginUserId, String admissionId) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
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
            String loginUserId,
            String name,
            Integer pageNum
    ) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
                .validate();

        return this.userAdmissionPort.findAll(UserState.AWAIT, name, pageNum)
                .map(UserAdmissionsResponseDto::from);
    }

    @Transactional
    public UserAdmissionResponseDto createAdmission(UserAdmissionCreateRequestDto userAdmissionCreateRequestDto) {
        UserDomainModel requestUser = this.userPort.findByEmail(userAdmissionCreateRequestDto.getEmail()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "회원가입된 사용자의 이메일이 아닙니다."
                )
        );

        if (this.userAdmissionPort.existsByUserId(requestUser.getId())) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    "이미 신청한 사용자 입니다."
            );
        }

        //TODO
        String attachImage = "나중에 고치기";

        /*userAdmissionCreateRequestDto.getAttachImage()
                .map(image ->
                        this.gcpFileUploader.uploadImageToGcp(image, ImageLocation.USER_ADMISSION))
                .orElse(null);
         */


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
            String loginUserId,
            String admissionId
    ) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
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
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
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
            String loginUserId,
            String admissionId
    ) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
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
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
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
            String loginUserId,
            String boardId
    ) {
        UserDomainModel user = this.userPort.findById(loginUserId).orElseThrow(
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
            String loginUserId,
            String userId
    ) {
        UserDomainModel requestUser = this.userPort.findById(loginUserId).orElseThrow(
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
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
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

    @Transactional
    public UserSignInResponseDto updateToken(String refreshToken) {
        // STEP1 : refreshToken이 유효한지 확인
        jwtTokenProvider.validateToken(refreshToken);

        // STEP2 : refreshToken으로 맵핑된 유저 찾기
        UserDomainModel user = this.userPort.findByRefreshToken(refreshToken).orElseThrow(
            () -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                "로그인된 사용자를 찾을 수 없습니다."
            )
        );

        // STEP3 : 새로운 accessToken 제공
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole(), user.getState());
        String newRefreshToken = jwtTokenProvider.createRefreshToken();
        this.userPort.updateRefreshToken(user.getId(), newRefreshToken);

        return UserSignInResponseDto.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken)
            .build();
    }
}
