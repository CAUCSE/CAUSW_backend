package net.causw.application;

import net.causw.application.dto.DuplicatedCheckResponseDto;
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
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.Role;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
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
public class CircleService {
    private final CirclePort circlePort;
    private final UserPort userPort;
    private final CircleMemberPort circleMemberPort;
    private final BoardPort boardPort;
    private final PostPort postPort;
    private final CommentPort commentPort;
    private final Validator validator;

    public CircleService(
            CirclePort circlePort,
            UserPort userPort,
            CircleMemberPort circleMemberPort,
            BoardPort boardPort,
            PostPort postPort,
            CommentPort commentPort,
            Validator validator
    ) {
        this.circlePort = circlePort;
        this.userPort = userPort;
        this.circleMemberPort = circleMemberPort;
        this.boardPort = boardPort;
        this.postPort = postPort;
        this.commentPort = commentPort;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public CircleResponseDto findById(String id) {
        CircleDomainModel circle = this.circlePort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "소모임을 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        return CircleResponseDto.from(
                circle,
                this.circleMemberPort.getNumMember(id)
        );
    }

    @Transactional(readOnly = true)
    public List<CirclesResponseDto> findAll(String userId) {
        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
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
                            if (userDomainModel.getRole().equals(Role.ADMIN)) {
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
                        "소모임을 찾을 수 없습니다."
                )
        );

        UserDomainModel userDomainModel = this.userPort.findById(currentUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .validate();

        if (!userDomainModel.getRole().equals(Role.ADMIN)) {
            CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(currentUserId, circleDomainModel.getId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.NOT_MEMBER,
                            "로그인된 사용자가 가입 신청한 소모임이 아닙니다."
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
                        .map(boardDomainModel -> this.postPort.findLatest(boardDomainModel.getId()).map(
                                postDomainModel -> BoardOfCircleResponseDto.from(
                                        boardDomainModel,
                                        userDomainModel.getRole(),
                                        postDomainModel,
                                        this.commentPort.countByPostId(postDomainModel.getId())
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
                        "소모임을 찾을 수 없습니다."
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "소모임을 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.LEADER_CIRCLE, Role.PRESIDENT)))
                .validate();

        return this.circleMemberPort.findByCircleId(circleId, status)
                .stream()
                .map(circleMember -> {
                    UserDomainModel member = this.userPort.findById(circleMember.getUserId()).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    "소모임원을 찾을 수 없습니다."
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        UserDomainModel leader = this.userPort.findById(circleCreateRequestDto.getLeaderId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "등록할 소모임장을 다시 확인해주세요."
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
                            "중복된 소모임 이름입니다."
                    );
                }
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(ConstraintValidator.of(circleDomainModel, this.validator))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.PRESIDENT)))
                .consistOf(GrantableRoleValidator.of(requestUser.getRole(), Role.LEADER_CIRCLE, leader.getRole()))
                .validate();

        // Grant role to the LEADER
        leader = this.userPort.updateRole(circleCreateRequestDto.getLeaderId(), Role.LEADER_CIRCLE).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.INTERNAL_SERVER,
                        "Leader id checked, but exception occurred"
                )
        );

        // Create circle
        CircleDomainModel newCircle = this.circlePort.create(circleDomainModel);

        // Create boards of circle
        BoardDomainModel noticeBoard = BoardDomainModel.of(
                "공지 게시판",
                newCircle.getName() + " 공지 게시판",
                Stream.of(Role.ADMIN, Role.PRESIDENT, Role.LEADER_CIRCLE)
                        .map(Role::getValue)
                        .collect(Collectors.toList()),
                "공지 게시판",
                newCircle
        );
        this.boardPort.create(noticeBoard);

        BoardDomainModel generalBoard = BoardDomainModel.of(
                "자유 게시판",
                newCircle.getName() + " 자유 게시판",
                Stream.of(Role.ADMIN, Role.PRESIDENT, Role.COUNCIL, Role.LEADER_1, Role.LEADER_2, Role.LEADER_3, Role.LEADER_4,
                                Role.LEADER_CIRCLE, Role.LEADER_ALUMNI, Role.COMMON, Role.PROFESSOR)
                        .map(Role::getValue)
                        .collect(Collectors.toList()),
                "자유 게시판",
                newCircle
        );
        this.boardPort.create(generalBoard);

        // Apply the leader automatically to the circle
        CircleMemberDomainModel circleMemberDomainModel = this.circleMemberPort.create(leader, newCircle);
        this.circleMemberPort.updateStatus(circleMemberDomainModel.getId(), CircleMemberStatus.MEMBER).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Circle id immediately can be used, but exception occurred"
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
                        "수정할 소모임을 찾을 수 없습니다."
                )
        );

        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        if (!circle.getName().equals(circleUpdateRequestDto.getName())) {
            this.circlePort.findByName(circleUpdateRequestDto.getName()).ifPresent(
                    name -> {
                        throw new BadRequestException(
                                ErrorCode.ROW_ALREADY_EXIST,
                                "중복된 소모임 이름입니다."
                        );
                    }
            );
        }

        circle.update(
                circleUpdateRequestDto.getName(),
                circleUpdateRequestDto.getMainImage(),
                circleUpdateRequestDto.getDescription()
        );

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(ConstraintValidator.of(circle, this.validator))
                .consistOf(UserRoleValidator.of(
                        user.getRole(),
                        List.of(Role.PRESIDENT, Role.LEADER_CIRCLE)
                ));

        if (user.getRole().equals(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            "This circle has not circle leader"
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
                        "Circle id checked, but exception occurred"
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
                        "삭제할 소모임을 찾을 수 없습니다."
                )
        );

        UserDomainModel user = this.userPort.findById(requestUserId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(
                        user.getRole(),
                        List.of(Role.PRESIDENT, Role.LEADER_CIRCLE)
                ));

        if (user.getRole().equals(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            "This circle has not circle leader"
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

        this.userPort.updateRole(leaderId, Role.COMMON).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Leader id checked, but exception occurred"
                )
        );

        return CircleResponseDto.from(this.circlePort.delete(circleId).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Circle id checked, but exception occurred"
                )
        ));
    }

    @Transactional
    public CircleMemberResponseDto userApply(String userId, String circleId) {
        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "신청할 소모임을 찾을 수 없습니다."
                )
        );

        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
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
                                            "Application id checked, but exception occurred"
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "탈퇴할 소모임을 찾을 수 없습니다."
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(user.getId(), circle.getId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "가입 신청한 소모임이 아닙니다."
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
                                        "This circle has not circle leader"
                                )
                        ),
                        userId))
                .validate();

        return CircleMemberResponseDto.from(user, this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        UserDomainModel user = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "추방할 사용자를 찾을 수 없습니다."
                )
        );

        CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "소모임을 찾을 수 없습니다."
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(userId, circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "추방시킬 사용자가 가입 신청한 소모임이 아닙니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.LEADER_CIRCLE)));

        if (requestUser.getRole().equals(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            "This circle has not circle leader"
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
                                        "This circle has not circle leader"
                                )
                        ),
                        userId))
                .validate();

        return CircleMemberResponseDto.from(user, this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.DROP).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
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
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        CircleMemberDomainModel circleMember = this.circleMemberPort.findById(applicationId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "소모임 가입 신청을 찾을 수 없습니다."
                )
        );

        UserDomainModel user = this.userPort.findById(circleMember.getUserId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "가입 요청한 사용자를 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                .consistOf(UserRoleValidator.of(requestUser.getRole(), List.of(Role.LEADER_CIRCLE)));

        if (requestUser.getRole().equals(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            circleMember.getCircle().getLeader().map(UserDomainModel::getId).orElseThrow(
                                    () -> new InternalServerException(
                                            ErrorCode.INTERNAL_SERVER,
                                            "This circle has not circle leader"
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
                        "Application id checked, but exception occurred"
                )
        ));
    }
}
