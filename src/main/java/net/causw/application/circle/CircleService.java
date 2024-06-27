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
import net.causw.application.dto.util.DtoMapper;
import net.causw.application.dto.util.StatusUtil;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CircleService {
    private final Validator validator;
    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public CircleResponseDto findById(String circleId) {
        Circle circle = getCircle(circleId);

        initializeValidator(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE).validate();

        return CircleResponseDto.toCircleResponseDtoExtended(circle, getCircleNumMember(circleId));
    }

    @Transactional(readOnly = true)
    public List<CirclesResponseDto> findAll(String currentUserId) {
        User user = getUser(currentUserId);

        initializeUserValidator(user.getState(), user.getRole()).validate();

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
                        return CirclesResponseDto.toCirclesResponseDtoExtended(
                                circle,
                                getCircleNumMember(circle.getId()),
                                LocalDateTime.now()
                        );
                    } else {
                        if (joinedCircleMap.containsKey(circle.getId())) {
                            return CirclesResponseDto.toCirclesResponseDtoExtended(
                                    circle,
                                    getCircleNumMember(circle.getId()),
                                    joinedCircleMap.get(circle.getId()).getUpdatedAt()
                            );
                        } else {
                            return CirclesResponseDto.toCirclesResponseDto(
                                    circle,
                                    getCircleNumMember(circle.getId())
                            );
                        }
                    }
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CircleBoardsResponseDto findBoards(
            String currentUserId,
            String circleId
    ) {
        User user = getUser(currentUserId);

        Circle circle = getCircle(circleId);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        if (!StatusUtil.isAdminOrPresident(user)) {
            CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(currentUserId, circleId).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.ROW_DOES_NOT_EXIST,
                            MessageUtil.CIRCLE_APPLY_INVALID
                    )
            );

            ValidatorBucket.of()
                    .consistOf(CircleMemberStatusValidator.of(
                            circleMember.getStatus(),
                            List.of(CircleMemberStatus.MEMBER)
                    ))
                    .validate();
        }

        return CircleBoardsResponseDto.toCircleBoardsResponseDto(
                circle,
                getCircleNumMember(circleId),
                boardRepository.findByCircle_IdAndIsDeletedIsFalseOrderByCreatedAtAsc(circleId)
                        .stream()
                        .map(board -> postRepository.findTop1ByBoard_IdAndIsDeletedIsFalseOrderByCreatedAtDesc(board.getId())
                                    .map(post -> BoardOfCircleResponseDto.toBoardOfCircleResponseDtoExtended(
                                            board,
                                            user.getRole(),
                                            post,
                                            postRepository.countAllCommentByPost_Id(post.getId())
                                    )).orElse(
                                        BoardOfCircleResponseDto.toBoardOfCircleResponseDto(
                                                    board,
                                                    user.getRole()
                                            ))
                        ).collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public Long getNumMember(String id) {
        return getCircleNumMember(getCircle(id).getId());
    }

    @Transactional(readOnly = true)
    public List<CircleMemberResponseDto> getUserList(
            String currentUserId,
            String circleId,
            CircleMemberStatus status
    ) {
        User user = getUser(currentUserId);

        Circle circle = getCircle(circleId);

        User circleLeader = getCircleLeader(circle);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(user.getRole(),
                        List.of(Role.LEADER_CIRCLE)))
                .consistOf(UserEqualValidator.of(user.getId(), circleLeader.getId()))
                .validate();

        return circleMemberRepository.findByCircle_Id(circle.getId())
                .stream()
                .filter(circleMember -> circleMember.getStatus().equals(status))
                .map(circleMember -> CircleMemberResponseDto.toCircleMemberResponseDto(
                        circleMember,
                        CircleResponseDto.toCircleResponseDto(circleMember.getCircle()),
                        UserResponseDto.toUserResponseDto(userRepository.findById(circleMember.getUser().getId()).orElseThrow(
                                () -> new BadRequestException(
                                        ErrorCode.ROW_DOES_NOT_EXIST,
                                        MessageUtil.USER_NOT_FOUND
                                )
                        ))
                )).collect(Collectors.toList());
    }

    @Transactional
    public CircleResponseDto create(String userId, CircleCreateRequestDto circleCreateRequestDto) {
        User requestUser = getUser(userId);

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
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(ConstraintValidator.of(circle, this.validator))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
                .consistOf(GrantableRoleValidator.of(requestUser.getRole(), Role.LEADER_CIRCLE, leader.getRole()))
                .validate();

        // Grant role to the LEADER
        leader = updateUserRole(circleCreateRequestDto.getLeaderId(), Role.LEADER_CIRCLE).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );

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

        return CircleResponseDto.toCircleResponseDto(circle);
    }

    @Transactional
    public CircleResponseDto update(
            String userId,
            String circleId,
            CircleUpdateRequestDto circleUpdateRequestDto
    ) {
        Circle circle = getCircle(circleId);

        User user = getUser(userId);

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
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(ConstraintValidator.of(circle, this.validator))
                .consistOf(UserRoleValidator.of(
                        user.getRole(),
                        List.of(Role.LEADER_CIRCLE)
                ));

        if (user.getRole().getValue().contains("LEADER_CIRCLE")) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circle).getId(),
                            userId
                    ));
        }

        validatorBucket
                .validate();

        return CircleResponseDto.toCircleResponseDto(updateCircle(circleId, circle));
    }

    @Transactional
    public CircleResponseDto delete(
            String requestUserId,
            String circleId
    ) {
        Circle circle = getCircle(circleId);

        User user = getUser(requestUserId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(
                        user.getRole(),
                        List.of(Role.LEADER_CIRCLE)
                ));

        if (user.getRole().getValue().contains("LEADER_CIRCLE")) {
            User leader = circle.getLeader().orElse(null);
            if (leader == null) {
                throw new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.CIRCLE_WITHOUT_LEADER
                );
            }
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            leader.getId(),
                            user.getId()
                    ));
        }

        validatorBucket
                .validate();

        // Change leader role to COMMON
        User leader = getCircleLeader(circle);

        List<Circle> ownCircleList = circleRepository.findByLeader_Id(leader.getId());

        if (ownCircleList.size() == 1) {
            removeUserRole(leader.getId(), Role.LEADER_CIRCLE).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            MessageUtil.INTERNAL_SERVER_ERROR
                    )
            );
        }

        CircleResponseDto circleResponseDto = CircleResponseDto.toCircleResponseDto(deleteCircle(circleId).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));

        deleteAllCircleBoard(circleId);

        return circleResponseDto;
    }

    @Transactional
    public CircleMemberResponseDto userApply(String userId, String circleId) {
        Circle circle = getCircle(circleId);

        User user = getUser(userId);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(StudentIdIsNullValidator.of(user.getStudentId()))
                .validate();

        CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circle.getId()).map(
                srcCircleMember -> {
                    ValidatorBucket.of()
                            .consistOf(CircleMemberStatusValidator.of(
                                    srcCircleMember.getStatus(),
                                    List.of(CircleMemberStatus.LEAVE, CircleMemberStatus.REJECT)
                            ))
                            .validate();
                    return updateCircleMemberStatus(srcCircleMember.getId(), CircleMemberStatus.AWAIT);
                }
        ).orElseGet(() -> createCircleMember(user, circle));

        return CircleMemberResponseDto.toCircleMemberResponseDto(
                circleMember,
                CircleResponseDto.toCircleResponseDto(circle),
                UserResponseDto.toUserResponseDto(user)
        );
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedName(String name) {
        return DuplicatedCheckResponseDto.toDuplicatedCheckResponseDto(circleRepository.findByName(name).isPresent());
    }

    @Transactional
    public CircleMemberResponseDto leaveUser(String userId, String circleId) {
        User user = getUser(userId);

        Circle circle = getCircle(circleId);

        CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .consistOf(UserNotEqualValidator.of(
                        getCircleLeader(circle).getId(),
                        userId))
                .validate();

        return CircleMemberResponseDto.toCircleMemberResponseDto(
                updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.LEAVE),
                CircleResponseDto.toCircleResponseDto(circle),
                UserResponseDto.toUserResponseDto(user)
        );
    }

    @Transactional
    public CircleMemberResponseDto dropUser(
            String requestUserId,
            String userId,
            String circleId
    ) {
        User requestUser = getUser(requestUserId);

        User user = getUser(userId);

        Circle circle = getCircle(circleId);

        CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(requestUser.getRole(),
                        List.of(Role.LEADER_CIRCLE)));

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE")) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circle).getId(),
                            requestUserId
                    ));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .consistOf(UserNotEqualValidator.of(
                        getCircleLeader(circle).getId(),
                        userId))
                .validate();

        return CircleMemberResponseDto.toCircleMemberResponseDto(
                updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.DROP),
                CircleResponseDto.toCircleResponseDto(circle),
                UserResponseDto.toUserResponseDto(user)
        );
    }

    @Transactional
    public CircleMemberResponseDto acceptUser(String requestUserId, String applicationId) {
        return this.updateUserApplication(
                requestUserId,
                applicationId,
                CircleMemberStatus.MEMBER
        );
    }

    @Transactional
    public CircleMemberResponseDto rejectUser(String requestUserId, String applicationId) {
        return updateUserApplication(
                requestUserId,
                applicationId,
                CircleMemberStatus.REJECT
        );
    }

    private CircleMemberResponseDto updateUserApplication(
            String requestUserId,
            String applicationId,
            CircleMemberStatus targetStatus
    ) {
        User requestUser = getUser(requestUserId);

        CircleMember circleMember = circleMemberRepository.findById(applicationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_APPLY_NOT_FOUND
                )
        );

        User user = getUser(circleMember.getUser().getId());

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(requestUser.getRole(),
                        List.of(Role.LEADER_CIRCLE)));

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE")) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circleMember.getCircle()).getId(),
                            requestUserId));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.AWAIT)
                ))
                .validate();

        return CircleMemberResponseDto.toCircleMemberResponseDto(
                updateCircleMemberStatus(applicationId, targetStatus),
                CircleResponseDto.toCircleResponseDto(circleMember.getCircle()),
                UserResponseDto.toUserResponseDto(user)
        );
    }

    @Transactional
    public CircleMemberResponseDto restoreUser(String loginUserId, String circleId, String targetUserId) {
        User loginUser = getUser(loginUserId);

        User targetUser = getUser(targetUserId);

        Circle circle = getCircle(circleId);

        CircleMember restoreTargetMember = circleMemberRepository.findByUser_IdAndCircle_Id(targetUserId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(loginUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(loginUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(loginUser.getRole(),
                        List.of(Role.LEADER_CIRCLE)))
                .consistOf(UserEqualValidator.of(
                        loginUserId,
                        getCircleLeader(circle).getId()
                ));
        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        restoreTargetMember.getStatus(),
                        List.of(CircleMemberStatus.DROP)
                )).validate();

        return CircleMemberResponseDto.toCircleMemberResponseDto(
                updateCircleMemberStatus(restoreTargetMember.getId(), CircleMemberStatus.MEMBER),
                CircleResponseDto.toCircleResponseDto(circle),
                UserResponseDto.toUserResponseDto(targetUser)
        );
    }



    // CRUD Entity or Entity Information

    // Circle
    private Circle getCircle(String circleId) {
        return circleRepository.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
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

    // CircleMember
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

    // User
    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
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

    private Optional<User> updateUserRole(String userId, Role newRole) {
        return this.userRepository.findById(userId).map(
                srcUser -> {
                    if(srcUser.getRole().equals(Role.COMMON)){
                        srcUser.setRole(newRole);
                    }
                    else if (newRole.equals(Role.LEADER_CIRCLE)) {
                        if(!srcUser.getRole().getValue().contains("LEADER_CIRCLE")){
                            String combinedRoleValue = srcUser.getRole().getValue() + "_N_" + "LEADER_CIRCLE";
                            Role combinedRole = Role.of(combinedRoleValue.toUpperCase());
                            srcUser.setRole(combinedRole);
                        }
                        else {
                            srcUser.setRole(srcUser.getRole());
                        }
                    }
                    else if(srcUser.getRole().equals(Role.LEADER_CIRCLE)){
                        if(newRole.equals(Role.COMMON)){
                            srcUser.setRole(newRole);
                        } else{
                            String combinedRoleValue = newRole.getValue() + "_N_" + "LEADER_CIRCLE";
                            Role combinedRole = Role.of(combinedRoleValue.toUpperCase());
                            srcUser.setRole(combinedRole);
                        }
                    }
                    else {
                        srcUser.setRole(newRole);
                    }
                    return userRepository.save(srcUser);
                }
        );
    }

    private Optional<User> removeUserRole(String userId, Role targetRole) {
        return userRepository.findById(userId).map(
                srcUser -> {
                    if(srcUser.getRole().equals(targetRole)){
                        srcUser.setRole(Role.COMMON);
                    }
                    else if (srcUser.getRole().getValue().contains(targetRole.getValue())) {
                        if(targetRole.equals(Role.LEADER_CIRCLE)){
                            String updatedRoleValue = srcUser.getRole().getValue().replace(targetRole.getValue(), "").replace("_N_","");
                            srcUser.setRole(Role.of(updatedRoleValue));
                        }
                    }
                    //학생회 겸 동아리장, 학년대표 겸 동아리장의 경우 타깃이 동아리 장만 남기는걸로 변경
                    else if(targetRole.equals(Role.COMMON)){
                        srcUser.setRole(Role.LEADER_CIRCLE);
                    }
                    return userRepository.save(srcUser);
                }
        );
    }

    // Board
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

    // Validator

    private ValidatorBucket initializeValidator(Boolean isDeleted, String staticValue) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(isDeleted, staticValue));
        return validatorBucket;
    }

    private ValidatorBucket initializeUserValidator(UserState state, Role role) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(state))
                .consistOf(UserRoleIsNoneValidator.of(role));
        return validatorBucket;
    }


    private BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Role userRole, Post post, Long numComment) {
        boolean writable = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))).stream().anyMatch(str -> userRole.getValue().contains(str));
        return DtoMapper.INSTANCE.toBoardOfCircleResponseDto(
                board,
                post,
                numComment,
                writable
        );
    }

    private BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Role userRole) {
        boolean writable = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(","))).stream().anyMatch(str -> userRole.getValue().contains(str));
        return DtoMapper.INSTANCE.toBoardOfCircleResponseDto(
                board,
                0L,
                writable
        );
    }
}
