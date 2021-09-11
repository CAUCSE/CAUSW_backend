package net.causw.application;

import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardFullDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.BoardUpdateRequestDto;
import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.Role;
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

        UserFullDto creatorFullDto = this.userPort.findById(creatorId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        CircleFullDto circleFullDto = null;
        if (boardCreateRequestDto.getCircleId() != null) {
            circleFullDto = this.circlePort.findById(boardCreateRequestDto.getCircleId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.ROW_DOES_NOT_EXIST,
                            "Invalid circle id"
                    )
            );
            validatorBucket
                    .consistOf(UserEqualValidator.of(circleFullDto.getManager().getId(), creatorId))
                    .consistOf(UserRoleValidator.of(creatorFullDto.getRole(), List.of(Role.LEADER_CIRCLE)));
        } else {
            validatorBucket
                    .consistOf(UserRoleValidator.of(creatorFullDto.getRole(), List.of(Role.PRESIDENT)));
        }

        BoardDomainModel boardDomainModel = BoardDomainModel.of(
                null,
                boardCreateRequestDto.getName(),
                boardCreateRequestDto.getDescription(),
                boardCreateRequestDto.getCreateRoleList(),
                boardCreateRequestDto.getModifyRoleList(),
                boardCreateRequestDto.getReadRoleList()
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(boardDomainModel, this.validator))
                .validate();

        return this.boardPort.create(boardCreateRequestDto, Optional.ofNullable(circleFullDto));
    }

    @Transactional
    public BoardResponseDto update(
            String updaterId,
            String boardId,
            BoardUpdateRequestDto boardUpdateRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        UserFullDto updaterFullDto = this.userPort.findById(updaterId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid user id"
                )
        );

        BoardFullDto boardFullDto = this.boardPort.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        );

        if (boardUpdateRequestDto.getCircleId() != null) {
            CircleFullDto circleFullDto = this.circlePort.findById(boardUpdateRequestDto.getCircleId()).orElseThrow(
                    () -> new BadRequestException(
                            ErrorCode.ROW_DOES_NOT_EXIST,
                            "Invalid circle id"
                    )
            );
            validatorBucket
                    .consistOf(UserEqualValidator.of(circleFullDto.getManager().getId(), updaterId))
                    .consistOf(UserRoleValidator.of(updaterFullDto.getRole(), List.of(Role.LEADER_CIRCLE)));
        } else {
            validatorBucket
                    .consistOf(UserRoleValidator.of(updaterFullDto.getRole(), List.of(Role.PRESIDENT)));
        }

        BoardDomainModel boardDomainModel = BoardDomainModel.of(
                null,
                boardUpdateRequestDto.getName(),
                boardUpdateRequestDto.getDescription(),
                boardUpdateRequestDto.getCreateRoleList(),
                boardUpdateRequestDto.getModifyRoleList(),
                boardUpdateRequestDto.getReadRoleList()
        );

        validatorBucket
                .consistOf(TargetIsDeletedValidator.of(boardFullDto.getIsDeleted()))
                .consistOf(ConstraintValidator.of(boardDomainModel, this.validator))
                .validate();

        return this.boardPort.update(boardId, boardUpdateRequestDto).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        "Invalid board id"
                )
        );
    }
}
