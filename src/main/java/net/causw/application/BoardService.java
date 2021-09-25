package net.causw.application;

import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.BoardUpdateRequestDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.UserDomainModel;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;

@Service
public class BoardService {
    private final BoardPort boardPort;
    private final UserPort userPort;
    private final CirclePort circlePort;
    private final Validator validator;

    public BoardService(
            BoardPort boardPort,
            UserPort userPort,
            CirclePort circlePort,
            Validator validator
    ) {
        this.boardPort = boardPort;
        this.userPort = userPort;
        this.circlePort = circlePort;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public BoardResponseDto findById(String id) {
        return BoardResponseDto.from(this.boardPort.findById(id).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        ));
    }

    @Transactional
    public BoardResponseDto create(String creatorId, BoardCreateRequestDto boardCreateRequestDto) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserDomainModel creatorDomainModel = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid request user id"
                )
        );

        CircleDomainModel circleDomainModel = boardCreateRequestDto.getCircleId().map(
                circleId -> {
                    CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    "Invalid circle id"
                            )
                    );
                    validatorBucket
                            .consistOf(UserEqualValidator.of(circle.getLeader().getId(), creatorId))
                            .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.LEADER_CIRCLE, Role.ADMIN)));

                    return circle;
                }
        ).orElseGet(
                () -> {
                    validatorBucket
                            .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT, Role.ADMIN)));

                    return null;
                }
        );

        BoardDomainModel boardDomainModel = BoardDomainModel.of(
                boardCreateRequestDto.getName(),
                boardCreateRequestDto.getDescription(),
                boardCreateRequestDto.getCreateRoleList(),
                boardCreateRequestDto.getModifyRoleList(),
                boardCreateRequestDto.getReadRoleList(),
                circleDomainModel
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(boardDomainModel, this.validator))
                .validate();

        return BoardResponseDto.from(this.boardPort.create(boardDomainModel));
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
                        "Invalid user id"
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        );
        validatorBucket.consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted()));


        boardDomainModel.getCircle().ifPresentOrElse(
                circleDomainModel -> {
                    validatorBucket
                            .consistOf(UserEqualValidator.of(circleDomainModel.getLeader().getId(), updaterId))
                            .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.LEADER_CIRCLE)));
                },
                () -> validatorBucket
                        .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)))
        );

        boardDomainModel = BoardDomainModel.of(
                boardDomainModel.getId(),
                boardUpdateRequestDto.getName(),
                boardUpdateRequestDto.getDescription(),
                boardUpdateRequestDto.getCreateRoleList(),
                boardUpdateRequestDto.getModifyRoleList(),
                boardUpdateRequestDto.getReadRoleList(),
                boardDomainModel.getIsDeleted(),
                boardDomainModel.getCircle().orElse(null)
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(boardDomainModel, this.validator))
                .validate();

        return BoardResponseDto.from(this.boardPort.update(boardId, boardDomainModel).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Board id checked, but exception occurred"
                )
        ));
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
                        "Invalid user id"
                )
        );

        BoardDomainModel boardDomainModel = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        );
        validatorBucket.consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted()));

        boardDomainModel.getCircle().ifPresentOrElse(
                circleDomainModel ->
                        validatorBucket
                                .consistOf(UserEqualValidator.of(circleDomainModel.getLeader().getId(), deleterId))
                                .consistOf(UserRoleValidator.of(deleterDomainModel.getRole(), List.of(Role.LEADER_CIRCLE)))
                                .validate(),
                () ->
                        validatorBucket
                                .consistOf(UserRoleValidator.of(deleterDomainModel.getRole(), List.of(Role.PRESIDENT)))
                                .validate()
        );

        return BoardResponseDto.from(this.boardPort.delete(boardId).orElseThrow(
                () -> new InternalServerException(
                        ErrorCode.INTERNAL_SERVER,
                        "Application id checked, but exception occurred"
                )
        ));
    }
}
