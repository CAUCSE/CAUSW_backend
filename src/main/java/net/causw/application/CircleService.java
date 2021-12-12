package net.causw.application;

import net.causw.application.dto.CircleAllResponseDto;
import net.causw.application.dto.CircleCreateRequestDto;
import net.causw.application.dto.CircleMemberResponseDto;
import net.causw.application.dto.CircleResponseDto;
import net.causw.application.dto.CircleUpdateRequestDto;
import net.causw.application.dto.DuplicatedCheckDto;
import net.causw.application.spi.CircleMemberPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.CircleMemberDomainModel;
import net.causw.domain.model.CircleMemberStatus;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.GrantableRoleValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.StudentIdIsNullValidator;
import net.causw.domain.validation.TimePassedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserNotEqualValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CircleService {
    private final CirclePort circlePort;
    private final UserPort userPort;
    private final CircleMemberPort circleMemberPort;
    private final Validator validator;

    public CircleService(
            CirclePort circlePort,
            UserPort userPort,
            CircleMemberPort circleMemberPort,
            Validator validator
    ) {
        this.circlePort = circlePort;
        this.userPort = userPort;
        this.circleMemberPort = circleMemberPort;
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
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), circle.getDOMAIN()))
                .validate();

        return CircleResponseDto.from(
                circle,
                this.circleMemberPort.getNumMember(id)
        );
    }

    @Transactional(readOnly = true)
    public List<CircleAllResponseDto> findAll(String userId) {
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
                .map(
                    circleDomainModel -> {
                        if (joinedCircleMap.containsKey(circleDomainModel.getId())) {
                            return CircleAllResponseDto.from(
                                    circleDomainModel,
                                    this.circleMemberPort.getNumMember(circleDomainModel.getId()),
                                    joinedCircleMap.get(circleDomainModel.getId()).getUpdatedAt()
                            );
                        } else {
                            return CircleAllResponseDto.from(
                                    circleDomainModel,
                                    this.circleMemberPort.getNumMember(circleDomainModel.getId())
                            );
                        }
                    }
                )
                .collect(Collectors.toList());
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
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), circle.getDOMAIN()))
                .consistOf(UserRoleValidator.of(user.getRole(), List.of(Role.LEADER_CIRCLE, Role.PRESIDENT)))
                .validate();

        return this.circleMemberPort.findByCircleId(circleId, status)
                .stream()
                .map(CircleMemberResponseDto::from)
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

        CircleDomainModel circleDomainModel = CircleDomainModel.of(
                circle.getId(),
                circleUpdateRequestDto.getName(),
                circleUpdateRequestDto.getMainImage(),
                circleUpdateRequestDto.getDescription(),
                circle.getIsDeleted(),
                circle.getLeader().orElse(null)
        );

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), circleDomainModel.getDOMAIN()))
                .consistOf(ConstraintValidator.of(circleDomainModel, this.validator))
                .consistOf(UserRoleValidator.of(
                        user.getRole(),
                        List.of(Role.PRESIDENT, Role.LEADER_CIRCLE)
                ));

        if (user.getRole().equals(Role.LEADER_CIRCLE)) {
            validatorBucket
                    .consistOf(UserEqualValidator.of(
                            circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
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

        return CircleResponseDto.from(this.circlePort.update(circleId, circleDomainModel).orElseThrow(
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
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), circle.getDOMAIN()))
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
        ValidatorBucket validatorBucket = ValidatorBucket.of();

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

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), circle.getDOMAIN()))
                .consistOf(StudentIdIsNullValidator.of(user.getStudentId()));

        return CircleMemberResponseDto.from(this.circleMemberPort.findByUserIdAndCircleId(user.getId(), circle.getId()).map(
                circleMember -> {
                    validatorBucket
                            .consistOf(
                                    CircleMemberStatusValidator.of(
                                            circleMember.getStatus(),
                                            List.of(CircleMemberStatus.LEAVE, CircleMemberStatus.REJECT)
                                    )
                            )
                            .consistOf(TimePassedValidator.of(circleMember.getUpdatedAt()))
                            .validate();

                    return this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.AWAIT).orElseThrow(
                            () -> new InternalServerException(
                                    ErrorCode.INTERNAL_SERVER,
                                    "Application id checked, but exception occurred"
                            )
                    );
                }
        ).orElseGet(
                () -> {
                    validatorBucket.validate();
                    return this.circleMemberPort.create(user, circle);
                }
        ));
    }

    @Transactional(readOnly = true)
    public DuplicatedCheckDto isDuplicatedName(String name) {
        return DuplicatedCheckDto.of(this.circlePort.findByName(name).isPresent());
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
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), circleMember.getCircle().getDOMAIN()))
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

        return CircleMemberResponseDto.from(this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.LEAVE).orElseThrow(
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
                .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), circle.getDOMAIN()))
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

        return CircleMemberResponseDto.from(this.circleMemberPort.updateStatus(circleMember.getId(), CircleMemberStatus.DROP).orElseThrow(
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

        validatorBucket
                .consistOf(UserStateValidator.of(requestUser.getState()))
                .consistOf(UserRoleIsNoneValidator.of(requestUser.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleMember.getCircle().getIsDeleted(), circleMember.getCircle().getDOMAIN()))
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

        return CircleMemberResponseDto.from(this.circleMemberPort.updateStatus(applicationId, targetStatus).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }
}
