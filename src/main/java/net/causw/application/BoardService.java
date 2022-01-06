package net.causw.application;

import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardOfCircleResponseDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.BoardUpdateRequestDto;
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
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.CircleMemberStatusValidator;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {
    private final BoardPort boardPort;
    private final UserPort userPort;
    private final PostPort postPort;
    private final CirclePort circlePort;
    private final CircleMemberPort circleMemberPort;
    private final CommentPort commentPort;
    private final Validator validator;

    public BoardService(
            BoardPort boardPort,
            UserPort userPort,
            PostPort postPort,
            CirclePort circlePort,
            CircleMemberPort circleMemberPort,
            CommentPort commentPort,
            Validator validator
    ) {
        this.boardPort = boardPort;
        this.userPort = userPort;
        this.postPort = postPort;
        this.circlePort = circlePort;
        this.circleMemberPort = circleMemberPort;
        this.commentPort = commentPort;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> findAll(String userId, Boolean isDeleted) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel userDomainModel = this.userPort.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        if (isDeleted) {
            validatorBucket.consistOf(UserRoleValidator.of(userDomainModel.getRole(), List.of(Role.PRESIDENT)));
        }

        validatorBucket
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .validate();

        if (isDeleted) {
            return this.boardPort.findDeleted()
                    .stream()
                    .map(boardDomainModel -> BoardResponseDto.from(boardDomainModel, userDomainModel.getRole()))
                    .collect(Collectors.toList());
        }

        return this.boardPort.findAll()
                .stream()
                .map(boardDomainModel -> BoardResponseDto.from(boardDomainModel, userDomainModel.getRole()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BoardOfCircleResponseDto> findAllByCircleId(
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

        CircleMemberDomainModel circleMember = this.circleMemberPort.findByUserIdAndCircleId(currentUserId, circleDomainModel.getId()).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.NOT_MEMBER,
                        "로그인된 사용자가 가입 신청한 소모임이 아닙니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(userDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(userDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), circleDomainModel.getDOMAIN()))
                .consistOf(CircleMemberStatusValidator.of(
                        circleMember.getStatus(),
                        List.of(CircleMemberStatus.MEMBER)
                ))
                .validate();

        return this.boardPort.findByCircleId(circleId)
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
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardResponseDto create(String creatorId, BoardCreateRequestDto boardCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(creatorDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creatorDomainModel.getRole()));

        CircleDomainModel circleDomainModel = boardCreateRequestDto.getCircleId().map(
                circleId -> {
                    CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    "소모임을 찾을 수 없습니다."
                            )
                    );
                    validatorBucket
                            .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.LEADER_CIRCLE)));

                    if (creatorDomainModel.getRole().equals(Role.LEADER_CIRCLE)) {
                        validatorBucket
                                .consistOf(UserEqualValidator.of(
                                        circle.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                () -> new InternalServerException(
                                                        ErrorCode.INTERNAL_SERVER,
                                                        "The board has circle without circle leader"
                                                )
                                        ),
                                        creatorId
                                ));
                    }

                    return circle;
                }
        ).orElseGet(
                () -> {
                    validatorBucket
                            .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)));

                    return null;
                }
        );

        BoardDomainModel boardDomainModel = BoardDomainModel.of(
                boardCreateRequestDto.getName(),
                boardCreateRequestDto.getDescription(),
                boardCreateRequestDto.getCreateRoleList(),
                boardCreateRequestDto.getCategory(),
                circleDomainModel
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(boardDomainModel, this.validator))
                .validate();

        return BoardResponseDto.from(this.boardPort.create(boardDomainModel), creatorDomainModel.getRole());
    }

    @Transactional
    public BoardResponseDto update(
            String updaterId,
            String boardId,
            BoardUpdateRequestDto boardUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel updaterDomainModel = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "수정할 게시판을 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(updaterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(updaterDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), boardDomainModel.getDOMAIN()));

        boardDomainModel.getCircle().ifPresentOrElse(
                circleDomainModel -> {
                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), circleDomainModel.getDOMAIN()))
                            .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.LEADER_CIRCLE)));

                    if (updaterDomainModel.getRole().equals(Role.LEADER_CIRCLE)) {
                        validatorBucket
                                .consistOf(UserEqualValidator.of(
                                        circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                () -> new InternalServerException(
                                                        ErrorCode.INTERNAL_SERVER,
                                                        "The board has circle without circle leader"
                                                )
                                        ),
                                        updaterId
                                ));
                    }
                },
                () -> validatorBucket
                        .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
        );

        boardDomainModel = BoardDomainModel.of(
                boardDomainModel.getId(),
                boardUpdateRequestDto.getName(),
                boardUpdateRequestDto.getDescription(),
                boardUpdateRequestDto.getCreateRoleList(),
                boardUpdateRequestDto.getCategory(),
                boardDomainModel.getIsDeleted(),
                boardDomainModel.getCircle().orElse(null)
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(boardDomainModel, this.validator))
                .validate();

        return BoardResponseDto.from(
                this.boardPort.update(boardId, boardDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Board id checked, but exception occurred"
                        )
                ),
                updaterDomainModel.getRole()
        );
    }

    @Transactional
    public BoardResponseDto delete(
            String deleterId,
            String boardId
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel deleterDomainModel = this.userPort.findById(deleterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "삭제할 게시판을 찾을 수 없습니다."
                )
        );

        validatorBucket
                .consistOf(UserStateValidator.of(deleterDomainModel.getState()))
                .consistOf(UserRoleIsNoneValidator.of(deleterDomainModel.getRole()))
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), boardDomainModel.getDOMAIN()));

        boardDomainModel.getCircle().ifPresentOrElse(
                circleDomainModel -> {
                    validatorBucket
                            .consistOf(UserRoleValidator.of(deleterDomainModel.getRole(), List.of(Role.LEADER_CIRCLE)));

                    if (deleterDomainModel.getRole().equals(Role.LEADER_CIRCLE)) {
                        validatorBucket
                                .consistOf(UserEqualValidator.of(
                                        circleDomainModel.getLeader().map(UserDomainModel::getId).orElseThrow(
                                                () -> new InternalServerException(
                                                        ErrorCode.INTERNAL_SERVER,
                                                        "The board has circle without circle leader"
                                                )
                                        ),
                                        deleterId
                                ));
                    }
                },
                () ->
                        validatorBucket
                                .consistOf(UserRoleValidator.of(deleterDomainModel.getRole(), List.of(Role.PRESIDENT)))
        );

        validatorBucket
                .validate();

        return BoardResponseDto.from(
                this.boardPort.delete(boardId).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Board id checked, but exception occurred"
                        )
                ),
                deleterDomainModel.getRole()
        );
    }

    @Transactional
    public BoardResponseDto restore(
            String updaterId,
            String boardId
    ) {
        UserDomainModel updaterDomainModel = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "로그인된 사용자를 찾을 수 없습니다."
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "복구할 게시판을 찾을 수 없습니다."
                )
        );

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(updaterDomainModel.getState()))
                .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                .validate();

        if (!boardDomainModel.getIsDeleted()) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    "삭제된 게시판이 아닙니다."
            );
        }

        boardDomainModel.setIsDeleted(false);

        return BoardResponseDto.from(
                this.boardPort.update(boardId, boardDomainModel).orElseThrow(
                        () -> new InternalServerException(
                                ErrorCode.INTERNAL_SERVER,
                                "Board id checked, but exception occurred"
                        )
                ),
                updaterDomainModel.getRole()
        );
    }
}
