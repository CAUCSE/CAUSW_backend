package net.causw.application.circle;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.circle.*;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.util.CircleServiceDtoMapper;
import net.causw.application.dto.util.StatusUtil;
import net.causw.application.util.ServiceProxy;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.*;
import net.causw.domain.validation.valid.CircleMemberValid;
import net.causw.domain.validation.valid.CircleValid;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.causw.application.dto.board.BoardOfCircleResponseDto.isWriteable;

@Service
@RequiredArgsConstructor
public class CircleService {
    private final Validator validator;
    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final ServiceProxy serviceProxy;

    @Transactional(readOnly = true)
    public CircleResponseDto findById(
            String circleId
    ) {
        Circle circle = getCircle(circleId);
        initializeValidator(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE).validate();

        return this.toCircleResponseDtoExtended(circle, getCircleNumMember(circleId));
    }

    @Transactional(readOnly = true)
    public List<CirclesResponseDto> findAll(@UserValid User user) {
        Map<String, CircleMember> joinedCircleMap = circleMemberRepository.findByUser_Id(user.getId())
                .stream()
                .filter(circleMember -> circleMember.getStatus().equals(CircleMemberStatus.MEMBER))
                .collect(Collectors.toMap(
                        circleMember -> circleMember.getCircle().getId(),
                        circleMember -> circleMember
                ));

        return circleRepository.findAll()
                .stream()
                .map(circle -> {
                    if (StatusUtil.isAdminOrPresident(user)) {
                        return this.toCirclesResponseDtoExtended(
                                circle,
                                getCircleNumMember(circle.getId()),
                                LocalDateTime.now()
                        );
                    } else {
                        if (joinedCircleMap.containsKey(circle.getId())) {
                            return this.toCirclesResponseDtoExtended(
                                    circle,
                                    getCircleNumMember(circle.getId()),
                                    joinedCircleMap.get(circle.getId()).getUpdatedAt()
                            );
                        } else {
                            return this.toCirclesResponseDto(
                                    circle,
                                    getCircleNumMember(circle.getId())
                            );
                        }
                    }
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CircleBoardsResponseDto findBoards(
            @UserValid User user,
            String circleId
    ) {
        Set<Role> roles = user.getRoles();
        Circle circle = getCircle(circleId);

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        if (!StatusUtil.isAdminOrPresident(user)) {
            serviceProxy.getCircleMemberCircleService(user.getId(), circleId, List.of(CircleMemberStatus.MEMBER));
        }
        return this.toCircleBoardsResponseDto(
                circle,
                getCircleNumMember(circleId),
                boardRepository.findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(circleId)
                        .stream()
                        .map(board -> postRepository.findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(board.getId())
                                    .map(post -> this.toBoardOfCircleResponseDtoExtended(
                                            board,
                                            roles,
                                            post,
                                            postRepository.countAllCommentByPost_Id(post.getId())
                                    )).orElse(
                                        this.toBoardOfCircleResponseDto(
                                                    board,
                                                    roles
                                            ))
                        ).collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public Long getNumMember(
            String id
    ) {
        return getCircleNumMember(getCircle(id).getId());
    }

    @Transactional(readOnly = true)
    public List<CircleMemberResponseDto> getUserList(
            @UserValid(UserRoleValidator = true, targetRoleSet = {"LEADER_CIRCLE"}) User user,
            String circleId,
            CircleMemberStatus status
    ) {
        Circle circle = serviceProxy.getCircleWithUserEqualValidator(user, circleId);

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        return circleMemberRepository.findByCircle_Id(circle.getId())
                .stream()
                .filter(circleMember -> circleMember.getStatus().equals(status))
                .map(circleMember -> this.toCircleMemberResponseDto(
                        circleMember,
                        circleMember.getCircle(),
                        userRepository.findById(circleMember.getUser().getId()).orElseThrow(
                                () -> new BadRequestException(
                                        ErrorCode.ROW_DOES_NOT_EXIST,
                                        MessageUtil.USER_NOT_FOUND
                                )
                        )
                )).collect(Collectors.toList());
    }

    @Transactional
    public CircleResponseDto create(
            @UserValid(UserRoleValidator = true) User requestUser,
            CircleCreateRequestDto circleCreateRequestDto
    ) {
        Set<Role> roles = requestUser.getRoles();
        User leader = userRepository.findById(circleCreateRequestDto.getLeaderId())
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.NEW_CIRCLE_LEADER_NOT_FOUND)
                );

        Circle circle = Circle.of(
                circleCreateRequestDto.getName(),
                circleCreateRequestDto.getMainImage(),
                circleCreateRequestDto.getDescription(),
                false,
                leader
        );

        /* Check if the request user is president or admin
         * Then, validate the circle name whether it is duplicated or not
         */
        circleRepository.findByName(circle.getName()).ifPresent(
                name -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.CIRCLE_DUPLICATE_NAME
                    );
                }
        );

        // user Role이 Common이 아니면 아예 안 됨. -> 권한의 중첩이 필요하다. User Role에 대한 새로운 table 생성 어떤지?
        // https://www.inflearn.com/questions/21303/enum%EC%9D%84-list%EB%A1%9C-%EC%96%B4%EB%96%BB%EA%B2%8C-%EB%B0%9B%EB%8A%94%EC%A7%80-%EA%B6%81%EA%B8%88%ED%95%A9%EB%8B%88%EB%8B%A4
        // User Role Table 분리 필요하다고 봅니다...
        ValidatorBucket.of()
                .consistOf(ConstraintValidator.of(circle, this.validator))
                .consistOf(GrantableRoleValidator.of(roles, Role.LEADER_CIRCLE, leader.getRoles()))
                .validate();

        // Grant role to the LEADER
        leader = updateRole(leader, Role.LEADER_CIRCLE);

        // Create circle
        circleRepository.save(circle);

        // Create boards of circle
        Board noticeBoard = Board.of(
                circle.getName() + "공지 게시판",
                circle.getName() + "공지 게시판",
                Stream.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT, Role.LEADER_CIRCLE)
                        .map(Role::getValue)
                        .collect(Collectors.toList()),
                "동아리 공지 게시판",
                circle
        );
        boardRepository.save(noticeBoard);


        // Apply the leader automatically to the circle
        CircleMember circleMember = createCircleMember(leader, circle);

        updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.MEMBER);

        return this.toCircleResponseDto(circle);
    }

    @Transactional
    public CircleResponseDto update(
            @UserValid(UserRoleValidator = true, targetRoleSet = {"LEADER_CIRCLE"}) User user,
            String circleId,
            CircleUpdateRequestDto circleUpdateRequestDto
    ) {
        Circle circle = serviceProxy.getCircleWithValidatorWithRoleCheck(user, circleId);

        if (!circle.getName().equals(circleUpdateRequestDto.getName())) {
            circleRepository.findByName(circleUpdateRequestDto.getName()).ifPresent(
                    name -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                MessageUtil.CIRCLE_DUPLICATE_NAME
                        );
                    }
            );
        }

        String mainImage = circleUpdateRequestDto.getMainImage();
        if (mainImage.isEmpty()) {
            mainImage = circle.getMainImage();
        }
        circle.update(
                circleUpdateRequestDto.getName(),
                mainImage,
                circleUpdateRequestDto.getDescription()
        );

        ValidatorBucket validatorBucket = ValidatorBucket.of();

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(ConstraintValidator.of(circle, this.validator));

        return this.toCircleResponseDto(updateCircle(circleId, circle));
    }

    @Transactional
    public CircleResponseDto delete(
            @UserValid(UserRoleValidator = true, targetRoleSet = {"LEADER_CIRCLE"}) User user,
            String circleId
    ) {
        Circle circle = serviceProxy.getCircleWithValidatorWithRoleCheck(user, circleId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE));

        validatorBucket
                .validate();

        // Change leader role to COMMON
        User leader = getCircleLeader(circle);

        List<Circle> ownCircleList = circleRepository.findByLeader_Id(leader.getId());

        if (ownCircleList.size() == 1) {
            this.removeRole(leader, Role.LEADER_CIRCLE);
        }

        CircleResponseDto circleResponseDto = this.toCircleResponseDto(deleteCircle(circleId).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
        deleteAllCircleBoard(circleId);

        return circleResponseDto;
    }

    @Transactional
    public CircleMemberResponseDto userApply(@UserValid(StudentIsNullValidator = true) User user, String circleId) {
        Circle circle = getCircle(circleId);

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        CircleMember circleMember = serviceProxy.getCircleMemberOrCreate(user, circle, List.of(CircleMemberStatus.LEAVE, CircleMemberStatus.REJECT));
        updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.AWAIT);
        circleMemberRepository.save(circleMember);

        return this.toCircleMemberResponseDto(
                circleMember,
                circle,
                user
        );
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedName(
            String name
    ) {
        return this.toDuplicatedCheckResponseDto(circleRepository.findByName(name).isPresent());
    }

    @Transactional
    public CircleMemberResponseDto leaveUser(@UserValid User user, String circleId) {
        Circle circle = serviceProxy.getCircleWithUserNotEqualValidator(user, circleId);
        CircleMember circleMember = serviceProxy.getCircleMemberCircleService(user.getId(), circleId, List.of(CircleMemberStatus.MEMBER));

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.LEAVE),
                circle,
                user
        );
    }

    @Transactional
    public CircleMemberResponseDto dropUser(
            @UserValid(UserRoleValidator = true, targetRoleSet = {"LEADER_CIRCLE"}) User requestUser,
            String userId,
            String circleId
    ) {
        User user = getUser(userId);
        Circle circle = serviceProxy.getCircleWithValidatorWithRoleCheckAll(requestUser, circleId);
        CircleMember circleMember = serviceProxy.getCircleMemberCircleService(user.getId(), circleId, List.of(CircleMemberStatus.MEMBER));

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE));

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.DROP),
                circle,
                user
        );
    }

    @Transactional
    public CircleMemberResponseDto acceptUser(User requestUser, String applicationId) {
        return this.updateUserApplication(
                requestUser,
                applicationId,
                CircleMemberStatus.MEMBER
        );
    }

    @Transactional
    public CircleMemberResponseDto rejectUser(User requestUser, String applicationId) {
        return updateUserApplication(
                requestUser,
                applicationId,
                CircleMemberStatus.REJECT
        );
    }

    private CircleMemberResponseDto updateUserApplication(
            @UserValid(UserRoleValidator = true, targetRoleSet = {"LEADER_CIRCLE"}) User requestUser,
            String applicationId,
            CircleMemberStatus targetStatus
    ) {
        CircleMember circleMember = serviceProxy.getCircleMemberById(applicationId, List.of(CircleMemberStatus.AWAIT));
        User user = getUser(circleMember.getUser().getId());
        serviceProxy.getCircleWithValidatorWithRoleCheck(requestUser, circleMember.getCircle().getId());

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE));

        validatorBucket
                .validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(applicationId, targetStatus),
                circleMember.getCircle(),
                user
        );
    }

    @Transactional
    public CircleMemberResponseDto restoreUser(
            @UserValid(UserRoleValidator = true, targetRoleSet = {"LEADER_CIRCLE"}) User loginUser,
            String circleId,
            String targetUserId
    ) {
        User targetUser = getUser(targetUserId);
        Circle circle = serviceProxy.getCircleWithUserEqualValidator(loginUser, circleId);
        CircleMember restoreTargetMember = serviceProxy.getCircleMemberCircleService(targetUserId, circleId, List.of(CircleMemberStatus.DROP));

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE));

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(restoreTargetMember.getId(), CircleMemberStatus.MEMBER),
                circle,
                targetUser
        );
    }

    private Circle updateCircle(String id, Circle circle) {
        return circleRepository.findById(id).map(
                srcCircle -> {
                    srcCircle.update(circle.getDescription(), circle.getName(), circle.getMainImage());
                    return circleRepository.save(srcCircle);
                }
        ).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                ));
    }

    private Optional<Circle> deleteCircle(String id) {
        return circleRepository.findById(id).map(
                srcCircle -> {
                    srcCircle.delete();
                    return circleRepository.save(srcCircle);
                }
        );
    }

    private Long getCircleNumMember(String circleId) {
        return circleMemberRepository.getNumMember(circleId);
    }

    // Entity or Entity Information CRUD - CircleMemberValid
    private CircleMember createCircleMember(User user, Circle circle) {
        return circleMemberRepository.save(CircleMember.of(
                CircleMemberStatus.AWAIT,
                circle,
                user
        ));
    }

    private CircleMember updateCircleMemberStatus(String applicationId, CircleMemberStatus targetStatus) {
        return circleMemberRepository.findById(applicationId).map(
                circleMember -> {
                    circleMember.setStatus(targetStatus);
                    return circleMemberRepository.save(circleMember);
                }
        ).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );
    }

    // Entity or Entity Information CRUD - User
    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
    }

    // Get Circle with NO VALIDATION
    public Circle getCircle(String circleId) {
        return circleRepository.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );
    }

    // Get Circle with UserEqualValidator (not role check)
    @CircleValid(UserEqualValidator = true)
    public Circle getCircleUserEqualValidator(User user, String circleId) {
        return circleRepository.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );
    }

    // Get Circle with UserNotEqualValidator (not role check)
    @CircleValid(UserNotEqualValidator = true)
    public Circle getCircleUserNotEqualValidator(User user, String circleId) {
        return circleRepository.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );
    }

    // Get Circle with UserEqualValidator (role check)
    @CircleValid(UserEqualValidator = true)
    public Circle getCircleWithRoleCheck(User user, String circleId) {
        if (user.getRoles().contains(Role.LEADER_CIRCLE)) {
            return circleRepository.findById(circleId).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.ROW_DOES_NOT_EXIST,
                            MessageUtil.SMALL_CLUB_NOT_FOUND
                    )
            );
        }
        return null;
    }

    // Get Circle with UserEqualValidator + UserNotEqualValidator (role check)
    @CircleValid(UserEqualValidator = true, UserNotEqualValidator = true)
    public Circle getCircleWithRoleCheckAll(User user, String circleId) {
        if (user.getRoles().contains(Role.LEADER_CIRCLE)) {
            return circleRepository.findById(circleId).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.ROW_DOES_NOT_EXIST,
                            MessageUtil.SMALL_CLUB_NOT_FOUND
                    )
            );
        }
        return null;
    }

    @CircleMemberValid(CircleMemberStatusValidator = true)
    public CircleMember getCircleMember(String applicationId, List<CircleMemberStatus> list) {
        return circleMemberRepository.findById(applicationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_APPLY_NOT_FOUND
                )
        );
    }

    @CircleMemberValid(CircleMemberStatusValidator = true)
    public CircleMember getCircleMember(String userId, String circleId, List<CircleMemberStatus> list) {
        return circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );
    }

    @CircleMemberValid(CircleMemberStatusValidator = true)
    public CircleMember getCircleMemberOrCreate(User user, Circle circle, List<CircleMemberStatus> list) {
        return circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circle.getId())
                .orElseGet(() -> createCircleMember(user, circle));
    }


    private User getCircleLeader(Circle circle) {
        User leader = circle.getLeader().orElse(null);

        if (leader == null) {
            throw new InternalServerException(
                    ErrorCode.INTERNAL_SERVER,
                    MessageUtil.CIRCLE_WITHOUT_LEADER
            );
        }

        return userRepository.findById(leader.getId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
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

    // Entity or Entity Information CRUD - Board
    private List<Board> deleteAllCircleBoard(String circleId) {
        List<Board> boardList = boardRepository.findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(circleId);
        for (Board board : boardList) {
            boardRepository.findById(board.getId()).map(
                    srcBoard -> {
                        srcBoard.setIsDeleted(true);

                        return boardRepository.save(srcBoard);
                    }).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            MessageUtil.INTERNAL_SERVER_ERROR
                    ));
        }
        return boardList;
    }

    // ValidatorBucket Constructor

    private ValidatorBucket initializeValidator(Boolean isDeleted, String staticValue) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(isDeleted, staticValue));
        return validatorBucket;
    }

    // Dto Mapper

    private UserResponseDto toUserResponseDto(User user) {
        return CircleServiceDtoMapper.INSTANCE.toUserResponseDto(user);
    }

    private CircleResponseDto toCircleResponseDto(Circle circle) {
        return CircleServiceDtoMapper.INSTANCE.toCircleResponseDto(circle);
    }

    private CircleResponseDto toCircleResponseDtoExtended(Circle circle, Long numMember) {
        return CircleServiceDtoMapper.INSTANCE.toCircleResponseDtoExtended(circle, numMember);
    }

    private CirclesResponseDto toCirclesResponseDto(Circle circle, Long numMember) {
        return CircleServiceDtoMapper.INSTANCE.toCirclesResponseDto(circle, numMember);
    }

    private CirclesResponseDto toCirclesResponseDtoExtended(Circle circle, Long numMember, LocalDateTime joinedAt) {
        return CircleServiceDtoMapper.INSTANCE.toCirclesResponseDtoExtended(circle, numMember, joinedAt);
    }

    private BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Set<Role> userRoles) {
        return CircleServiceDtoMapper.INSTANCE.toBoardOfCircleResponseDto(
                board,
                isWriteable(board, userRoles)
        );
    }

    private BoardOfCircleResponseDto toBoardOfCircleResponseDtoExtended(Board board, Set<Role> userRoles, Post post, Long numComment) {
        return CircleServiceDtoMapper.INSTANCE.toBoardOfCircleResponseDtoExtended(
                board,
                isWriteable(board, userRoles),
                post,
                numComment
        );
    }

    private CircleBoardsResponseDto toCircleBoardsResponseDto(Circle circle, Long numMember, List<BoardOfCircleResponseDto> boardList) {
        return CircleServiceDtoMapper.INSTANCE.toCircleBoardsResponseDto(this.toCircleResponseDtoExtended(circle, numMember), boardList);
    }

    private CircleMemberResponseDto toCircleMemberResponseDto(CircleMember circleMember, CircleResponseDto circleResponseDto, UserResponseDto userResponseDto) {
        return CircleServiceDtoMapper.INSTANCE.toCircleMemberResponseDto(circleMember, circleResponseDto, userResponseDto);
    }

    private CircleMemberResponseDto toCircleMemberResponseDto(CircleMember circleMember, Circle circle, User user) {
        return this.toCircleMemberResponseDto(
                circleMember,
                this.toCircleResponseDto(circle),
                this.toUserResponseDto(user)
        );
    }

    private DuplicatedCheckResponseDto toDuplicatedCheckResponseDto(Boolean isDuplicated) {
        return CircleServiceDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(isDuplicated);
    }
}
