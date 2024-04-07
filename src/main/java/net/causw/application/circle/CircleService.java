package net.causw.application.circle;

import lombok.RequiredArgsConstructor;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.board.BoardOfCircleResponseDto;
import net.causw.application.dto.circle.CircleBoardsResponseDto;
import net.causw.application.dto.circle.CircleCreateRequestDto;
import net.causw.application.dto.circle.CircleMemberResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.circle.CircleUpdateRequestDto;
import net.causw.application.dto.circle.CirclesResponseDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.CommentPort;
import net.causw.application.spi.PostPort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.board.BoardDomainModel;
import net.causw.domain.model.circle.CircleDomainModel;
import net.causw.domain.model.circle.CircleMemberDomainModel;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.model.user.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.GrantableRoleValidator;
import net.causw.domain.validation.StudentIdIsNullValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserNotEqualValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CircleService {
    private final CirclePort circlePort;
    private final UserPort userPort;
    private final CircleMemberPort circleMemberPort;
    private final BoardPort boardPort;
    private final PostPort postPort;
    private final CommentPort commentPort;
    private final Validator validator;

    @Transactional(readOnly = true)
    public CircleResponseDto findById(String circleId) {
        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        return CircleResponseDto.from(
                circle,
                this.circleMemberPort.getNumMember(circleId)
        );
    }

    @Transactional(readOnly = true)
    public List<CirclesResponseDto> findAll(String currentUserId) {
        UserDomainModel userDomainModel = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .validate();

        Map<String, CircleMemberDomainModel> joinedCircleMap = this.circleMemberPort.findCircleByUserId(userDomainModel.getId());

        return this.circlePort.findAll()
                .stream()
                .map(circleDomainModel -> {
                            if (userDomainModel.getRole().equals(Role.ADMIN) || userDomainModel.getRole().getValue().contains("PRESIDENT")) {
                                return CirclesResponseDto.from(
                                        circleDomainModel,
                                        this.circleMemberPort.getNumMember(circleDomainModel.getId()),
                                        LocalDateTime.now()
                                );
                            }

                            return Optional.ofNullable(joinedCircleMap.get(circleDomainModel.getId()))
                                    .map(circleMemberDomainModel -> CirclesResponseDto.from(
                                            circleDomainModel,
                                            this.circleMemberPort.getNumMember(circleDomainModel.getId()),
                                            circleMemberDomainModel.getUpdatedAt()
                                    ))
                                    .orElse(CirclesResponseDto.from(
                                            circleDomainModel,
                                            this.circleMemberPort.getNumMember(circleDomainModel.getId())
                                            ));
                        }
                )
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CircleBoardsResponseDto findBoards(
            String currentUserId,
            String circleId
    ) {
        CircleDomainModel circleDomainModel = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        UserDomainModel userDomainModel = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        if (!(userDomainModel.getRole().equals(Role.ADMIN) || userDomainModel.getRole().getValue().contains("PRESIDENT"))) {
            CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(currentUserId, circleDomainModel.getId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.NOT_MEMBER,
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

        return CircleBoardsResponseDto.from(
                CircleResponseDto.from(
                        circleDomainModel,
                        this.circleMemberPort.getNumMember(circleId)
                ),
                this.boardPort.findByCircleId(circleId)
                        .stream()
                        .map(boardDomainModel -> this.postPort.findLatestPost(boardDomainModel.getId()).map(
                                postDomainModel -> BoardOfCircleResponseDto.from(
                                        boardDomainModel,
                                        userDomainModel.getRole(),
                                        postDomainModel,
                                        this.postPort.countAllComment(postDomainModel.getId())
                                )
                        ).orElse(
                                BoardOfCircleResponseDto.from(
                                        boardDomainModel,
                                        userDomainModel.getRole()
                                )
                        ))
                        .collect(Collectors.toList())
        );
    }

    @Transactional(readOnly = true)
    public Long getNumMember(String id) {
        this.circlePort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        return this.circleMemberPort.getNumMember(id);
    }

    @Transactional(readOnly = true)
    public List<CircleMemberResponseDto> getUserList(
            String currentUserId,
            String circleId,
            CircleMemberStatus status
    ) {
        UserDomainModel user = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        UserDomainModel circleLeader = circle.getLeader().orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_LEADER_NOR_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(user.getRole(),
                        List.of(Role.LEADER_CIRCLE)))
                .consistOf(UserEqualValidator.of(user.getId(), circleLeader.getId()))
                .validate();

        return this.circleMemberPort.findByCircleId(circleId, status)
                .stream()
                .map(circleMember -> {
                    UserDomainModel member = this.userPort.findById(circleMember.getUserId()).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    MessageUtil.CIRCLE_MEMBER_NOT_FOUND
                            )
                    );

                    return CircleMemberResponseDto.from(member, circleMember);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CircleResponseDto create(String userId, CircleCreateRequestDto circleCreateRequestDto) {
        UserDomainModel requestUser = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        UserDomainModel leader = this.userPort.findById(circleCreateRequestDto.getLeaderId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        CircleDomainModel circleDomainModel = CircleDomainModel.of(
                circleCreateRequestDto.getName(),
                circleCreateRequestDto.getMainImage(),
                circleCreateRequestDto.getDescription(),
                leader
        );

        /* Check if the request user is president or admin
         * Then, validate the circle name whether it is duplicated or not
         */
        this.circlePort.findByName(circleDomainModel.getName()).ifPresent(
                name -> {
                    throw new BadRequestException(
                            ErrorCode.ROW_ALREADY_EXIST,
                            MessageUtil.CIRCLE_DUPLICATE_NAME
                    );
                }
        );

        // user Role이 Common이 아니면 아예 안 됨. -> 권한의 중첩이 필요하다. User Role에 대한 새로운 table 생성 어떤지?
        // https://www.inflearn.com/questions/21303/enum%EC%9D%84-list%EB%A1%9C-%EC%96%B4%EB%96%BB%EA%B2%8C-%EB%B0%9B%EB%8A%94%EC%A7%80-%EA%B6%81%EA%B8%88%ED%95%A9%EB%8B%88%EB%8B%A4
        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(ConstraintValidator.of(circleDomainModel, this.validator))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of()))
                .consistOf(GrantableRoleValidator.of(requestUser.getRole(), Role.LEADER_CIRCLE, leader.getRole()))
                .validate();

        // Grant role to the LEADER
        leader = this.userPort.updateRole(circleCreateRequestDto.getLeaderId(), Role.LEADER_CIRCLE).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );

        // Create circle
        CircleDomainModel newCircle = this.circlePort.create(circleDomainModel);

        // Create boards of circle
        BoardDomainModel noticeBoard = BoardDomainModel.of(
                newCircle.getName() + " 공지 게시판",
                newCircle.getName() + " 공지 게시판",
                Stream.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT, Role.LEADER_CIRCLE)
                        .map(Role::getValue)
                        .collect(Collectors.toList()),
                "동아리 공지 게시판",
                newCircle
        );
        this.boardPort.createBoard(noticeBoard);


        // Apply the leader automatically to the circle
        CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.create(leader, newCircle);
        this.circleMemberPort.updateStatus(circleMemberDomainModel.getId(), CircleMemberStatus.MEMBER).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        );

        return CircleResponseDto.from(newCircle);
    }

    @Transactional
    public CircleResponseDto update(
            String userId,
            String circleId,
            CircleUpdateRequestDto circleUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        if (!circle.getName().equals(circleUpdateRequestDto.getName())) {
            this.circlePort.findByName(circleUpdateRequestDto.getName()).ifPresent(
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
                            circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            MessageUtil.CIRCLE_WITHOUT_LEADER
                                    )
                            ),
                            userId
                    ));
        }

        validatorBucket
                .validate();

        return CircleResponseDto.from(this.circlePort.update(circleId, circle).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional
    public CircleResponseDto delete(
            String requestUserId,
            String circleId
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        UserDomainModel user = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(
                        user.getRole(),
                        List.of(Role.LEADER_CIRCLE)
                ));

        if (user.getRole().getValue().contains("LEADER_CIRCLE")) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            MessageUtil.CIRCLE_WITHOUT_LEADER
                                    )
                            ),
                            user.getId()
                    ));
        }

        validatorBucket
                .validate();

        // Change leader role to COMMON
        String leaderId = circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Leader id of this circle is null"
                )
        );

        List<CircleDomainModel> ownCircles = this.circlePort.findByLeaderId(leaderId);
        if (ownCircles.size() == 1) {
            this.userPort.removeRole(leaderId, Role.LEADER_CIRCLE).orElseThrow(
                    () -> new InternalServerException(
                            ErrorCode.INTERNAL_SERVER,
                            MessageUtil.INTERNAL_SERVER_ERROR
                    )
            );
        }
        CircleResponseDto circleResponseDto = CircleResponseDto.from(this.circlePort.delete(circleId).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
        boardPort.deleteAllCircleBoard(circleId);
        return circleResponseDto;
    }

    @Transactional
    public CircleMemberResponseDto userApply(String userId, String circleId) {
        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(StudentIdIsNullValidator.of(user.getStudentId()))
                .validate();

        return CircleMemberResponseDto.from(
                user,
                this.circleMemberPort.findByUserIdAndCircleId(user.getId(), circle.getId())
                        .map(circleMember -> {
                            ValidatorBucket.of()
                                    .consistOf(CircleMemberStatusValidator.of(
                                            circleMember.getStatus(),
                                            List.of(CircleMemberStatus.LEAVE, CircleMemberStatus.REJECT)
                                    ))
                                    .validate();

                            return this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.AWAIT).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            MessageUtil.INTERNAL_SERVER_ERROR
                                    )
                            );
                        })
                        .orElseGet(() -> this.circleMemberPort.create(user, circle))
        );
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckResponseDto isDuplicatedName(String name) {
        return DuplicatedCheckResponseDto.of(this.circlePort.findByName(name).isPresent());
    }

    @Transactional
    public CircleMemberResponseDto leaveUser(String userId, String circleId) {
        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(user.getId(), circle.getId()).orElseThrow(
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
                        circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                () -> new InternalServerException(
                                        ErrorCode.INTERNAL_SERVER,
                                        MessageUtil.CIRCLE_WITHOUT_LEADER
                                )
                        ),
                        userId))
                .validate();

        return CircleMemberResponseDto.from(user, this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional
    public CircleMemberResponseDto dropUser(
            String requestUserId,
            String userId,
            String circleId
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                       MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(requestUser.getRole(),
                        List.of(Role.LEADER_CIRCLE)));

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE")) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            MessageUtil.CIRCLE_WITHOUT_LEADER
                                    )
                            ),
                            requestUserId
                    ));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .consistOf(UserNotEqualValidator.of(
                        circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                () -> new InternalServerException(
                                        ErrorCode.INTERNAL_SERVER,
                                        MessageUtil.CIRCLE_WITHOUT_LEADER
                                )
                        ),
                        userId))
                .validate();

        return CircleMemberResponseDto.from(user, this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.DROP).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
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
        return this.updateUserApplication(
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
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel requestUser = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findById(applicationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_APPLY_NOT_FOUND
                )
        );

        UserDomainModel user = this.userPort.findById(circleMember.getUserId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(requestUser.getRole(),
                        List.of(Role.LEADER_CIRCLE)));

        if (requestUser.getRole().getValue().contains("LEADER_CIRCLE")) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            circleMember.getCircle().getLeader().map(UserDomainModel::getId).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            MessageUtil.CIRCLE_WITHOUT_LEADER
                                    )
                            ),
                            requestUserId));
        }

        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.AWAIT)
                ))
                .validate();

        return CircleMemberResponseDto.from(user, this.circleMemberPort.updateStatus(applicationId, targetStatus).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }

    @Transactional
    public CircleMemberResponseDto restoreUser(String loginUserId, String circleId, String targetUserId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel loginUser = this.userPort.findById(loginUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.LOGIN_USER_NOT_FOUND
                )
        );
        UserDomainModel targetUser = this.userPort.findById(targetUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );

        CircleMemberDomainModel restoreTargetMember = this.circleMemberPort.findByUserIdAndCircleId(targetUserId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.CIRCLE_APPLY_INVALID
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(loginUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(loginUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(loginUser.getRole(),
                        List.of(Role.LEADER_CIRCLE)))
                .consistOf(UserEqualValidator.of(loginUserId,circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                MessageUtil.CIRCLE_WITHOUT_LEADER
                        )
                )));
        validatorBucket
                .consistOf(CircleMemberStatusValidator.of(
                        restoreTargetMember.getStatus(),
                        List.of(CircleMemberStatus.DROP)
                )).validate();

        return CircleMemberResponseDto.from(targetUser, this.circleMemberPort.updateStatus(restoreTargetMember.getId(), CircleMemberStatus.MEMBER).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        MessageUtil.INTERNAL_SERVER_ERROR
                )
        ));
    }
}
