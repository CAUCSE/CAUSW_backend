package net.causw.application.user;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.locker.LockerLog;
import net.causw.adapter.persistence.page.PageableFactory;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.user.UserAdmission;
import net.causw.adapter.persistence.user.UserAdmissionLog;
import net.causw.adapter.persistence.repository.BoardRepository;
import net.causw.adapter.persistence.repository.FavoriteBoardRepository;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.application.delegation.DelegationFactory;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.comment.CommentsOfUserResponseDto;
import net.causw.application.dto.user.*;
import net.causw.application.dto.util.DtoMapper;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.application.storage.StorageService;
import net.causw.application.util.ServiceProxy;
import net.causw.config.security.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.*;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.RedisUtils;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.PasswordCorrectValidator;
import net.causw.domain.validation.PasswordFormatValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.domain.validation.valid.AdminValid;
import net.causw.domain.validation.valid.CircleMemberValid;
import net.causw.domain.validation.valid.UserValid;
import net.causw.infrastructure.GoogleMailSender;
import net.causw.infrastructure.PasswordGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;

import java.util.*;
import java.util.Arrays;
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
    private final ServiceProxy serviceProxy;

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
    private final BoardRepository boardRepository;
    private final FavoriteBoardRepository favoriteBoardRepository;
    private final UserRoleIsNoneValidator userRoleIsNoneValidator;

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
    public UserResponseDto findByUserId(
            String targetUserId,
            @UserValid(UserRoleValidator = true, targetRoleSet = {"LEADER_CIRCLE"}) User requestUser
    ) {
        Set<Role> roles = requestUser.getRoles();

        if (roles.contains(Role.LEADER_CIRCLE)) {
            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(requestUser.getId());
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
    public UserResponseDto findCurrentUser(@UserValid User requestUser) {
        Set<Role> roles = requestUser.getRoles();

        if (roles.contains(Role.LEADER_CIRCLE)) {
            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(requestUser.getId());
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
    public UserPostsResponseDto findPosts(@UserValid User requestUser, Integer pageNum) {
        return UserPostsResponseDto.of(
                requestUser,
                this.postRepository.findByUserId(requestUser.getId(), this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
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
    public UserCommentsResponseDto findComments(@UserValid User requestUser, Integer pageNum) {
        return UserCommentsResponseDto.of(
                requestUser,
                this.commentRepository.findByUserId(requestUser.getId(), this.pageableFactory.create(pageNum, StaticValue.DEFAULT_COMMENT_PAGE_SIZE))
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
    public List<UserResponseDto> findByName(
            @UserValid(UserRoleValidator = true, targetRoleSet = {"LEADER_CIRCLE"}) User requestUser,
            String name
    ) {
        Set<Role> roles = requestUser.getRoles();

//        ValidatorBucket.of()
//                .consistOf(UserRoleValidator.of(
//                        roles,
//                        Set.of(Role.LEADER_CIRCLE)
//                ))
//                .validate();

        if (roles.contains(Role.LEADER_CIRCLE)) {
            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(requestUser.getId());
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
    public UserPrivilegedResponseDto findPrivilegedUsers(
            @UserValid(UserRoleValidator = true, targetRoleSet = {}) User user
    ) {
        Set<Role> roles = user.getRoles();

//        ValidatorBucket.of()
//                .consistOf(UserRoleValidator.of(roles, Set.of()))
//                .validate();

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
                            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(user.getId());
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
            @UserValid(UserRoleValidator = true, targetRoleSet = {}) User user,
            String state,
            String name,
            Integer pageNum
    ) {
        Set<Role> roles = user.getRoles();

//        ValidatorBucket.of()
//                .consistOf(UserRoleValidator.of(roles, Set.of()))
//                .validate();

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
            if (userEntity.getRoles().contains(Role.LEADER_CIRCLE) && !"INACTIVE_N_DROP".equals(state)) {
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
    public List<CircleResponseDto> getCircleList(@UserValid User user) {
        Set<Role> roles = user.getRoles();

        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
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
    public UserResponseDto signUp(@UserValid(AdmissionYearValidator = true) UserCreateRequestDto userCreateRequestDto) {
        // Make domain model for generalized data model and validate the format of request parameter

        this.userRepository.findByEmail(userCreateRequestDto.getEmail()).ifPresent(
                email -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.EMAIL_ALREADY_EXIST
                    );
                }
        );


        //DomainModel 제거과정에서 role과 state가 누락된 것에 대한 해결을 위해 user에 직접 NONE과 AWAIT 설정
        Set<Role> roles = new HashSet<>();
        roles.add(Role.NONE);
        User user = userCreateRequestDto.toEntity(passwordEncoder.encode(userCreateRequestDto.getPassword()), roles, UserState.AWAIT);

        this.userRepository.save(user);

        // Validate password format, admission year range, and whether the email is duplicate or not
        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(user, this.validator))
                .validate();
        new PasswordFormatValidator().validate(userCreateRequestDto.getPassword());

        return UserResponseDto.from(user);
    }

    @Transactional
    public UserSignInResponseDto signIn(UserSignInRequestDto userSignInRequestDto) {
        User user = serviceProxy.getUserByEmailSignIn(userSignInRequestDto.getEmail());

        /* Validate the input password and user state
         * The sign-in process is rejected if the user is in BLOCKED, WAIT, or INACTIVE state.
         */

        new PasswordCorrectValidator(passwordEncoder).validate(user.getPassword(), userSignInRequestDto.getPassword());

        if (user.getState() == UserState.AWAIT) {
            userAdmissionRepository.findByUser_Id(user.getId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.NO_APPLICATION,
                            MessageUtil.NO_APPLICATION
                    )
            );
        }

        // refreshToken은 redis에 보관
        String refreshToken = jwtTokenProvider.createRefreshToken();
        redisUtils.setData(refreshToken,user.getId(),StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);

        return UserSignInResponseDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState()))
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

    @Transactional
    public UserResponseDto update(
            @UserValid User user,
            @UserValid(AdmissionYearValidator = true) UserUpdateRequestDto userUpdateRequestDto
    ) {
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
                .consistOf(ConstraintValidator.of(user, this.validator))
                .validate();

        User updatedUser = userRepository.save(user);

        return UserResponseDto.from(updatedUser);
    }


    //TODO: 반드시 로직 수정 필요
    @Transactional
    public UserResponseDto updateUserRole(
            @UserValid User grantor,
            String granteeId,
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        // Load the user data from input grantor and grantee ids.
        Set<Role> roles = grantor.getRoles();

//        User grantee = userRepository.findById(granteeId).orElseThrow(
//                () -> new BadRequestException(
//                        ErrorCode.ROW_DOES_NOT_EXIST,
//                        MessageUtil.USER_NOT_FOUND
//                )
//        );
        User grantee = serviceProxy.getGrantee(granteeId, roles, userUpdateRoleRequestDto.getRole());

        /* Validate the role
         * 1) Combination of grantor role and the role to be granted must be acceptable
         * 2) Combination of grantor role and the grantee role must be acceptable
         */
//        ValidatorBucket.of()
//                .consistOf(GrantableRoleValidator.of(
//                        roles,
//                        userUpdateRoleRequestDto.getRole(),
//                        grantee.getRoles()
//                ))
//                .validate();

        /* 권한 위임
         * 1. 권한 위임자와 넘겨주는 권한이 같을 경우, 권한을 위임자가 동아리장일 경우 진행
         * 2. 넘겨받을 권한이 동아리장일 경우 넘겨받을 동아리 id 저장
         * 3. DelegationFactory를 통해 권한 위임 진행(동아리장 위임 경우 circle id를 넘겨주어서 어떤 동아리의 동아리장 권한을 위임하는 것인지 확인)
         * */


        if (roles.contains(userUpdateRoleRequestDto.getRole())){
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
                    .delegate(grantor.getId(), granteeId);
        }
        /* 권한 위임
         * 1. 권한 위임자가 학생회장이거나 관리자일 경우 이면서 넘겨받을 권한이 동아리장 일때
         * 2. 동아리장 업데이트
         * 3. 기존 동아리장의 동아리장 권한 박탈
         * */
        else if ((roles.contains(Role.PRESIDENT) || roles.equals(Role.ADMIN))
                && (userUpdateRoleRequestDto.getRole().equals(Role.VICE_PRESIDENT))
        ){
            List<User> previousVicePresidents = userRepository.findByRoleAndState(Role.VICE_PRESIDENT, UserState.ACTIVE);
            if (!previousVicePresidents.isEmpty()) {
                previousVicePresidents.forEach(previousVicePresident -> {
                    this.removeRole(previousVicePresident, Role.VICE_PRESIDENT);
                });
            }
        }

        else if ((roles.contains(Role.PRESIDENT) || roles.contains(Role.ADMIN))
                && userUpdateRoleRequestDto.getRole().equals(Role.LEADER_CIRCLE)
        ) {
            String circleId = userUpdateRoleRequestDto.getCircleId()
                    .orElseThrow(() -> new BadRequestException(
                            ErrorCode.INVALID_PARAMETER,
                            MessageUtil.CIRCLE_ID_REQUIRED_FOR_LEADER_DELEGATION
                    ));
            if(grantee.getRoles().equals(Role.VICE_PRESIDENT)){
                throw new UnauthorizedException(
                        ErrorCode.API_NOT_ALLOWED,
                        MessageUtil.CONCURRENT_JOB_IMPOSSIBLE

                );
            }

            serviceProxy.getCircleMemberUser(granteeId, circleId, List.of(CircleMemberStatus.MEMBER));

            this.circleRepository.findById(circleId)
                    .ifPresentOrElse(circle -> {
                        circle.getLeader().ifPresent(leader -> {
                            // Check if the leader is the leader of only one circle
                            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(leader.getId());
                            if (ownCircles.size() == 1) {
                                this.userPort.removeRole(leader.getId(), Role.LEADER_CIRCLE);
                            }
                        });
                        updateLeader(circle.getId(), grantee);
                    }, () -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.SMALL_CLUB_NOT_FOUND
                        );
                    });

        }
        //관리자가 권한을 삭제하는 경우
        //학생회 권한 삭제일 때는 바로 삭제 가능
        //학생회라면 삭제
        //but 동아리장 겸직일 때 어떤 권한을 삭제하는지 확인이 필요함
        else if ((roles.contains(Role.PRESIDENT) || roles.contains(Role.ADMIN))
                && userUpdateRoleRequestDto.getRole().equals(Role.COMMON)
        ) {
            //TODO : 로직 수정 필요
            if(grantee.getRoles().contains(Role.COUNCIL)){
                return UserResponseDto.from(removeRole(grantee, Role.COMMON));
            }
        }
        else if ((roles.contains(Role.PRESIDENT) || roles.contains(Role.ADMIN))
                && userUpdateRoleRequestDto.getRole().equals(Role.LEADER_ALUMNI)
        ) {
            User previousLeaderAlumni = this.userRepository.findByRoleAndState(Role.LEADER_ALUMNI, UserState.ACTIVE)
                    .stream().findFirst()
                    .orElseThrow(
                            () -> new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    MessageUtil.INTERNAL_SERVER_ERROR
                            ));

            removeRole(previousLeaderAlumni, Role.LEADER_ALUMNI);
        }

        /* Grant the role
         * The linked updating process is performed on previous delegation process
         * Therefore, the updating for the grantee is performed in this process
         */
        return UserResponseDto.from(this.updateRole(grantee, userUpdateRoleRequestDto.getRole()));
    }

    @AdminValid
    public User getGrantee(String granteeId, Set<Role> granterRoles, Role targetRole) {
        return userRepository.findById(granteeId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
    }

    //UserPort의 removeRole 로직 서비스단으로 이동
    //TODO : role 관련 로직 변경후 권한 삭제 코드 변경 필요
    //유저가 존재하지 않는 경우는 위에서 처리하기 때문에 optional 삭제
    private User removeRole(User targetUser, Role targetRole) {

        Set<Role> roles = targetUser.getRoles();

        if(roles.contains(targetRole)){
            roles.remove(targetRole);
            //TODO: 디폴트로 common이라는 역할을 남기는 경우를 생성하고 지우기
            roles.add(Role.COMMON);
        }
        targetUser.setRoles(roles);

        return this.userRepository.save(targetUser);
    }

    private Optional<Circle> updateLeader(String circleId, User newLeader) {
        return this.circleRepository.findById(circleId).map(
                srcCircle -> {
                    srcCircle.setLeader(newLeader);

                    return this.circleRepository.save(srcCircle);
                }
        );
    }

    private User updateRole(User targetUser, Role newRole) {
        Set<Role> roles = targetUser.getRoles();

        //common이 포함되어 있을때는 common을 지우고 새로운 역할 추가
        if(roles.contains(Role.COMMON)){
            roles.remove(Role.COMMON);
        }
        roles.add(newRole);
        return this.userRepository.save(targetUser);
    }



    @Transactional
    public UserResponseDto updatePassword(
            @UserValid User user,
            UserUpdatePasswordRequestDto userUpdatePasswordRequestDto
    ) {
        new PasswordCorrectValidator(passwordEncoder).validate(user.getPassword(), userUpdatePasswordRequestDto.getOriginPassword());
        new PasswordFormatValidator().validate(userUpdatePasswordRequestDto.getUpdatedPassword());

        user.setPassword(this.passwordEncoder.encode(userUpdatePasswordRequestDto.getUpdatedPassword()));
        User updatedUser = this.userRepository.save(user);

        return UserResponseDto.from(updatedUser);
    }

    @Transactional
    public UserResponseDto leave(
            @UserValid(
                    UserRoleWithoutAdminValidator = true
            ) User user
    ) {
        this.lockerRepository.findByUser_Id(user.getId())
                .ifPresent(locker -> {
                    locker.returnLocker();
                    this.lockerRepository.save(locker);

                    LockerLog lockerLog = LockerLog.builder()
                            .lockerNumber(locker.getLockerNumber())
                            .lockerLocationName(locker.getLocation().getName())
                            .userEmail(user.getEmail())
                            .userName(user.getName())
                            .action(LockerLogAction.RETURN)
                            .message("사용자 탈퇴")
                            .build();

                    this.lockerLogRepository.save(lockerLog);

                });

        // Change user role to NONE
        this.updateRole(user, Role.NONE);

        // Leave from circle where user joined
        this.circleMemberRepository.findByUser_Id(user.getId())
                .forEach(circleMember ->
                        this.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE)
                );

        return UserResponseDto.from(this.updateState(user.getId(), UserState.INACTIVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }


    private Optional<CircleMember> updateStatus(String applicationId, CircleMemberStatus targetStatus) {
        return this.circleMemberRepository.findById(applicationId).map(
                circleMember -> {
                    circleMember.setStatus(targetStatus);
                    return this.circleMemberRepository.save(circleMember);
                }
        );
    }

    private Optional<User> updateState(String id, UserState state) {
        return this.userRepository.findById(id).map(
                srcUser -> {
                    srcUser.setState(state);

                    this.userRepository.save(srcUser);
                    return srcUser;
                }
        );
    }


    @Transactional
    public UserResponseDto dropUser(
            @UserValid(
                    UserRoleValidator = true, targetRoleSet = {},
                    UserRoleWithoutAdminValidator = true
            ) User requestUser,
            String userId
    ) {
        Set<Role> roles = requestUser.getRoles();

        User droppedUser = this.userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        this.lockerRepository.findByUser_Id(userId)
                .ifPresent(locker -> {
                    locker.returnLocker();
                    this.lockerRepository.save(locker);

                    LockerLog lockerLog = LockerLog.builder()
                            .lockerNumber(locker.getLockerNumber())
                            .lockerLocationName(locker.getLocation().getName())
                            .userEmail(requestUser.getEmail())
                            .userName(requestUser.getName())
                            .action(LockerLogAction.RETURN)
                            .message("사용자 추방")
                            .build();

                    this.lockerLogRepository.save(lockerLog);
                });

        this.updateRole(droppedUser, Role.NONE);

        return UserResponseDto.from(this.updateState(userId, UserState.DROP).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional(readOnly = true)
    public UserAdmissionResponseDto findAdmissionById(
            @UserValid(UserRoleValidator = true, targetRoleSet = {}) User requestUser,
            String admissionId
    ) {
        Set<Role> roles = requestUser.getRoles();

//        ValidatorBucket.of()
//                .consistOf(UserRoleValidator.of(roles, Set.of()))
//                .validate();

        return UserAdmissionResponseDto.from(this.userAdmissionRepository.findById(admissionId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_APPLY_NOT_FOUND
                )
        ));
    }

    @Transactional(readOnly = true)
    public Page<UserAdmissionsResponseDto> findAllAdmissions(
            @UserValid(UserRoleValidator = true, targetRoleSet = {}) User requestUser,
            String name,
            Integer pageNum
    ) {
        Set<Role> roles = requestUser.getRoles();

//        ValidatorBucket.of()
//                .consistOf(UserRoleValidator.of(roles, Set.of()))
//                .validate();

        return this.userAdmissionRepository.findAllWithName(UserState.AWAIT.getValue(), name, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                .map(UserAdmissionsResponseDto::from);
    }

    @Transactional
    public UserAdmissionResponseDto createAdmission(UserAdmissionCreateRequestDto userAdmissionCreateRequestDto) {
        User requestUser = getUserByEmail(userAdmissionCreateRequestDto.getEmail());

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
                .consistOf(ConstraintValidator.of(userAdmission, this.validator))
                .validate();

        return UserAdmissionResponseDto.from(this.userAdmissionRepository.save(userAdmission));
    }

    @UserValid(
            UserRolesIsNoneValidator = false,
            UserStateValidator = false,
            UserStateIsNotDropAndActiveValidator = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));
    }

    @Transactional
    public UserAdmissionResponseDto accept(
            @UserValid(UserRoleValidator = true, targetRoleSet = {}) User requestUser,
            String admissionId
    ) {
        Set<Role> roles = requestUser.getRoles();

        UserAdmission userAdmission = this.userAdmissionRepository.findById(admissionId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_APPLY_NOT_FOUND)
        );

//        ValidatorBucket.of()
//                .consistOf(UserRoleValidator.of(roles, Set.of()))
//                .validate();

        // Update user role to COMMON
        this.updateRole(userAdmission.getUser(), Role.COMMON);

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
        this.userAdmissionLogRepository.save(userAdmissionLog);
        this.userAdmissionRepository.delete(userAdmission);

        return UserAdmissionResponseDto.of(
                userAdmission,
                this.updateState(userAdmission.getUser().getId(), UserState.ACTIVE).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.ADMISSION_EXCEPTION
                        )
                )
        );
    }

    @Transactional
    public UserAdmissionResponseDto reject(
            @UserValid(UserRoleValidator = true, targetRoleSet = {}) User requestUser,
            String admissionId
    ) {
        Set<Role> roles = requestUser.getRoles();

        UserAdmission userAdmission = this.userAdmissionRepository.findById(admissionId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_APPLY_NOT_FOUND)
        );

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

    //TODO: 현재 사용하지 않는 기능으로 주석처리
    //사용 여부 결정 후 board 수정 후 도입 필요할 것으로 보임
//    @Transactional
//    public BoardResponseDto cre한ateFavoriteBoard(
//            String loginUserId,
//            String boardId
//    ) {
//        User user = this.userRepository.findById(loginUserId).orElseThrow(
//                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.LOGIN_USER_NOT_FOUND)
//        );
//
//        Board board = this.boardRepository.findById(boardId).orElseThrow(
//                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.BOARD_NOT_FOUND)
//        );
//
//        FavoriteBoard favoriteBoard = FavoriteBoard.builder()
//                .user(user)
//                .board(board)
//                .build();
//
//        ValidatorBucket.of()
//                .consistOf(UserStateValidator.of(user.getState()))
//                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
//                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD))
//                .consistOf(ConstraintValidator.of(favoriteBoard, this.validator))
//                .validate();
//
//        return BoardResponseDto.from(this.favoriteBoardRepository.save(favoriteBoard).getBoard(), user.getRole());
//    }
    //사용하지 않는 기능으로 주석처리
//    @Transactional
//    public BoardResponseDto createFavoriteBoard(
//            String loginUserId,
//            String boardId
//    ) {
//        User user = getUser(loginUserId);
//        Board board = getBoard(boardId);
//
//        FavoriteBoard favoriteBoard = FavoriteBoard.of(
//                user,
//                board
//        );
//
//        ValidatorBucket.of()
//                .consistOf(UserStateValidator.of(user.getState()))
//                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
//                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD))
//                .consistOf(ConstraintValidator.of(favoriteBoard, this.validator))
//                .validate();
//
//        return toBoardResponseDto(
//                favoriteBoardRepository.save(favoriteBoard).getBoard(),
//                user.getRole()
//        );
//    }

    @Transactional
    public UserResponseDto restore(
            @UserValid(
                    UserRolesIsNoneValidator = false,
                    UserStateValidator = false,
                    UserRoleValidator = true,
                    targetRoleSet = {},
                    UserStateIsDropOrIsInActiveValidator = true
            ) User requestUser,
            String userId
    ) {
        User restoredUser = this.userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND)
        );
        this.updateRole(restoredUser, Role.COMMON);

        return UserResponseDto.from(this.updateState(restoredUser.getId(), UserState.ACTIVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional
    public UserSignInResponseDto updateToken(String refreshToken) {
        // STEP1 : refreshToken으로 맵핑된 유저 찾기
        User user = getUserById(refreshToken);
        this.userRepository.findById(getUserIdFromRefreshToken(refreshToken));

        // STEP2 : 새로운 accessToken 제공
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState());
        return UserSignInResponseDto.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @UserValid
    public User getUserById(String refreshToken) {
        return userRepository.findById(getUserIdFromRefreshToken(refreshToken)).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.INVALID_TOKEN)
        );
    }

    private String getUserIdFromRefreshToken(String refreshToken) {
        return Optional.ofNullable(redisUtils.getData(refreshToken))
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "RefreshToken 유효성 검증 실패"));
    }

    public UserSignOutResponseDto signOut(UserSignOutRequestDto userSignOutRequestDto){
        redisUtils.addToBlacklist(userSignOutRequestDto.getAccessToken());
        redisUtils.deleteData(userSignOutRequestDto.getRefreshToken());

        return UserSignOutResponseDto.builder()
                .message("로그아웃 성공")
                .build();
    }

    private BoardResponseDto toBoardResponseDto(Board board, Role userRole) {
        List<String> roles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
        Boolean writable = roles.stream().anyMatch(str -> userRole.getValue().contains(str));
        String circleId = Optional.ofNullable(board.getCircle()).map(Circle::getId).orElse(null);
        String circleName = Optional.ofNullable(board.getCircle()).map(Circle::getName).orElse(null);
        return DtoMapper.INSTANCE.toBoardResponseDto(
                board,
                roles,
                writable,
                circleId,
                circleName
        );
    }

    @UserValid(UserRolesIsNoneValidator = false)
    public User getUserByEmailSignIn(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.INVALID_SIGNIN,
                        MessageUtil.EMAIL_INVALID
                )
        );
    }

    @CircleMemberValid(CircleMemberStatusValidator = true)
    public CircleMember getCircleMember(String granteeId, String circleId, List<CircleMemberStatus> list) {
        return circleMemberRepository.findByUser_IdAndCircle_Id(granteeId, circleId).orElseThrow(
                () -> new UnauthorizedException(
                        ErrorCode.NOT_MEMBER,
                        MessageUtil.CIRCLE_APPLY_INVALID)
        );
    }

    private Board getBoard(String boardId) {
        return boardRepository.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
                )
        );
    }
}
