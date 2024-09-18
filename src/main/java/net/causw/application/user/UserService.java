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
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.user.*;
import net.causw.application.dto.util.dtoMapper.BoardDtoMapper;
import net.causw.application.dto.util.dtoMapper.CircleDtoMapper;
import net.causw.application.dto.util.dtoMapper.PostDtoMapper;
import net.causw.application.dto.util.dtoMapper.UserDtoMapper;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.config.security.JwtTokenProvider;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.*;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.RedisUtils;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.AdmissionYearValidator;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.GrantableRoleValidator;
import net.causw.domain.validation.PasswordCorrectValidator;
import net.causw.domain.validation.PasswordFormatValidator;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    private final UuidFileService uuidFileService;
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
    private final ChildCommentRepository childCommentRepository;
    private final UserAdmissionRepository userAdmissionRepository;
    private final RedisUtils redisUtils;
    private final LockerRepository lockerRepository;
    private final LockerLogRepository lockerLogRepository;
    private final UserAdmissionLogRepository userAdmissionLogRepository;
    private final BoardRepository boardRepository;
    private final FavoritePostRepository favoritePostRepository;
    private final LikePostRepository likePostRepository;

    @Transactional
    public UserResponseDto findPassword(
            UserFindPasswordRequestDto userFindPasswordRequestDto
    ) {
        User requestUser = userRepository.findByEmailAndNameAndStudentIdAndPhoneNumber(
                    userFindPasswordRequestDto.getEmail(),
                    userFindPasswordRequestDto.getName(),
                    userFindPasswordRequestDto.getStudentId(),
                    userFindPasswordRequestDto.getPhoneNumber()
                ).orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));
        // 임시 비밀번호 생성
        String newPassword = this.passwordGenerator.generate();

        // 메일 전송
        this.googleMailSender.sendNewPasswordMail(requestUser.getEmail(), newPassword);

        // 비밀번호 변경 (db save)
        this.userRepository.findById(requestUser.getId()).map(
                srcUser -> {
                    srcUser.setPassword(passwordEncoder.encode(newPassword));
                    return this.userRepository.save(srcUser);
                }).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.USER_NOT_FOUND));

        return UserDtoMapper.INSTANCE.toUserResponseDto(requestUser, getCircleIdsIfLeader(requestUser), getCircleNamesIfLeader(requestUser));
    }

    // Find process of another user
    @Transactional(readOnly = true)
    public UserResponseDto findByUserId(String targetUserId, User requestUser) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)))
                .validate();

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

        User entity = this.userRepository.findById(targetUserId)
                .orElseThrow(() ->
                        new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.USER_NOT_FOUND
                        ));
        return UserDtoMapper.INSTANCE.toUserResponseDto(entity, null, null);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findCurrentUser(User requestUser) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        if (roles.contains(Role.LEADER_CIRCLE)) {
            List<Circle> ownCircles = this.circleRepository.findByLeader_Id(requestUser.getId());
            if (ownCircles.isEmpty()) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
                );
            }

            return UserDtoMapper.INSTANCE.toUserResponseDto(
                    requestUser,
                    ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
                    ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
            );
        }
        return UserDtoMapper.INSTANCE.toUserResponseDto(requestUser, null, null);
    }

    @Transactional(readOnly = true)
    public UserPostsResponseDto findPosts(User requestUser, Integer pageNum) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        return UserDtoMapper.INSTANCE.toUserPostsResponseDto(
                requestUser,
                this.postRepository.findByUserId(requestUser.getId(), this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE)
                ).map(post -> PostDtoMapper.INSTANCE.toPostsResponseDto(
                        post,
                        getNumOfComment(post),
                        getNumOfPostLikes(post),
                        getNumOfPostFavorites(post)
                ))
        );
    }

    @Transactional(readOnly = true)
    public UserPostsResponseDto findFavoritePosts(User requestUser, Integer pageNum) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        return UserDtoMapper.INSTANCE.toUserPostsResponseDto(
                requestUser,
                this.favoritePostRepository.findByUserId(requestUser.getId(), this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE))
                        .map(favoritePost -> PostDtoMapper.INSTANCE.toPostsResponseDto(
                                favoritePost.getPost(),
                                getNumOfComment(favoritePost.getPost()),
                                getNumOfPostLikes(favoritePost.getPost()),
                                getNumOfPostFavorites(favoritePost.getPost())
                        ))
        );
    }

    @Transactional(readOnly = true)
    public UserPostsResponseDto findCommentedPosts(User requestUser, Integer pageNum) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        Pageable pageable = this.pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE / 2 + 5);

        Page<Post> postsFromComments = this.commentRepository.findPostsByUserId(requestUser.getId(), pageable);
        Page<Post> postsFromChildComments = this.childCommentRepository.findPostsByUserId(requestUser.getId(), pageable);

        //Comment와 ChildComment의 Post 중복 제거
        Set<Post> combinedPosts = new HashSet<>();
        combinedPosts.addAll(postsFromComments.getContent());
        combinedPosts.addAll(postsFromChildComments.getContent());

        //Set에서 Page로 타입 변환 및 정렬
        List<Post> combinedPostsList = new ArrayList<>(combinedPosts);
        combinedPostsList.sort((post1, post2) -> post2.getCreatedAt().compareTo(post1.getCreatedAt()));
        Page<Post> combinedPostsPage = new PageImpl<>(combinedPostsList, pageable, combinedPostsList.size());

        return UserDtoMapper.INSTANCE.toUserPostsResponseDto(
                requestUser,
                combinedPostsPage.map(post -> PostDtoMapper.INSTANCE.toPostsResponseDto(
                        post,
                        getNumOfComment(post),
                        getNumOfPostLikes(post),
                        getNumOfPostFavorites(post)
                ))
        );
    }

    @Transactional(readOnly = true)
    public UserCommentsResponseDto findComments(User requestUser, Integer pageNum) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .validate();

        return UserDtoMapper.INSTANCE.toUserCommentsResponseDto(
                requestUser,
                this.commentRepository.findByUserId(requestUser.getId(), this.pageableFactory.create(pageNum, StaticValue.DEFAULT_COMMENT_PAGE_SIZE))
                        .map(comment -> {
                            Post post = this.postRepository.findById(comment.getPost().getId())
                                    .orElseThrow(() -> new BadRequestException(
                                            ErrorCode.ROW_DOES_NOT_EXIST,
                                            MessageUtil.POST_NOT_FOUND
                                    ));
                            return UserDtoMapper.INSTANCE.toCommentsOfUserResponseDto(
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
    public List<UserResponseDto> findByName(User requestUser, String name) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE
                        )))
                .validate();

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
                    .map(user -> UserDtoMapper.INSTANCE.toUserResponseDto(
                            user,
                            ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
                            ownCircles.stream().map(Circle::getName).collect(Collectors.toList())))
                    .collect(Collectors.toList());
        }

        return this.userRepository.findByName(name)
                .stream()
                .filter(user -> user.getState().equals(UserState.ACTIVE))
                .map(user -> UserDtoMapper.INSTANCE.toUserResponseDto(user, null, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserPrivilegedResponseDto findPrivilegedUsers(User user) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        //todo: 현재 겸직을 고려하기 위해 _N_ 사용 중이나 port 와 domain model 삭제를 위해 배제
        //때문에 추후 userRole 관리 리팩토링 후 겸직을 고려하게 변경 필요
        return UserDtoMapper.INSTANCE.toUserPrivilegedResponseDto(
                this.userRepository.findByRoleAndState(Role.PRESIDENT, UserState.ACTIVE)
                        .stream()
                        .map(president -> UserDtoMapper.INSTANCE.toUserResponseDto(president, null, null))
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.VICE_PRESIDENT, UserState.ACTIVE)
                        .stream()
                        .map(vicePresident -> UserDtoMapper.INSTANCE.toUserResponseDto(vicePresident, null, null))
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.COUNCIL, UserState.ACTIVE)
                        .stream()
                        .map(council -> UserDtoMapper.INSTANCE.toUserResponseDto(council, null, null))
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_1, UserState.ACTIVE)
                        .stream()
                        .map(leader1 -> UserDtoMapper.INSTANCE.toUserResponseDto(leader1, null, null))
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_2, UserState.ACTIVE)
                        .stream()
                        .map(leader2 -> UserDtoMapper.INSTANCE.toUserResponseDto(leader2, null, null))
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_3, UserState.ACTIVE)
                        .stream()
                        .map(leader3 -> UserDtoMapper.INSTANCE.toUserResponseDto(leader3, null, null))
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_4, UserState.ACTIVE)
                        .stream()
                        .map(leader4 -> UserDtoMapper.INSTANCE.toUserResponseDto(leader4, null, null))
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
                            return UserDtoMapper.INSTANCE.toUserResponseDto(
                                    userDomainModel,
                                    ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
                                    ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
                            );
                        })
                        .collect(Collectors.toList()),
                this.userRepository.findByRoleAndState(Role.LEADER_ALUMNI, UserState.ACTIVE)
                        .stream()
                        .map(alumni -> UserDtoMapper.INSTANCE.toUserResponseDto(alumni, null, null))
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDto> findByState(
            User user,
            String state,
            String name,
            Integer pageNum
    ) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
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
            if (userEntity.getRoles().contains(Role.LEADER_CIRCLE) && !"INACTIVE_N_DROP".equals(state)) {
                List<Circle> ownCircles = circleRepository.findByLeader_Id(userEntity.getId());
                if (ownCircles.isEmpty()) {
                    throw new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            MessageUtil.NO_ASSIGNED_CIRCLE_FOR_LEADER
                    );
                }

                return UserDtoMapper.INSTANCE.toUserResponseDto(
                        userEntity,
                        ownCircles.stream().map(Circle::getId).collect(Collectors.toList()),
                        ownCircles.stream().map(Circle::getName).collect(Collectors.toList())
                );
            } else {
                return UserDtoMapper.INSTANCE.toUserResponseDto(userEntity, null, null);
            }
        });
    }



    @Transactional(readOnly = true)
    public List<CircleResponseDto> getCircleList(User user) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .validate();

        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
            return this.circleRepository.findAllByIsDeletedIsFalse()
                    .stream()
                    .map(circle -> {
                        User leader = circle.getLeader()
                                .orElse(null);
                        return CircleDtoMapper.INSTANCE.toCircleResponseDto(circle, leader);
                    })
                    .collect(Collectors.toList());
        }

        return this.circleMemberRepository.findByUser_Id(user.getId())
                .stream()
                .filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER && !circleMember.getCircle().getIsDeleted())
                .map(circleMember -> {
                    User leader = circleMember.getCircle().getLeader()
                            .orElse(null);
                    return CircleDtoMapper.INSTANCE.toCircleResponseDto(circleMember.getCircle(), leader);
                })
                .collect(Collectors.toList());
    }

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

        this.userRepository.findByNickname(userCreateRequestDto.getNickname()).ifPresent(
                nickname -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.NICKNAME_ALREADY_EXIST
                    );
                }
        );

        this.userRepository.findByStudentId(userCreateRequestDto.getStudentId()).ifPresent(
                studentId -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.STUDENT_ID_ALREADY_EXIST
                    );
                }
        );

        this.userRepository.findByPhoneNumber(userCreateRequestDto.getPhoneNumber()).ifPresent(
                phoneNumber -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.PHONE_NUMBER_ALREADY_EXIST
                    );
                }
        );

        User user = User.from(userCreateRequestDto, passwordEncoder.encode(userCreateRequestDto.getPassword()));

        this.userRepository.save(user);

        // Validate password format, admission year range, and whether the email is duplicate or not
        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(user, this.validator))
                .consistOf(PasswordFormatValidator.of(userCreateRequestDto.getPassword()))
                .consistOf(AdmissionYearValidator.of(userCreateRequestDto.getAdmissionYear()))
                .validate();

        return UserDtoMapper.INSTANCE.toUserResponseDto(user, null, null);
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
        redisUtils.setData(refreshToken,user.getId(),StaticValue.JWT_REFRESH_TOKEN_VALID_TIME);

        return UserDtoMapper.INSTANCE.toUserSignInResponseDto(
                jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState()),
                jwtTokenProvider.createRefreshToken()
        );
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
        return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(userFoundByEmail.isPresent());
    }

    /**
     * 닉네임 중복 확인 메소드
     *
     * @param nickname
     * @return DuplicatedCheckResponseDto
     */
    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedNickname(String nickname) {
        Optional<User> userFoundByNickname = userRepository.findByNickname(nickname);
        if (userFoundByNickname.isPresent()) {
            UserState state = userFoundByNickname.get().getState();
            if (state.equals(UserState.INACTIVE) || state.equals(UserState.DROP)) {
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        MessageUtil.USER_ALREADY_APPLY
                );
            }
        }
        return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(userFoundByNickname.isPresent());
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedStudentId(String studentId) {
        Optional<User> userFoundByStudentId = userRepository.findByStudentId(studentId);
        if (userFoundByStudentId.isPresent()) {
            UserState state = userFoundByStudentId.get().getState();
            if (state.equals(UserState.INACTIVE) || state.equals(UserState.DROP)) {
                throw new BadRequestException(
                        ErrorCode.ROW_ALREADY_EXIST,
                        MessageUtil.USER_ALREADY_APPLY
                );
            }
        }
        return UserDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(userFoundByStudentId.isPresent());
    }

    @Transactional
    public UserResponseDto update(User user, UserUpdateRequestDto userUpdateRequestDto, MultipartFile profileImage) {
        Set<Role> roles = user.getRoles();

        // 닉네임이 변경되었을 때 중복 체크 (이메일 중복 체크의 경우 바뀐 기획에서 이메일 변경이 불가능하여 삭제)
        if (user.getNickname() == null || !user.getNickname().equals(userUpdateRequestDto.getNickname())) {
            userRepository.findByNickname(userUpdateRequestDto.getNickname()).ifPresent(
                    nickname -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                MessageUtil.NICKNAME_ALREADY_EXIST
                        );
                    }
            );
        }

        this.userRepository.findByStudentId(userUpdateRequestDto.getStudentId()).ifPresent(
                studentId -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.STUDENT_ID_ALREADY_EXIST
                    );
                }
        );

        this.userRepository.findByPhoneNumber(userUpdateRequestDto.getPhoneNumber()).ifPresent(
                phoneNumber -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.PHONE_NUMBER_ALREADY_EXIST
                    );
                }
        );

        //다른 서비스단과 update 방식 통일하기
        UuidFile profileImageUuidFile = (profileImage == null) ?
                null :
                uuidFileService.updateFile(user.getProfileImageUuidFile(), profileImage, FilePath.USER_PROFILE);

        user.update(userUpdateRequestDto.getNickname(), userUpdateRequestDto.getAcademicStatus(), profileImageUuidFile);

        // Validate the admission year range
        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(ConstraintValidator.of(user, this.validator))
                .consistOf(AdmissionYearValidator.of(userUpdateRequestDto.getAdmissionYear()))
                .validate();

        User updatedUser = userRepository.save(user);

        return UserDtoMapper.INSTANCE.toUserResponseDto(updatedUser, null, null);
    }


    //TODO: 반드시 로직 수정 필요
    @Transactional
    public UserResponseDto updateUserRole(
            User grantor,
            String granteeId,
            UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        // 위임인의 권한을 모두 조회
        Set<Role> roles = grantor.getRoles();

        // 피위임인의 Id로 피위임인 조회
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
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(GrantableRoleValidator.of(
                        roles,
                        userUpdateRoleRequestDto.getRole(),
                        grantee.getRoles()
                ))
                .validate();

        // 학생회장, 동아리장, 관리자만 권한 위임 가능
        if (roles.contains(Role.PRESIDENT) || roles.contains(Role.LEADER_CIRCLE) || roles.contains(Role.ADMIN)) {
            // 위임인이 자신의 권한을 위임하는 경우
            if (roles.contains(userUpdateRoleRequestDto.getRole())) {
                String circleId = "";

                // 학생회장 권한을 위임하는 경우
                if (userUpdateRoleRequestDto.getRole().equals(Role.PRESIDENT)) {
                    updatePresident(grantee);
                }
                // 동아리장 권한을 위임하는 경우
                else if (userUpdateRoleRequestDto.getRole().equals(Role.LEADER_CIRCLE)) {
                    // 피위임인이 학생회장 또는 부학생회장인지 확인 후 circleId 할당
                    circleId = checkAuthAndCircleId(userUpdateRoleRequestDto, grantee);

                    //동아리가 존재하면 본인 동아리가 맞는지 circleid로 circle 조회
                    Circle circle = circleRepository.findByIdAndIsDeletedIsFalse(circleId)
                            .orElseThrow(() -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    MessageUtil.CIRCLE_NOT_FOUND
                            ));

                    // 위임인이 해당 동아리의 동아리장인지 확인
                    circle.getLeader().filter(leader -> leader.getId().equals(grantor.getId()))
                            .orElseThrow(() -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    MessageUtil.NOT_CIRCLE_LEADER
                            ));

                    // 피위임인이 해당 동아리 소속인지 확인
                    this.circleMemberRepository.findByUser_IdAndCircle_Id(granteeId, circleId)
                            .orElseThrow(() -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    MessageUtil.CIRCLE_MEMBER_NOT_FOUND
                            ));

                    updateLeader(circleId, grantee); // 모두 맞다면 피위임인을 해당 동아리의 동아리장으로 위임
                }
                // 위임인의 권한 삭제 및 피위임인에게 권한 위임
                removeRole(grantor, userUpdateRoleRequestDto.getRole());
                updateRole(grantee, userUpdateRoleRequestDto.getRole());
            }
            else { // 타인의 권한을 위임하는 경우
                // 학생회장, 관리자만 타인의 권한 위임 가능
                if (roles.contains(Role.PRESIDENT) || roles.contains(Role.ADMIN)) {
                    // 부학생회장 권한을 위임하는 경우
                    if (userUpdateRoleRequestDto.getRole().equals(Role.VICE_PRESIDENT)) {
                        updateVicePresident();

                    // 동아리장 권한을 위임하는 경우
                    } else if (userUpdateRoleRequestDto.getRole().equals(Role.LEADER_CIRCLE)) {
                        String circleId = checkAuthAndCircleId(userUpdateRoleRequestDto, grantee);

                        // 피위임인이 해당 동아리 소속인지 확인
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

                        // circleId로 동아리 조회
                        this.circleRepository.findById(circleId)
                                .ifPresentOrElse(circle -> {
                                    circle.getLeader().ifPresent(leader -> { // 동아리장이 존재하는지 확인
                                        // 존재하는 경우 기존 동아리장의 동아리장 권한 삭제
                                        User previousLeaderCircle = leader;
                                        updateLeader(circle.getId(), grantee);
                                        removeRole(previousLeaderCircle, Role.LEADER_CIRCLE);
                                    });
                                }, () -> {
                                    throw new BadRequestException(
                                            ErrorCode.ROW_DOES_NOT_EXIST,
                                            MessageUtil.SMALL_CLUB_NOT_FOUND
                                    );
                                });

                    } // 동문회장 권한을 위임하는 경우
                    else if (userUpdateRoleRequestDto.getRole().equals(Role.LEADER_ALUMNI)) {
                        updateLeaderAlumni(grantee);

                    } // 학년대표 또는 학생회 권한을 위임하는 경우
                    else if (userUpdateRoleRequestDto.getRole().equals(Role.LEADER_1) || userUpdateRoleRequestDto.getRole().equals(Role.LEADER_2)
                    || userUpdateRoleRequestDto.getRole().equals(Role.LEADER_3) || userUpdateRoleRequestDto.getRole().equals(Role.LEADER_4)
                    || userUpdateRoleRequestDto.getRole().equals(Role.COUNCIL)) {
                        Role role = userUpdateRoleRequestDto.getRole();
                        updateRole(grantee, role);

                    } // 일반 사용자로 전환하는 경우
                    else if (userUpdateRoleRequestDto.getRole().equals(Role.COMMON)) {
                        grantee.getRoles().clear(); // 피위임인의 권한을 모두 삭제
                        addRole(grantee, Role.COMMON);

                    } // 그 외의 권한은 위임 불가
                    else {
                        throw new UnauthorizedException(
                                ErrorCode.API_NOT_ACCESSIBLE,
                                MessageUtil.API_NOT_ACCESSIBLE
                        );
                    }
                }
            }
            // grantor, grantee의 권한 확인 후 아무 권한이 없는 경우 COMMON 부여
            if (grantor.getRoles().isEmpty()) {
                addRole(grantor, Role.COMMON);
            }
            if (grantee.getRoles().isEmpty()) {
                addRole(grantee, Role.COMMON);
            }
        }
        else {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ACCESSIBLE,
                    MessageUtil.API_NOT_ACCESSIBLE
            );
        }

        return UserDtoMapper.INSTANCE.toUserResponseDto(this.updateRole(grantee, userUpdateRoleRequestDto.getRole()), null, null);
    }

    private String checkAuthAndCircleId(UserUpdateRoleRequestDto userUpdateRoleRequestDto, User grantee) {
        String circleId;
        // 학생회장, 부학생회장은 동아리장 겸직 불가
        if(grantee.getRoles().equals(Role.VICE_PRESIDENT) || grantee.getRoles().equals(Role.PRESIDENT)){
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    MessageUtil.CONCURRENT_JOB_IMPOSSIBLE
            );
        }

        circleId = userUpdateRoleRequestDto.getCircleId()
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.INVALID_PARAMETER,
                        MessageUtil.CIRCLE_ID_REQUIRED_FOR_LEADER_DELEGATION
                ));
        return circleId;
    }

    private void updateLeaderAlumni(User grantee) {
        // 기존 동문회장 조회
        User previousLeaderAlumni = this.userRepository.findByRoleAndState(Role.LEADER_ALUMNI, UserState.ACTIVE)
                .stream().findFirst()
                .orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.INTERNAL_SERVER_ERROR
                        ));

        removeRole(previousLeaderAlumni, Role.LEADER_ALUMNI); // 기존 동문회장의 동문회장 권한 삭제
        updateRole(grantee, Role.LEADER_ALUMNI);
    }

    private void updateVicePresident() {
        // 부학생회장 리스트 조회 후 부학생회장 권한 삭제
        List<User> previousVicePresidents = userRepository.findByRoleAndState(Role.VICE_PRESIDENT, UserState.ACTIVE);
        if (!previousVicePresidents.isEmpty()) {
            previousVicePresidents.forEach(previousVicePresident -> {
                this.removeRole(previousVicePresident, Role.VICE_PRESIDENT);
            });
        }
    }

    private void updatePresident(User grantee) {
        if (!grantee.getRoles().contains(Role.COMMON)) {
            throw new UnauthorizedException(
                    ErrorCode.API_NOT_ALLOWED,
                    MessageUtil.CONCURRENT_JOB_IMPOSSIBLE
            );
        }

        // 학생회 리스트 조회 후 학생회 권한 삭제
        List<User> councilList = this.userRepository.findByRoleAndState(Role.COUNCIL, UserState.ACTIVE);
        if (!councilList.isEmpty()) {
            councilList.forEach(user -> removeRole(user, Role.COUNCIL));
        }

        // 부학생회장 리스트 조회 후 부학생회장 권한 삭제
        List<User> vicePresident = this.userRepository.findByRoleAndState(Role.VICE_PRESIDENT, UserState.ACTIVE);
        if (!vicePresident.isEmpty()) {
            vicePresident.forEach(user -> removeRole(user, Role.VICE_PRESIDENT));
        }
    }

    //remove는 그냥 역할을 지우기만 한다. (역할이 아무것도 없어지면 common을 추가한다)
    private User removeRole(User targetUser, Role targetRole) {

        Set<Role> roles = targetUser.getRoles();
        if(targetRole.equals(Role.LEADER_CIRCLE)){
            List<Circle> ownCircles = circleRepository.findByLeader_Id(targetUser.getId());
            if(ownCircles.isEmpty()) roles.remove(targetRole);
        } else{
            roles.remove(targetRole);
        }

        if (roles.isEmpty()) {
            roles.add(Role.COMMON);
        }

        targetUser.setRoles(roles);

        return this.userRepository.save(targetUser);
    }

    private User addRole(User targetUser, Role targetRole) {
        Set<Role> roles = targetUser.getRoles();
        roles.add(targetRole);
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
            User user,
            UserUpdatePasswordRequestDto userUpdatePasswordRequestDto
    ) {
        Set<Role> roles = user.getRoles();


        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(PasswordCorrectValidator.of(
                        this.passwordEncoder,
                        user.getPassword(),
                        userUpdatePasswordRequestDto.getOriginPassword())
                )
                .consistOf(PasswordFormatValidator.of(userUpdatePasswordRequestDto.getUpdatedPassword()))
                .validate();

        user.setPassword(this.passwordEncoder.encode(userUpdatePasswordRequestDto.getUpdatedPassword()));
        User updatedUser = this.userRepository.save(user);

        return UserDtoMapper.INSTANCE.toUserResponseDto(updatedUser, null, null);
    }

    //유저정보 완전 삭제
    @Transactional
    public UserResponseDto eraseUserData(User requestuser, String userId) {
        Set<Role> roles = requestuser.getRoles();

        //삭제할 유저
        User deleteUser = getUser(userId);

        //관리자 or 대표만 삭제 가능
        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestuser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleWithoutAdminValidator.of(roles, Set.of(Role.ADMIN, Role.PRESIDENT)))
                .validate();

        //사용중인 사물함이 있을경우 사물함 반환처리
        this.lockerRepository.findByUser_Id(deleteUser.getId())
                .ifPresent(locker -> {
                    locker.returnLocker();
                    this.lockerRepository.save(locker);

                    LockerLog lockerLog = LockerLog.of(
                            locker.getLockerNumber(),
                            locker.getLocation().getName(),
                            deleteUser.getEmail(),
                            deleteUser.getName(),
                            LockerLogAction.RETURN,
                            "사용자 탈퇴"
                    );

                    this.lockerLogRepository.save(lockerLog);

                });

        // 가입된 동아리가 있다면 탈퇴
        this.circleMemberRepository.findByUser_Id(deleteUser.getId())
                .forEach(circleMember ->
                        this.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE)
                );

        this.userRepository.delete(deleteUser);

        return UserDtoMapper.INSTANCE.toUserResponseDto(deleteUser, null, null);
    }

    @Transactional
    public UserResponseDto leave(User user) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleWithoutAdminValidator.of(roles, Set.of(Role.COMMON, Role.PROFESSOR)))
                .validate();

        this.lockerRepository.findByUser_Id(user.getId())
                .ifPresent(locker -> {
                    locker.returnLocker();
                    this.lockerRepository.save(locker);

                    LockerLog lockerLog = LockerLog.of(
                            locker.getLockerNumber(),
                            locker.getLocation().getName(),
                            user.getEmail(),
                            user.getName(),
                            LockerLogAction.RETURN,
                            "사용자 탈퇴"
                    );

                    this.lockerLogRepository.save(lockerLog);

                });

        // Change user role to NONE
        this.updateRole(user, Role.NONE);

        // Leave from circle where user joined
        this.circleMemberRepository.findByUser_Id(user.getId())
                .forEach(circleMember ->
                        this.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE)
                );

        User entity = this.updateState(user.getId(), UserState.INACTIVE)
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                ));
        return UserDtoMapper.INSTANCE.toUserResponseDto(entity, null, null);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteUser() {
        LocalDateTime dueDate = LocalDateTime.now().minusYears(5);

        userRepository.findAllByState(UserState.INACTIVE).stream()
                .filter(user -> user.getUpdatedAt().isBefore(dueDate))
                .forEach(user -> {
                    user.delete();
                    userRepository.save(user);
                });
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
    public UserResponseDto dropUser(User requestUser, String userId, String dropReason) {
        Set<Role> roles = requestUser.getRoles();

        User droppedUser = this.userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .consistOf(UserRoleWithoutAdminValidator.of(droppedUser.getRoles(), Set.of(Role.COMMON, Role.PROFESSOR)))
                .validate();

        this.lockerRepository.findByUser_Id(userId)
                .ifPresent(locker -> {
                    locker.returnLocker();
                    this.lockerRepository.save(locker);

                    LockerLog lockerLog = LockerLog.of(
                            locker.getLockerNumber(),
                            locker.getLocation().getName(),
                            droppedUser.getEmail(),
                            droppedUser.getName(),
                            LockerLogAction.RETURN,
                            "사용자 추방"
                    );

                    this.lockerLogRepository.save(lockerLog);
                });

        this.updateRole(droppedUser, Role.NONE);

        User entity = this.updateState(userId, UserState.DROP)
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                ));
        entity.updateRejectionOrDropReason(dropReason);
        this.userRepository.save(requestUser);

        return UserDtoMapper.INSTANCE.toUserResponseDto(entity, null, null);
    }

    @Transactional(readOnly = true)
    public UserAdmissionResponseDto findAdmissionById(User requestUser, String admissionId) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(
                this.userAdmissionRepository.findById(admissionId)
                        .orElseThrow(() -> new BadRequestException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.USER_APPLY_NOT_FOUND
                        ))
        );
    }

    @Transactional(readOnly = true)
    public Page<UserAdmissionsResponseDto> findAllAdmissions(
            User requestUser,
            String name,
            Integer pageNum
    ) {
        Set<Role> roles = requestUser.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        return this.userAdmissionRepository.findAllWithName(UserState.AWAIT.getValue(), name, this.pageableFactory.create(pageNum, StaticValue.DEFAULT_PAGE_SIZE))
                .map(UserDtoMapper.INSTANCE::toUserAdmissionsResponseDto);
    }

    @Transactional
    public UserAdmissionResponseDto createAdmission(UserAdmissionCreateRequestDto userAdmissionCreateRequestDto, List<MultipartFile> userAdmissionAttachImageList) {
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

        if (userAdmissionAttachImageList.isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.INVALID_PARAMETER,
                    MessageUtil.USER_ADMISSION_MUST_HAVE_IMAGE
            );
        }

        List<UuidFile> uuidFileList = uuidFileService.saveFileList(userAdmissionAttachImageList, FilePath.USER_ADMISSION);

        UserAdmission userAdmission = UserAdmission.of(
                requestUser,
                uuidFileList,
                userAdmissionCreateRequestDto.getDescription()
        );

        ValidatorBucket.of()
                .consistOf(UserStateIsNotDropAndActiveValidator.of(requestUser.getState()))
                .consistOf(ConstraintValidator.of(userAdmission, this.validator))
                .validate();

        return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(this.userAdmissionRepository.save(userAdmission));
    }

    @Transactional
    public UserAdmissionResponseDto accept(
            User requestUser,
            String admissionId
    ) {
        Set<Role> roles = requestUser.getRoles();

        UserAdmission userAdmission = this.userAdmissionRepository.findById(admissionId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_APPLY_NOT_FOUND)
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        // Update user role to COMMON
        this.updateRole(userAdmission.getUser(), Role.COMMON);

        UserAdmissionLog userAdmissionLog = UserAdmissionLog.of(
                userAdmission.getUser().getEmail(),
                userAdmission.getUser().getName(),
                requestUser.getEmail(),
                requestUser.getName(),
                UserAdmissionLogAction.ACCEPT,
                userAdmission.getUserAdmissionAttachImageUuidFileList(),
                userAdmission.getDescription(),
                null
        );
        // Add admission log

        // Remove the admission
        this.userAdmissionLogRepository.save(userAdmissionLog);
        this.userAdmissionRepository.delete(userAdmission);

        return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(
                userAdmissionLog,
                this.updateState(userAdmission.getUser().getId(), UserState.ACTIVE)
                        .orElseThrow(() -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.ADMISSION_EXCEPTION
                        ))
        );
    }

    @Transactional
    public UserAdmissionResponseDto reject(
            User requestUser,
            String admissionId,
            String rejectReason
    ) {
        Set<Role> roles = requestUser.getRoles();

        UserAdmission userAdmission = this.userAdmissionRepository.findById(admissionId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_APPLY_NOT_FOUND)
        );


        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .validate();

        UserAdmissionLog userAdmissionLog = UserAdmissionLog.of(
                userAdmission.getUser().getEmail(),
                userAdmission.getUser().getName(),
                requestUser.getEmail(),
                requestUser.getName(),
                UserAdmissionLogAction.REJECT,
                userAdmission.getUserAdmissionAttachImageUuidFileList(),
                userAdmission.getDescription(),
                rejectReason
        );
        // Add admission log


        this.userAdmissionLogRepository.save(userAdmissionLog);
        this.userAdmissionRepository.delete(userAdmission);
        requestUser.updateRejectionOrDropReason(rejectReason);
        this.userRepository.save(requestUser);

        return UserDtoMapper.INSTANCE.toUserAdmissionResponseDto(
                userAdmissionLog,
                this.updateState(userAdmission.getUser().getId(), UserState.REJECT)
                        .orElseThrow(() -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.ADMISSION_EXCEPTION
                        ))
        );
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
            User requestUser,
            String userId
    ) {
        Set<Role> roles = requestUser.getRoles();

        User restoredUser = this.userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND)
        );
        ValidatorBucket.of()
                .consistOf(UserRoleValidator.of(roles, Set.of()))
                .consistOf(UserStateIsDropOrIsInActiveValidator.of(restoredUser.getState()))
                .validate();

        this.updateRole(restoredUser, Role.COMMON);

        User entity = this.updateState(restoredUser.getId(), UserState.ACTIVE)
                .orElseThrow(() -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                ));
        return UserDtoMapper.INSTANCE.toUserResponseDto(entity, null, null);
    }

    @Transactional
    public UserSignInResponseDto updateToken(String refreshToken) {
        // STEP1 : refreshToken으로 맵핑된 유저 찾기
        User user = this.userRepository.findById(this.getUserIdFromRefreshToken(refreshToken)).orElseThrow(
                () -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.INVALID_REFRESH_TOKEN)
        );

        this.userRepository.findById(getUserIdFromRefreshToken(refreshToken));

        ValidatorBucket.of()
                .consistOf(UserRoleIsNoneValidator.of(user.getRoles()))
                .consistOf(UserStateValidator.of(user.getState()))
                .validate();

        // STEP2 : 새로운 accessToken 제공
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRoles(), user.getState());

        return UserDtoMapper.INSTANCE.toUserSignInResponseDto(newAccessToken, refreshToken);
    }

    private String getUserIdFromRefreshToken(String refreshToken) {
        return Optional.ofNullable(redisUtils.getData(refreshToken))
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.INVALID_REFRESH_TOKEN
                        ));
    }

    public UserSignOutResponseDto signOut(UserSignOutRequestDto userSignOutRequestDto){
        redisUtils.addToBlacklist(userSignOutRequestDto.getAccessToken());
        redisUtils.deleteData(userSignOutRequestDto.getRefreshToken());

        return UserDtoMapper.INSTANCE.toUserSignOutResponseDto("로그아웃 성공");
    }

    public UserFindIdResponseDto findUserId(UserFindIdRequestDto userIdFindRequestDto) {
        User user = this.userRepository.findByStudentIdAndNameAndPhoneNumber(
                userIdFindRequestDto.getStudentId(),
                userIdFindRequestDto.getName(),
                userIdFindRequestDto.getPhoneNumber()
        ).orElseThrow(() -> new BadRequestException(
                ErrorCode.ROW_DOES_NOT_EXIST,
                MessageUtil.USER_NOT_FOUND
        ));

        return UserDtoMapper.INSTANCE.toUserfindIdResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findByStudentId(String studentId) {
        List<User> userList = this.userRepository.findByStudentIdAndStateAndAcademicStatus(studentId, UserState.ACTIVE, AcademicStatus.ENROLLED);

        if (userList.isEmpty()) {
            throw new BadRequestException(
                    ErrorCode.ROW_DOES_NOT_EXIST,
                    MessageUtil.USER_NOT_FOUND
            );
        }

        return userList.stream()
                .map(user -> {
                    if (user.getRoles().contains(Role.LEADER_CIRCLE)) {
                        List<String> circleIdIfLeader = getCircleIdsIfLeader(user);
                        List<String> circleNameIfLeader = getCircleNamesIfLeader(user);
                        return UserDtoMapper.INSTANCE.toUserResponseDto(user, circleIdIfLeader, circleNameIfLeader);
                    }
                    else {
                        return UserDtoMapper.INSTANCE.toUserResponseDto(user, null, null);
                    }
                })
                .collect(Collectors.toList());
    }

    private List<String> getCircleNamesIfLeader(User user) {
        List<Circle> circleList = this.circleRepository.findByLeader_Id(user.getId());

        return circleList.stream()
                .map(Circle::getName)
                .collect(Collectors.toList());
    }

    private List<String> getCircleIdsIfLeader(User user) {
        List<Circle> circleList = this.circleRepository.findByLeader_Id(user.getId());

        return circleList.stream()
                .map(Circle::getId)
                .collect(Collectors.toList());
    }

    private BoardResponseDto toBoardResponseDto(Board board, Role userRole) {
        List<String> roles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
        Boolean writable = roles.stream().anyMatch(str -> userRole.getValue().contains(str));
        String circleId = Optional.ofNullable(board.getCircle()).map(Circle::getId).orElse(null);
        String circleName = Optional.ofNullable(board.getCircle()).map(Circle::getName).orElse(null);
        return BoardDtoMapper.INSTANCE.toBoardResponseDto(
                board,
                roles,
                writable,
                circleId,
                circleName
        );
    }

    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
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

    private Long getNumOfComment(Post post) {
        return this.postRepository.countAllCommentByPost_Id(post.getId());
    }

    private Long getNumOfPostLikes(Post post){
        return likePostRepository.countByPostId(post.getId());
    }

    private Long getNumOfPostFavorites(Post post){
        return favoritePostRepository.countByPostIdAndIsDeletedFalse(post.getId());
    }

}
