package net.causw.application.user;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.board.FavoriteBoard;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.adapter.persistence.user.UserAdmissionLog;
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
import net.causw.application.storage.StorageService;
import net.causw.config.security.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.board.FavoriteBoardDomainModel;
import net.causw.domain.model.enums.*;
import net.causw.domain.model.post.PostDomainModel;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.RedisUtils;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserAdmissionDomainModel;
import net.causw.domain.model.user.UserDomainModel;
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
import net.causw.domain.validation.UserStateIsDropOrIsInActiveValidator;
import net.causw.domain.validation.UserStateIsNotDropAndActiveValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.infrastructure.GoogleMailSender;
import net.causw.infrastructure.PasswordGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserPort userPort;
    private final CirclePort circlePort;
    private final CircleMemberPort circleMemberPort;
    private final JwtTokenProvider jwtTokenProvider;
    private final StorageService storageService;
    private final GoogleMailSender googleMailSender;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    private final UserRepository userRepository;
    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final PostRepository postRepository;
    private final PageableFactory pageableFactory;
    private final CommentRepository commentRepository;
    private final UserAdmissionRepository userAdmissionRepository;
    private final RedisUtils redisUtils;
    private final LockerRepository lockerRepository;
    private final LockerLogRepository lockerLogRepository;
    private final UserAdmissionLogRepository userAdmissionLogRepository;

    @Transactional
    public UserResponseDto findPassword(
            UserFindPasswordRequestDto userFindPasswordRequestDto
    ) {
        User requestUser = userRepository.findByEmailAndNameAndStudentId(userFindPasswordRequestDto.getEmail(), userFindPasswordRequestDto.getName(), userFindPasswordRequestDto.getStudentId())
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));

        String newPassword = this.passwordGenerator.generate();
        this.googleMailSender.sendNewPasswordMail(requestUser.getEmail(), newPassword);

        this.userRepository.findById(requestUser.getId()).map(
                srcUser -> {
                    srcUser.setPassword(passwordEncoder.encode(newPassword));
                    return this.userRepository.save(srcUser);
                }).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.USER_NOT_FOUND));
        return UserResponseDto.from(requestUser);
    }

    // Find process of another user
    @Transactional(readOnly = true)
    public UserResponseDto findByUserId(String targetUserId, String loginUserId) {
        User requestUser = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                ));


        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(),
                        List.of(Role.LEADER_CIRCLE)))
                .validate();

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE")) {
            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(loginUserId);
            if (ownCircles.isEmpty()) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
                );
            }

            boolean isMemberOfAnyCircle = ownCircles.stream()
                    .anyMatch(circleEntity ->
                            this.circleMemberRepository.findByUser_IdAndCircle_Id(targetUserId, circleEntity.getId())
                                    .map(circleMemberEntity -> circleMemberEntity.getStatus() == CircleMemberStatus.MEMBER)
                                    .orElse(false)
                    );

            if (!isMemberOfAnyCircle) {
                throw new BadRequestException(ErrorCode.NOT_MEMBER, MessageUtil.CIRCLE_MEMBER_NOT_FOUND);
            }
        }

        return this.userRepository.findById(targetUserId)
                .map(UserResponseDto::from)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    public UserResponseDto findCurrentUser(String loginUserId) {
        User requestUser = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE")) {
            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(loginUserId);
            if (ownCircles.isEmpty()) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
                );
            }

            return UserResponseDto.of(
                    requestUser,
                    ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
                    ownCircles.stream().map(Circle::getName).collect(Collectors.toList())

            );
        }

        return UserResponseDto.from(requestUser);
    }

    @Transactional(readOnly = true)
    public UserPostsResponseDto findPosts(String loginUserId, Integer pageNum) {
        User requestUser = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        return UserPostsResponseDto.of(
                requestUser,
                this.postRepository.findByUserId(loginUserId, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                        .map(post -> UserPostResponseDto.of(
                                post,
                                post.getBoard().getId(),
                                post.getBoard().getName(),
                                post.getBoard().getCircle() != null ? post.getBoard().getCircle().getId() : null,
                                post.getBoard().getCircle() != null ? post.getBoard().getCircle().getName() : null,
                                this.postRepository.countAllCommentByPost_Id(post.getId())
                        ))
        );
    }

    @Transactional(readOnly = true)
    public UserCommentsResponseDto findComments(String loginUserId, Integer pageNum) {
        User requestUser = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        return UserCommentsResponseDto.of(
                requestUser,
                this.commentRepository.findByUserId(loginUserId, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_COMMENT_PAGE_SIZE))
                .map(comment -> {
                    Post post = this.postRepository.findById(comment.getPost().getId()).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    MessageUtil.POST_NOT_FOUND
                            )
                    );

                    return CommentsOfUserResponseDto.of(
                            comment,
                            post.getBoard().getId(),
                            post.getBoard().getName(),
                            post.getId(),
                            post.getTitle(),
                            post.getBoard().getCircle() != null ? post.getBoard().getCircle().getId() : null,
                            post.getBoard().getCircle() != null ? post.getBoard().getCircle().getName() : null
                    );
                })

        );
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findByName(String loginUserId, String name) {
        User requestUser = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(),
                        List.of(Role.LEADER_CIRCLE
                        )))
                .validate();

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE")) {
            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(loginUserId);
            if (ownCircles.isEmpty()) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
                );
            }

            return this.userRepository.findByName(name)
                    .stream()
                    .filter(user -> user.getState().equals(UserState.ACTIVE))
                    .filter(user ->
                            ownCircles.stream()
                                    .anyMatch(circle ->
                                            this.circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circle.getId())
                                                    .map(circleMemberEntity -> circleMemberEntity.getStatus() == CircleMemberStatus.MEMBER)
                                                    .orElse(false)))
                    .map(user -> UserResponseDto.of(
                            user,
                            ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
                            ownCircles.stream().map(Circle::getName).collect(Collectors.toList())))
                    .collect(Collectors.toList());

        }

        return this.userRepository.findByName(name)
                .stream()
                .filter(user -> user.getState().equals(UserState.ACTIVE))
                .map(UserResponseDto::from)
                .collect(Collectors.toList());

    }

    @Transactional(readOnly = true)
    public UserPrivilegedResponseDto findPrivilegedUsers(String loginUserId) {
        User user = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                ));
        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of()))
                .validate();

        //todo: 현재 겸직을 고려하기 위해 _N_ 사용 중이나 port 와 domain model 삭제를 위해 배제
        //때문에 추후 userRole 관리 리팩토링 후 겸직을 고려하게 변경 필요
        return UserPrivilegedResponseDto.of(
                this.userRepository.findByRoleAndState(Role.PRESIDENT, UserState.ACTIVE)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.VICE_PRESIDENT, UserState.ACTIVE)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.COUNCIL, UserState.ACTIVE)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_1, UserState.ACTIVE)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_2, UserState.ACTIVE)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_3, UserState.ACTIVE)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_4, UserState.ACTIVE)
                        .stream()
                        .map(UserResponseDto::from)
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_CIRCLE, UserState.ACTIVE)
                        .stream()
                        .map(userDomainModel -> {
                            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(loginUserId);
                            if (ownCircles.isEmpty()) {
                                throw new InternalServerException(
                                        ErrorCode.INTERNAL_SERVER,
                                        MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
                                );
                            }
                            return UserResponseDto.of(
                                    userDomainModel,
                                    ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
                                    ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
                            );
                        })
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_ALUMNI, UserState.ACTIVE)
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
        User user = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of()))
                .validate();

        //portimpl 내부 로직 서비스단으로 이동
        Page<User> usersPage;
        if ("INACTIVE_N_DROP".equals(state)) {
            List<String> statesToSearch = Arrays.asList("INACTIVE", "DROP");
            usersPage = userRepository.findByStateInAndNameContaining(
                    statesToSearch,
                    name,
                    PageRequest.of(pageNum, StaticValue.USER_LIST_PAGE_SIZE)
            );
        } else {
            usersPage = userRepository.findByStateAndName(
                    state,
                    name,
                    PageRequest.of(pageNum, StaticValue.USER_LIST_PAGE_SIZE)
            );
        }

        return usersPage.map(userEntity -> {
            if (userEntity.getRole().getValue().contains("LEADER_CIRCLE") && !"INACTIVE_N_DROP".equals(state)) {
                List<Circle> ownCircles = circleRepository.findByLeader_Id(userEntity.getId());
                if (ownCircles.isEmpty()) {
                    throw new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
                    );
                }

                return UserResponseDto.of(
                        userEntity,
                        ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
                        ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
                );
            } else {
                return UserResponseDto.from(userEntity);
            }
        });
    }



    @Transactional(readOnly = true)
    public List<CircleResponseDto> getCircleList(String loginUserId) {
        User user = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .validate();

        if (user.getRole().equals(Role.ADMIN) || user.getRole().getValue().contains("PRESIDENT")) {
            return this.circleRepository.findAllByIsDeletedIsFalse()
                    .stream()
                    .map(CircleResponseDto::from)
                    .collect(Collectors.toList());
        }


        return this.circleMemberRepository.findByUser_Id(user.getId())
                .stream()
                .filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER && !circleMember.getCircle().getIsDeleted())
                .map(circleMember -> CircleResponseDto.from(circleMember.getCircle()))
                .collect(Collectors.toList());
    }

    /**
     * 회원가입 메소드
     *
     * @param userCreateRequestDto
     * @return UserResponseDto
     */
    @Transactional
    public UserResponseDto signUp(UserCreateRequestDto userCreateRequestDto) {
        // Make domain model for generalized data model and validate the format of request parameter

        this.userRepository.findByEmail(userCreateRequestDto.getEmail()).ifPresent(
                email -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.EMAIL_ALREADY_EXIST
                    );
                }
        );

        // Validate password format, admission year range, and whether the email is duplicate or not
        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(user, this.validator))
                .consistOf(PasswordFormatValidator.of(userCreateRequestDto.getPassword()))
                .consistOf(AdmissionYearValidator.of(userCreateRequestDto.getAdmissionYear()))
                .validate();

        return UserResponseDto.from(user);
    }

    @Transactional
    public UserSignInResponseDto signIn(UserSignInRequestDto userSignInRequestDto) {
        User user = userRepository.findByEmail(userSignInRequestDto.getEmail()).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.INVALID_SIGNIN,
                        MessageUtil.EMAIL_INVALID
                )
        );

        /* Validate the input password and user state
         * The sign-in process is rejected if the user is in BLOCKED, WAIT, or INACTIVE state.
         */
        ValidatorBucket.of()
                .consistOf(PasswordCorrectValidator.of(
                        this.passwordEncoder,
                        user.getPassword(),
                        userSignInRequestDto.getPassword()))
                .validate();

        if (user.getState() == UserState.AWAIT) {
            userAdmissionRepository.findByUser_Id(user.getId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.NO_APPLICATION,
                            MessageUtil.NO_APPLICATION
                    )
            );
        }

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .validate();

        // refreshToken은 redis에 보관
        String refreshToken = jwtTokenProvider.createRefreshToken();
        this.userPort.updateRefreshToken(userDomainModel.getId(), refreshToken);

        return UserSignInResponseDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getRole(), user.getState()))
                .refreshToken(jwtTokenProvider.createRefreshToken())
                .build();
    }

    /**
     * 이메일 중복 확인 메소드
     *
     * @param email
     * @return DuplicatedCheckResponseDto
     */
    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedEmail(String email) {
        Optional<User> userFoundByEmail = userRepository.findByEmail(email);
        if (userFoundByEmail.isPresent()) {
            UserState state = userFoundByEmail.get().getState();
            if (state.equals(UserState.INACTIVE) || state.equals(UserState.DROP)) {
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        MessageUtil.USER_ALREADY_APPLY
                );
            }
        }
        return DuplicatedCheckResponseDto.from(userFoundByEmail.isPresent());
    }

    /**
     * 사용자 정보 업데이트 메소드
     *
     * @param loginUserId
     * @param userUpdateRequestDto
     * @return UserResponseDto
     */
    @Transactional
    public UserResponseDto update(String loginUserId, UserUpdateRequestDto userUpdateRequestDto) {
        // First, load the user data from input user id
        User user = this.userRepository.findById(loginUserId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));


        /* The user requested changing the email if the request email is different from the original one
         * Then, validate it whether the requested email is duplicated or not
         */
        if (!user.getEmail().equals(userUpdateRequestDto.getEmail())) {
            userRepository.findByEmail(userUpdateRequestDto.getEmail()).ifPresent(
                    email -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                MessageUtil.EMAIL_ALREADY_EXIST
                        );
                    }
            );
        }

        // Update user entity with requested parameters
        user.setEmail(userUpdateRequestDto.getEmail());
        user.setName(userUpdateRequestDto.getName());
        user.setStudentId(userUpdateRequestDto.getStudentId());
        user.setAdmissionYear(userUpdateRequestDto.getAdmissionYear());
        user.setProfileImage(userUpdateRequestDto.getProfileImage());

        // Validate the admission year range
        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(ConstraintValidator.of(user, this.validator))
                .consistOf(AdmissionYearValidator.of(userUpdateRequestDto.getAdmissionYear()))
                .validate();

        User updatedUser = userRepository.save(user);

        return UserResponseDto.from(updatedUser);
    }

    /**
     * 사용자 권한 업데이트 메소드
     *
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
        User grantor = userRepository.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );
        User grantee = userRepository.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
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
         * 3. DelegationFactory를 통해 권한 위임 진행(동아리장 위임 경우 circle id를 넘겨주어서 어떤 동아리의 동아리장 권한을 위임하는 것인지 확인)
         * */


        if (grantor.getRole().getValue().contains(userUpdateRoleRequestDto.getRole().getValue())){
            String circleId = "";
            if (userUpdateRoleRequestDto.getRole().equals(Role.LEADER_CIRCLE)) {
                circleId = userUpdateRoleRequestDto.getCircleId()
                        .orElseThrow(() -> new BadRequestException(
                                ErrorCode.INVALID_PARAMETER,
                                MessageUtil.CIRCLE_ID_REQUIRED_FOR_LEADER_DELEGATION
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
                && (userUpdateRoleRequestDto.getRole().equals(Role.VICE_PRESIDENT))
        ){
            List<UserDomainModel> previousVicePresident = this.userPort.findByRole("VICE_PRESIDENT");
            if(!previousVicePresident.isEmpty()){
                previousVicePresident.forEach(
                        user -> this.userPort.removeRole(user.getId(), Role.VICE_PRESIDENT).orElseThrow(
                                () -> new InternalServerException(
                                        ErrorCode.INTERNAL_SERVER,
                                        MessageUtil.INTERNAL_SERVER_ERROR
                                ))
                );
            }
        }

        else if ((grantor.getRole().equals(Role.PRESIDENT) || grantor.getRole().equals(Role.ADMIN))
                && userUpdateRoleRequestDto.getRole().equals(Role.LEADER_CIRCLE)
        ) {
            String circleId = userUpdateRoleRequestDto.getCircleId()
                    .orElseThrow(() -> new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            MessageUtil.CIRCLE_ID_REQUIRED_FOR_LEADER_DELEGATION
                    ));
            if(grantee.getRole().equals(Role.VICE_PRESIDENT)){
                throw new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        MessageUtil.CONCURRENT_JOB_IMPOSSIBLE

                );
            }

            this.circleMemberRepository.findByUser_IdAndCircle_Id(granteeId, circleId)
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
                                        MessageUtil.CIRCLE_APPLY_INVALID
                                );
                            });

            this.circleRepository.findById(circleId)
                    .ifPresentOrElse(circle -> {
                        circle.getLeader().ifPresent(leader -> {
                            // Check if the leader is the leader of only one circle
                            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(leader.getId());
                            if (ownCircles.size() == 1) {
                                this.userPort.removeRole(leader.getId(), Role.LEADER_CIRCLE);
                            }
                        });

                        this.circlePort.updateLeader(circle.getId(), grantee);
                    }, () -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.SMALL_CLUB_NOT_FOUND
                        );
                    });

        }
        //관리자가 권한을 삭제하는 경우
        //학생회 권한 삭제일 때는 바로 삭제 가능
        //but 동아리장 겸직일 때 어떤 권한을 삭제하는지 확인이 필요함
        else if ((grantor.getRole().equals(Role.PRESIDENT) || grantor.getRole().equals(Role.ADMIN))
                && userUpdateRoleRequestDto.getRole().equals(Role.COMMON)
        ) {
            //TODO : 로직 수정 필요
            if(grantee.getRole().getValue().contains("COUNCIL") || grantee.getRole().getValue().contains("LEADER_\\d+")){
                return UserResponseDto.from(this.userPort.removeRole(granteeId, Role.COMMON).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.INTERNAL_SERVER_ERROR
                        )
                ));
            }
        }
        else if ((grantor.getRole().equals(Role.PRESIDENT) || grantor.getRole().equals(Role.ADMIN))
                && userUpdateRoleRequestDto.getRole().equals(Role.LEADER_ALUMNI)
        ) {
            User previousLeaderAlumni = this.userRepository.findByRoleContainingAndState(Role.LEADER_ALUMNI.getValue(), UserState.ACTIVE)
                    .stream().findFirst()
                    .orElseThrow(
                            () -> new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    MessageUtil.INTERNAL_SERVER_ERROR
                            ));

            this.userPort.removeRole(previousLeaderAlumni.getId(), Role.LEADER_ALUMNI).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            MessageUtil.INTERNAL_SERVER_ERROR
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
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional
    public UserResponseDto updatePassword(
            String loginUserId,
            UserUpdatePasswordRequestDto userUpdatePasswordRequestDto
    ) {
        // 사용자 정보 조회
        User user = this.userRepository.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
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

        user.setPassword(this.passwordEncoder.encode(userUpdatePasswordRequestDto.getUpdatedPassword()));
        User updatedUser = this.userRepository.save(user);


        return UserResponseDto.from(updatedUser);
    }

    @Transactional
    public UserResponseDto leave(String loginUserId) {
        User user = this.userRepository.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
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
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );

        // Leave from circle where user joined
        this.circleMemberRepository.findByUser_Id(loginUserId)
                .forEach(circleMember ->
                        this.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE)
                );

        return UserResponseDto.from(this.userPort.updateState(loginUserId, UserState.INACTIVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional
    public UserResponseDto dropUser(String loginUserId, String userId) {
        User requestUser = this.userRepository.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );


        User droppedUser = this.userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
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
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );

        return UserResponseDto.from(this.userPort.updateState(userId, UserState.DROP).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional(readOnly = true)
    public UserAdmissionResponseDto findAdmissionById(String loginUserId, String admissionId) {
        User requestUser = this.userRepository.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
                .validate();

        return UserAdmissionResponseDto.from(this.userAdmissionRepository.findById(admissionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_APPLY_NOT_FOUND
                )
        ));
    }

    @Transactional(readOnly = true)
    public Page<UserAdmissionsResponseDto> findAllAdmissions(
            String loginUserId,
            String name,
            Integer pageNum
    ) {
        User requestUser = this.userRepository.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
                .validate();

        return this.userAdmissionRepository.findAllWithName(UserState.AWAIT.getValue(), name, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                .map(UserAdmissionsResponseDto::from);
    }

    @Transactional
    public UserAdmissionResponseDto createAdmission(UserAdmissionCreateRequestDto userAdmissionCreateRequestDto) {
        User requestUser = this.userRepository.findByEmail(userAdmissionCreateRequestDto.getEmail()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );


        if (this.userAdmissionRepository.existsByUser_Id(requestUser.getId())) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    MessageUtil.USER_ALREADY_APPLY
            );
        }

        String attachImage = userAdmissionCreateRequestDto.getAttachImage()
                .map(multipartFile -> {
                    if (multipartFile != null) {
                        return storageService.uploadFile(multipartFile, "USER_ADMISSION");
                    } else {
                        return null; // 업로드 시도하지 않고 null 반환
                    }
                })
                .orElse("https://caucse-s3-bucket-prod.s3.ap-northeast-2.amazonaws.com/basic_profile.png");


        UserAdmission userAdmission = UserAdmission.builder()
                .user(requestUser)
                .attachImage(attachImage)
                .description(userAdmissionCreateRequestDto.getDescription())
                .build();

        ValidatorBucket.of()
                .consistOf(UserStateIsNotDropAndActiveValidator.of(requestUser.getState()))
                .consistOf(ConstraintValidator.of(userAdmission, this.validator))
                .validate();

        return UserAdmissionResponseDto.from(this.userAdmissionRepository.save(userAdmission));
    }

    @Transactional
    public UserAdmissionResponseDto accept(
            String loginUserId,
            String admissionId
    ) {
        User requestUser = this.userRepository.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.LOGIN_USER_NOT_FOUND)
        );

        UserAdmission userAdmission = this.userAdmissionRepository.findById(admissionId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_APPLY_NOT_FOUND)
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
                .validate();

        // Update user role to COMMON
        this.updateRole(userAdmission.getUser().getId(), Role.COMMON).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.ADMISSION_EXCEPTION
                )
        );

        UserAdmissionLog userAdmissionLog = UserAdmissionLog.builder()
                .userEmail(userAdmission.getUser().getEmail())
                .userName(userAdmission.getUser().getName())
                .adminUserEmail(requestUser.getEmail())
                .adminUserName(requestUser.getName())
                .action(UserAdmissionLogAction.ACCEPT)
                .attachImage(userAdmission.getAttachImage())
                .description(userAdmission.getDescription())
                .build();
        // Add admission log

        // Remove the admission
        this.userAdmissionPort.delete(userAdmissionDomainModel);

        return UserAdmissionResponseDto.of(
                userAdmissionDomainModel,
                this.userPort.updateState(userAdmissionDomainModel.getUser().getId(), UserState.ACTIVE).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.ADMISSION_EXCEPTION
                        )
                )
        );
    }

    @Transactional
    public UserAdmissionResponseDto reject(
            String loginUserId,
            String admissionId
    ) {
        User requestUser = this.userRepository.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.LOGIN_USER_NOT_FOUND)
        );

        UserAdmission userAdmission = this.userAdmissionRepository.findById(admissionId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_APPLY_NOT_FOUND)
        );


        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
                .validate();

        UserAdmissionLog userAdmissionLog = UserAdmissionLog.builder()
                .userEmail(userAdmission.getUser().getEmail())
                .userName(userAdmission.getUser().getName())
                .adminUserEmail(requestUser.getEmail())
                .adminUserName(requestUser.getName())
                .action(UserAdmissionLogAction.REJECT)
                .attachImage(userAdmission.getAttachImage())
                .description(userAdmission.getDescription())
                .build();


        this.userAdmissionLogRepository.save(userAdmissionLog);
        this.userAdmissionRepository.delete(userAdmission);

        return UserAdmissionResponseDto.of(
                userAdmission,
                this.updateState(userAdmission.getUser().getId(), UserState.REJECT).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.ADMISSION_EXCEPTION
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
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        BoardDomainModel board = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
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
        User requestUser = this.userRepository.findById(loginUserId).orElseThrow(
            () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.LOGIN_USER_NOT_FOUND)
        );

        User restoredUser = this.userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND)
        );
        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
                .consistOf(UserStateIsDropOrIsInActiveValidator.of(restoredUser.getState()))
                .validate();

        this.userPort.updateRole(restoredUser.getId(), Role.COMMON).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );

        return UserResponseDto.from(this.userPort.updateState(restoredUser.getId(), UserState.ACTIVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional
    public UserSignInResponseDto updateToken(String refreshToken) {
        // STEP1 : refreshToken으로 맵핑된 유저 찾기
        User user = this.userRepository.findById(this.getUserIdFromRefreshToken(refreshToken)).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.INVALID_TOKEN)
        );

        this.userRepository.findById(getUserIdFromRefreshToken(refreshToken));

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(UserStateValidator.of(user.getState()))
                .validate();

        // STEP2 : 새로운 accessToken 제공
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole(), user.getState());
        return UserSignInResponseDto.builder()
                .accessToken(newAccessToken)
                .build();
    }

    public UserSignOutResponseDto signOut(UserSignOutRequestDto userSignOutRequestDto){
        userPort.signOut(userSignOutRequestDto.getRefreshToken(), userSignOutRequestDto.getAccessToken());
        return UserSignOutResponseDto.builder()
                .message("로그아웃 성공")
                .build();
    }
}
