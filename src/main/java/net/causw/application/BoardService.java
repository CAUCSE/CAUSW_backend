package net.causw.application;

import net.causw.application.dto.BoardCreateRequestDto;
import net.causw.application.dto.BoardResponseDto;
import net.causw.application.dto.CircleFullDto;
import net.causw.application.dto.UserFullDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.BoardRoleValidator;
import net.causw.domain.validation.CorrectCircleLeaderValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
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
        return this.boardPort.findById(id);
    }

    @Transactional
    public BoardResponseDto create(String creatorId, BoardCreateRequestDto boardCreateRequestDto) {
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
        }

        BoardDomainModel boardDomainModel = BoardDomainModel.of(
                null,
                boardCreateRequestDto.getName(),
                boardCreateRequestDto.getDescription(),
                boardCreateRequestDto.getCreateRoleList(),
                boardCreateRequestDto.getModifyRoleList(),
                boardCreateRequestDto.getReadRoleList()
        );

        ConstraintValidator.of(boardDomainModel, this.validator)
                .linkWith(BoardRoleValidator.of(creatorFullDto.getRole(), boardCreateRequestDto.getCircleId())
                        .linkWith(CorrectCircleLeaderValidator.of(circleFullDto, creatorId)))
                .validate();

        return this.boardPort.create(boardCreateRequestDto, Optional.ofNullable(circleFullDto));
    }
}
