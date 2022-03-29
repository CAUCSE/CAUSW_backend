package net.causw.application;

import net.causw.application.dto.board.BoardCreateRequestDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.board.BoardUpdateRequestDto;
import net.causw.application.spi.BoardPort;
import net.causw.application.spi.CirclePort;
import net.causw.application.spi.UserPort;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.BoardDomainModel;
import net.causw.domain.model.CircleDomainModel;
import net.causw.domain.model.Role;
import net.causw.domain.model.StaticValue;
import net.causw.domain.model.UserDomainModel;
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
    public List<BoardResponseDto> findAll(String userId) {
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

        return this.boardPort.findAll()
                .stream()
                .filter(boardDomainModel -> !boardDomainModel.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE))
                .map(boardDomainModel -> BoardResponseDto.from(boardDomainModel, userDomainModel.getRole()))
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
                            .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
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
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), StaticValue.DOMAIN_BOARD));

        boardDomainModel.getCircle().ifPresentOrElse(
                circleDomainModel -> {
                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
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

        boardDomainModel.update(
                boardUpdateRequestDto.getName(),
                boardUpdateRequestDto.getDescription(),
                boardUpdateRequestDto.getCreateRoleList(),
                boardUpdateRequestDto.getCategory()
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
                .consistOf(TargetIsDeletedValidator.of(boardDomainModel.getIsDeleted(), StaticValue.DOMAIN_BOARD));

        boardDomainModel.getCircle().ifPresentOrElse(
                circleDomainModel -> {
                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circleDomainModel.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
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
                () -> validatorBucket
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
}
