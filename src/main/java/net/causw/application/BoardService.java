package net.causw.application;

import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.BoardUpdateRequestDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
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
import java.util.Optional;

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
                        "Invalid user id"
                )
        );

        Optional<CircleDomainModel> circleDomainModel = Optional.ofNullable(boardCreateRequestDto.getCircleId().map(
                circleId -> {
                    CircleDomainModel circle = this.circlePort.findById(circleId).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    "Invalid circle id"
                            )
                    );
                    validatorBucket
                            .consistOf(UserEqualValidator.of(circle.getLeader().getId(), creatorId))
                            .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.LEADER_CIRCLE)));

                    return circle;
                }
        ).orElseGet(
                () -> {
                    validatorBucket
                            .consistOf(UserRoleValidator.of(creatorDomainModel.getRole(), List.of(Role.PRESIDENT)));

                    return null;
                }
        ));

        BoardDomainModel boardDomainModel = BoardDomainModel.of(
                boardCreateRequestDto.getName(),
                boardCreateRequestDto.getDescription(),
                boardCreateRequestDto.getCreateRoleList(),
                boardCreateRequestDto.getModifyRoleList(),
                boardCreateRequestDto.getReadRoleList(),
                boardCreateRequestDto.getCircleId().orElse(null)
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(boardDomainModel, this.validator))
                .validate();

        return BoardResponseDto.from(this.boardPort.create(boardDomainModel, circleDomainModel));
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

        boardUpdateRequestDto.getCircleId().ifPresentOrElse(
                circleId -> {
                    CircleDomainModel circleDomainModel = this.circlePort.findById(circleId).orElseThrow(
                            () -> new BadRequestException(
                                    ErrorCode.ROW_DOES_NOT_EXIST,
                                    "Invalid circle id"
                            )
                    );
                    validatorBucket
                            .consistOf(UserEqualValidator.of(circleDomainModel.getLeader().getId(), updaterId))
                            .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.LEADER_CIRCLE)));
                },
                () -> {
                    validatorBucket
                            .consistOf(UserRoleValidator.of(updaterDomainModel.getRole(), List.of(Role.PRESIDENT)));
                }
        );

        boardDomainModel = BoardDomainModel.of(
                boardDomainModel.getId(),
                boardUpdateRequestDto.getName(),
                boardUpdateRequestDto.getDescription(),
                boardUpdateRequestDto.getCreateRoleList(),
                boardUpdateRequestDto.getModifyRoleList(),
                boardUpdateRequestDto.getReadRoleList(),
                boardDomainModel.getIsDeleted(),
                boardDomainModel.getCircleId()
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(boardDomainModel, this.validator))
                .validate();

        return BoardResponseDto.from(this.boardPort.update(boardId, boardDomainModel).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        ));
    }
}
