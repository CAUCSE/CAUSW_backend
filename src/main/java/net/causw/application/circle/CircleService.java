package net.causw.application.circle;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.post.Post;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.circle.*;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.util.dtoMapper.CircleDtoMapper;
import net.causw.application.dto.util.StatusUtil;
import net.causw.application.excel.CircleExcelService;
import net.causw.application.uuidFile.UuidFileService;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.FilePath;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.causw.application.dto.board.BoardOfCircleResponseDto.isWriteable;

@Service
@RequiredArgsConstructor
public class CircleService {
    private final CircleExcelService circleExcelService;
    private final Validator validator;
    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final UuidFileService uuidFileService;

    @Transactional(readOnly = true)
    public CircleResponseDto findById(String circleId) {
        Circle circle = getCircle(circleId);

        initializeValidator(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE).validate();

        return this.toCircleResponseDtoExtended(circle, getCircleNumMember(circleId));
    }

    @Transactional(readOnly = true)
    public List<CirclesResponseDto> findAll(User user) {
        Set<Role> roles = user.getRoles();

        initializeUserValidator(user.getState(), roles).validate();

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
            User user,
            String circleId
    ) {
        Set<Role> roles = user.getRoles();
        Circle circle = getCircle(circleId);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        if (!StatusUtil.isAdminOrPresident(user)) {
            CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circleId).orElseThrow(
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
    public Long getNumMember(String id) {
        return getCircleNumMember(getCircle(id).getId());
    }

    @Transactional(readOnly = true)
    public List<CircleMemberResponseDto> getUserList(
            User user,
            String circleId,
            CircleMemberStatus status
    ) {
        Set<Role> roles = user.getRoles();

        Circle circle = getCircle(circleId);

        User circleLeader = getCircleLeader(circle);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)))
                .consistOf(UserEqualValidator.of(user.getId(), circleLeader.getId()))
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
    public CircleResponseDto create(User requestUser, CircleCreateRequestDto circleCreateRequestDto, MultipartFile mainImage) {
        Set<Role> roles = requestUser.getRoles();

        User leader = userRepository.findById(circleCreateRequestDto.getLeaderId())
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.NEW_CIRCLE_LEADER_NOT_FOUND)
                );

        UuidFile uuidFile = (mainImage.isEmpty()) ?
                null :
                uuidFileService.saveFile(mainImage, FilePath.CIRCLE_PROFILE);

        Circle circle = Circle.of(
                circleCreateRequestDto.getName(),
                uuidFile,
                circleCreateRequestDto.getDescription(),
                false,
                circleCreateRequestDto.getCircleTax(),
                circleCreateRequestDto.getRecruitMembers(),
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
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(ConstraintValidator.of(circle, this.validator))
                .consistOf(UserRoleValidator.of(roles, Set.of()))
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
                false,
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
            User user,
            String circleId,
            CircleUpdateRequestDto circleUpdateRequestDto,
            MultipartFile mainImage
    ) {
        Circle circle = getCircle(circleId);
        Set<Role> roles = user.getRoles();

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

        ValidatorBucket validatorBucket = ValidatorBucket.of();

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(ConstraintValidator.of(circle, this.validator))
                .consistOf(UserRoleValidator.of(
                        roles,
                        Set.of(Role.LEADER_CIRCLE)
                ));

        if (roles.contains(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circle).getId(),
                            user.getId()
                    ));
        }

        validatorBucket
                .validate();

        UuidFile uuidFile = mainImage.isEmpty() ?
                circle.getCircleMainImageUuidFile() :
                uuidFileService.updateFile(circle.getCircleMainImageUuidFile(), mainImage, FilePath.CIRCLE_PROFILE);

        circle.update(
                circleUpdateRequestDto.getName(),
                circleUpdateRequestDto.getDescription(),
                uuidFile,
                circleUpdateRequestDto.getCircleTax(),
                circleUpdateRequestDto.getRecruitMembers()
        );

        return this.toCircleResponseDto(updateCircle(circleId, circle));
    }

    @Transactional
    public CircleResponseDto delete(
            User user,
            String circleId
    ) {
        Circle circle = getCircle(circleId);

        Set<Role> roles = user.getRoles();

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(
                        roles,
                        Set.of(Role.LEADER_CIRCLE)
                ));

        if (roles.contains(Role.LEADER_CIRCLE)) {
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
    public CircleMemberResponseDto userApply(User user, String circleId) {
        Circle circle = getCircle(circleId);
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
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

        return this.toCircleMemberResponseDto(
                circleMember,
                circle,
                user
        );
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedName(String name) {
        return this.toDuplicatedCheckResponseDto(circleRepository.findByName(name).isPresent());
    }

    @Transactional
    public CircleMemberResponseDto leaveUser(User user, String circleId) {
        Set<Role> roles = user.getRoles();

        Circle circle = getCircle(circleId);

        CircleMember circleMember = circleMemberRepository.findByUser_IdAndCircle_Id(user.getId(), circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .consistOf(UserNotEqualValidator.of(
                        getCircleLeader(circle).getId(),
                        user.getId())
                )
                .validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(circleMember.getId(), CircleMemberStatus.LEAVE),
                circle,
                user
        );
    }

    @Transactional
    public CircleMemberResponseDto dropUser(
            User requestUser,
            String userId,
            String circleId
    ) {
        Set<Role> roles = requestUser.getRoles();

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
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)));

        if (roles.contains(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circle).getId(),
                            requestUser.getId()
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
            User requestUser,
            String applicationId,
            CircleMemberStatus targetStatus
    ) {
        Set<Role> roles = requestUser.getRoles();

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
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)));

        if (roles.contains(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            getCircleLeader(circleMember.getCircle()).getId(),
                            requestUser.getId()));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.AWAIT)
                ))
                .validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(applicationId, targetStatus),
                circleMember.getCircle(),
                user
        );
    }

    @Transactional
    public CircleMemberResponseDto restoreUser(User loginUser, String circleId, String targetUserId) {
        Set<Role> roles = loginUser.getRoles();

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
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(roles,
                        Set.of(Role.LEADER_CIRCLE)))
                .consistOf(UserEqualValidator.of(
                        loginUser.getId(),
                        getCircleLeader(circle).getId()
                ));
        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        restoreTargetMember.getStatus(),
                        List.of(CircleMemberStatus.DROP)
                )).validate();

        return this.toCircleMemberResponseDto(
                updateCircleMemberStatus(restoreTargetMember.getId(), CircleMemberStatus.MEMBER),
                circle,
                targetUser
        );
    }

    @Transactional(readOnly = true)
    public void exportCircleMembersToExcel(User user, String circleId, HttpServletResponse response){
        Circle circle = getCircle(circleId);
        String circleName = circle.getName();
        List<CircleMemberResponseDto> awaitingMembers = getUserList(user, circleId, CircleMemberStatus.AWAIT);
        List<CircleMemberResponseDto> activeMembers = getUserList(user, circleId, CircleMemberStatus.MEMBER);

        circleExcelService.generateCircleExcel(response, circleName, awaitingMembers, activeMembers);
    }




    // Entity or Entity Information CRUD - Circle
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
                    srcCircle.update(circle.getName(), circle.getDescription(), circle.getCircleMainImageUuidFile(), circle.getCircleTax(), circle.getRecruitMembers());
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

    // Entity or Entity Information CRUD - CircleMember
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

    private ValidatorBucket initializeUserValidator(UserState state, Set<Role> roles) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(state))
                .consistOf(UserRoleIsNoneValidator.of(roles));
        return validatorBucket;
    }

    // Dto Mapper

    private UserResponseDto toUserResponseDto(User user) {
        return CircleDtoMapper.INSTANCE.toUserResponseDto(user);
    }

    private CircleResponseDto toCircleResponseDto(Circle circle) {
        return CircleDtoMapper.INSTANCE.toCircleResponseDto(circle);
    }

    private CircleResponseDto toCircleResponseDtoExtended(Circle circle, Long numMember) {
        return CircleDtoMapper.INSTANCE.toCircleResponseDtoExtended(circle, numMember);
    }

    private CirclesResponseDto toCirclesResponseDto(Circle circle, Long numMember) {
        return CircleDtoMapper.INSTANCE.toCirclesResponseDto(circle, numMember);
    }

    private CirclesResponseDto toCirclesResponseDtoExtended(Circle circle, Long numMember, LocalDateTime joinedAt) {
        return CircleDtoMapper.INSTANCE.toCirclesResponseDtoExtended(circle, numMember, joinedAt);
    }

    private BoardOfCircleResponseDto toBoardOfCircleResponseDto(Board board, Set<Role> userRoles) {
        return CircleDtoMapper.INSTANCE.toBoardOfCircleResponseDto(
                board,
                isWriteable(board, userRoles)
        );
    }

    private BoardOfCircleResponseDto toBoardOfCircleResponseDtoExtended(Board board, Set<Role> userRoles, Post post, Long numComment) {
        return CircleDtoMapper.INSTANCE.toBoardOfCircleResponseDtoExtended(
                board,
                isWriteable(board, userRoles),
                post,
                numComment
        );
    }

    private CircleBoardsResponseDto toCircleBoardsResponseDto(Circle circle, Long numMember, List<BoardOfCircleResponseDto> boardList) {
        return CircleDtoMapper.INSTANCE.toCircleBoardsResponseDto(this.toCircleResponseDtoExtended(circle, numMember), boardList);
    }

    private CircleMemberResponseDto toCircleMemberResponseDto(CircleMember circleMember, CircleResponseDto circleResponseDto, UserResponseDto userResponseDto) {
        return CircleDtoMapper.INSTANCE.toCircleMemberResponseDto(circleMember, circleResponseDto, userResponseDto);
    }

    private CircleMemberResponseDto toCircleMemberResponseDto(CircleMember circleMember, Circle circle, User user) {
        return this.toCircleMemberResponseDto(
                circleMember,
                this.toCircleResponseDto(circle),
                this.toUserResponseDto(user)
        );
    }

    private DuplicatedCheckResponseDto toDuplicatedCheckResponseDto(Boolean isDuplicated) {
        return CircleDtoMapper.INSTANCE.toDuplicatedCheckResponseDto(isDuplicated);
    }
}
