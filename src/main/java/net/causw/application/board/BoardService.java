package net.causw.application.board;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.repository.BoardRepository;
import net.causw.adapter.persistence.repository.CircleMemberRepository;
import net.causw.adapter.persistence.repository.CircleRepository;
import net.causw.adapter.persistence.repository.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.BoardCreateRequestDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.board.BoardUpdateRequestDto;
import net.causw.application.dto.board.NormalBoardCreateRequestDto;
import net.causw.application.dto.util.DtoMapper;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.model.util.StaticValue;
import net.causw.domain.validation.ConstraintValidator;
import net.causw.domain.validation.TargetIsDeletedValidator;
import net.causw.domain.validation.UserEqualValidator;
import net.causw.domain.validation.UserRoleIsNoneValidator;
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.UserStateValidator;
import net.causw.domain.validation.TargetIsNotDeletedValidator;
import net.causw.domain.validation.ValidatorBucket;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final Validator validator;

    @Transactional(readOnly = true)
    public List<BoardResponseDto> findAllBoard(
            String loginUserId
    ) {
        User user = getUser(loginUserId);

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .validate();

        if (user.getRole().equals(Role.ADMIN) || user.getRole().getValue().contains("PRESIDENT")) {
            return boardRepository.findByOrderByCreatedAtAsc().stream()
                    .map(board -> toBoardResponseDto(board, user.getRole()))
                    .collect(Collectors.toList());
        } else {
            List<Circle> joinCircles = circleMemberRepository.findByUser_Id(user.getId()).stream()
                    .filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER)
                    .map(CircleMember::getCircle)
                    .collect(Collectors.toList());
            if (joinCircles.isEmpty()) {
                return boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false).stream()
                        .map(board -> toBoardResponseDto(board, user.getRole()))
                        .collect(Collectors.toList());
            } else {
                List<String> circleIdList = joinCircles.stream()
                        .map(Circle::getId)
                        .collect(Collectors.toList());

                return Stream.concat(
                                this.boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false).stream(),
                                this.boardRepository.findByCircle_IdInAndIsDeletedFalseOrderByCreatedAtAsc(circleIdList).stream())
                        .map(board -> toBoardResponseDto(board, user.getRole()))
                        .collect(Collectors.toList());
            }
        }
    }

    @Transactional
    public BoardResponseDto createBoard(
            String loginUserId,
            BoardCreateRequestDto boardCreateRequestDto
    ) {
        User creator = getUser(loginUserId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(creator.getState()))
                .consistOf(UserRoleIsNoneValidator.of(creator.getRole()));

        Circle circle = boardCreateRequestDto.getCircleId().map(
                circleId -> {
                    Circle newCircle = getCircle(circleId);

                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(newCircle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                            .consistOf(UserRoleValidator.of(creator.getRole(),
                                    List.of(Role.LEADER_CIRCLE)));

                    if (creator.getRole().getValue().contains("LEADER_CIRCLE") && !creator.getRole().getValue().contains("PRESIDENT")) {
                        validatorBucket
                                .consistOf(UserEqualValidator.of(
                                        newCircle.getLeader().map(User::getId).orElseThrow(
                                                () -> new UnauthorizedException(
                                                        ErrorCode.API_NOT_ALLOWED,
                                                        MessageUtil.NOT_CIRCLE_LEADER
                                                )
                                        ),
                                        loginUserId
                                ));
                    }

                    return newCircle;
                }
        ).orElseGet(
                () -> {
                    validatorBucket
                            .consistOf(UserRoleValidator.of(creator.getRole(), List.of()));

                    return null;
                }
        );

        Board board = Board.of(
                boardCreateRequestDto.getName(),
                boardCreateRequestDto.getDescription(),
                boardCreateRequestDto.getCreateRoleList(),
                boardCreateRequestDto.getCategory(),
                circle
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(board, this.validator))
                .validate();

        return toBoardResponseDto(boardRepository.save(board), creator.getRole());
    }

    @Transactional
    public BoardResponseDto createNormalBoard(
            String loginUserId,
            NormalBoardCreateRequestDto normalBoardCreateRequestDto
    ) {
        User creator = getUser(loginUserId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(creator.getState()))   // 활성화된 사용자인지 확인
                .consistOf(UserRoleIsNoneValidator.of(creator.getRole())); // 권한이 없는 사용자인지 확인

        Board board = Board.fromName(normalBoardCreateRequestDto.getBoardName());

        validatorBucket
                .consistOf(ConstraintValidator.of(board, this.validator))
                .validate();

        return toBoardResponseDto(boardRepository.save(board), creator.getRole());
    }


    @Transactional
    public BoardResponseDto updateBoard(
            String loginUserId,
            String boardId,
            BoardUpdateRequestDto boardUpdateRequestDto
    ) {
        User updater = getUser(loginUserId);
        Board board = getBoard(boardId);

        ValidatorBucket validatorBucket = initializeValidatorBucket(updater, board);

        board.update(
                boardUpdateRequestDto.getName(),
                boardUpdateRequestDto.getDescription(),
                String.join(",", boardUpdateRequestDto.getCreateRoleList()),
                boardUpdateRequestDto.getCategory()
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(board, this.validator))
                .validate();

        return toBoardResponseDto(boardRepository.save(board), updater.getRole());
    }

    @Transactional
    public BoardResponseDto deleteBoard(
            String loginUserId,
            String boardId
    ) {
        User deleter = getUser(loginUserId);
        Board board = getBoard(boardId);

        ValidatorBucket validatorBucket = initializeValidatorBucket(deleter, board);
        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            deleter.getRole(),
                            List.of()
                    ));
        }
        validatorBucket.validate();

        board.setIsDeleted(true);
        return toBoardResponseDto(boardRepository.save(board), deleter.getRole());
    }

    @Transactional
    public BoardResponseDto restoreBoard(
            String loginUserId,
            String boardId
    ) {
        User restorer = getUser(loginUserId);
        Board board = getBoard(boardId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();

        validatorBucket
                .consistOf(UserStateValidator.of(restorer.getState()))
                .consistOf(UserRoleIsNoneValidator.of(restorer.getRole()))
                .consistOf(TargetIsNotDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD));

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles.ifPresentOrElse(
                circle -> {
                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                            .consistOf(UserRoleValidator.of(restorer.getRole(),
                                    List.of(Role.LEADER_CIRCLE)));

                    if (restorer.getRole().getValue().contains("LEADER_CIRCLE") && !restorer.getRole().getValue().contains("PRESIDENT")) {
                        validatorBucket
                                .consistOf(UserEqualValidator.of(
                                        circle.getLeader().map(User::getId).orElseThrow(
                                                () -> new UnauthorizedException(
                                                        ErrorCode.API_NOT_ALLOWED,
                                                        MessageUtil.NOT_CIRCLE_LEADER
                                                )
                                        ),
                                        restorer.getId()
                                ));
                    }
                },
                () -> validatorBucket
                        .consistOf(UserRoleValidator.of(restorer.getRole(), List.of()))
        );

        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            restorer.getRole(),
                            List.of()
                    ));
        }
        validatorBucket.validate();

        board.setIsDeleted(false);
        return toBoardResponseDto(boardRepository.save(board), restorer.getRole());
    }

    private ValidatorBucket initializeValidatorBucket(User user, Board board) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(user.getRole()))
                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD));

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles.ifPresentOrElse(
                circle -> {
                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                            .consistOf(UserRoleValidator.of(user.getRole(),
                                    List.of(Role.LEADER_CIRCLE)));

                    if (user.getRole().getValue().contains("LEADER_CIRCLE") && !user.getRole().getValue().contains("PRESIDENT")) {
                        validatorBucket
                                .consistOf(UserEqualValidator.of(
                                        circle.getLeader().map(User::getId).orElseThrow(
                                                () -> new UnauthorizedException(
                                                        ErrorCode.API_NOT_ALLOWED,
                                                        MessageUtil.NOT_CIRCLE_LEADER
                                                )
                                        ),
                                        user.getId()
                                ));
                    }
                },
                () -> validatorBucket
                        .consistOf(UserRoleValidator.of(user.getRole(), List.of()))
        );
        return validatorBucket;
    }

    private BoardResponseDto toBoardResponseDto(Board board, Role userRole) {
        List<String> roles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));
        Boolean writable = roles.stream().anyMatch(str -> userRole.getValue().contains(str));
        String circleId = Optional.ofNullable(board.getCircle()).map(Circle::getId).orElse(null);
        String circleName = Optional.ofNullable(board.getCircle()).map(Circle::getName).orElse(null);
        return DtoMapper.INSTANCE.toBoardResponseDto(
                board,
                roles,
                writable,
                circleId,
                circleName
        );
    }

    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                )
        );
    }

    private Board getBoard(String boardId) {
        return boardRepository.findById(boardId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.BOARD_NOT_FOUND
                )
        );
    }

    private Circle getCircle(String circleId) {
        return circleRepository.findById(circleId).orElseThrow(
                () -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.SMALL_CLUB_NOT_FOUND
                )
        );
    }
}
