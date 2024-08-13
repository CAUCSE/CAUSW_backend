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
import net.causw.domain.validation.UserRoleValidator;
import net.causw.domain.validation.TargetIsNotDeletedValidator;
import net.causw.domain.validation.ValidatorBucket;
import net.causw.domain.validation.valid.UserValid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Validator;

import java.util.*;
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
            @UserValid User user
    ) {
        Set<Role> roles = user.getRoles();

        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
            return boardRepository.findByOrderByCreatedAtAsc().stream()
                    .map(board -> toBoardResponseDto(board, roles))
                    .collect(Collectors.toList());
        } else {
            List<Circle> joinCircles = circleMemberRepository.findByUser_Id(user.getId()).stream()
                    .filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER)
                    .map(CircleMember::getCircle)
                    .toList();
            if (joinCircles.isEmpty()) {
                return boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false).stream()
                        .map(board -> toBoardResponseDto(board, roles))
                        .collect(Collectors.toList());
            } else {
                List<String> circleIdList = joinCircles.stream()
                        .map(Circle::getId)
                        .collect(Collectors.toList());

                return Stream.concat(
                                this.boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false).stream(),
                                this.boardRepository.findByCircle_IdInAndIsDeletedFalseOrderByCreatedAtAsc(circleIdList).stream())
                        .map(board -> toBoardResponseDto(board, roles))
                        .collect(Collectors.toList());
            }
        }
    }

    @Transactional
    public BoardResponseDto createBoard(
            @UserValid User creator,
            BoardCreateRequestDto boardCreateRequestDto
    ) {
        Set<Role> roles = creator.getRoles();

        ValidatorBucket validatorBucket = ValidatorBucket.of();

        Circle circle = boardCreateRequestDto.getCircleId().map(
                circleId -> {
                    Circle newCircle = getCircle(circleId);

                    new UserRoleValidator().validate(roles, Set.of(Role.LEADER_CIRCLE));
                    new TargetIsDeletedValidator().validate(newCircle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);

                    //동아리장인 경우와 회장단이 아닌경우에 아래 조건문을 실행한다.
                    if (roles.contains(Role.LEADER_CIRCLE)) {
                        new UserEqualValidator().validate(
                                creator.getId(),
                                newCircle.getLeader().map(User::getId).orElseThrow(
                                    () -> new UnauthorizedException(
                                            ErrorCode.API_NOT_ALLOWED,
                                            MessageUtil.NOT_CIRCLE_LEADER
                                    )
                                )
                        );
                    }

                    return newCircle;
                }
        ).orElseGet(
                () -> {
                    new UserRoleValidator().validate(roles, Set.of());
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

        return toBoardResponseDto(boardRepository.save(board), roles);
    }

    @Transactional
    public BoardResponseDto updateBoard(
            User updater,
            String boardId,
            BoardUpdateRequestDto boardUpdateRequestDto
    ) {
        Set<Role> roles = updater.getRoles();
        Board board = getBoard(boardId);

        initializeValidatorBucket(updater, board);

        board.update(
                boardUpdateRequestDto.getName(),
                boardUpdateRequestDto.getDescription(),
                String.join(",", boardUpdateRequestDto.getCreateRoleList()),
                boardUpdateRequestDto.getCategory()
        );

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(ConstraintValidator.of(board, this.validator))
                .validate();

        return toBoardResponseDto(boardRepository.save(board), roles);
    }

    @Transactional
    public BoardResponseDto deleteBoard(
            User deleter,
            String boardId
    ) {
        Set<Role> roles = deleter.getRoles();
        Board board = getBoard(boardId);

        initializeValidatorBucket(deleter, board);

        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            new UserRoleValidator().validate(roles, Set.of());
        }

        board.setIsDeleted(true);
        return toBoardResponseDto(boardRepository.save(board), roles);
    }

    @Transactional
    public BoardResponseDto restoreBoard(
            @UserValid User restorer,
            String boardId
    ) {
        Set<Role> roles = restorer.getRoles();
        Board board = getBoard(boardId);
        new TargetIsNotDeletedValidator().validate(board.getIsDeleted(), StaticValue.DOMAIN_BOARD);

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles.ifPresentOrElse(
                circle -> {
                    new UserRoleValidator().validate(roles, Set.of(Role.LEADER_CIRCLE));
                    new TargetIsDeletedValidator().validate(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);

                    if (roles.contains(Role.LEADER_CIRCLE)) {
                        new UserEqualValidator().validate(
                                restorer.getId(),
                                circle.getLeader().map(User::getId).orElseThrow(
                                        () -> new UnauthorizedException(
                                                ErrorCode.API_NOT_ALLOWED,
                                                MessageUtil.NOT_CIRCLE_LEADER
                                        )
                                )
                        );
                    }
                },
                () -> new UserRoleValidator().validate(roles, Set.of())
        );

        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            new UserRoleValidator().validate(roles, Set.of());
        }
        board.setIsDeleted(false);
        return toBoardResponseDto(boardRepository.save(board), roles);
    }

    private void initializeValidatorBucket(@UserValid User user, Board board) {
        Set<Role> roles = user.getRoles();
        new TargetIsDeletedValidator().validate(board.getIsDeleted(), StaticValue.DOMAIN_BOARD);

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles.ifPresentOrElse(
                circle -> {
                    new UserRoleValidator().validate(roles, Set.of(Role.LEADER_CIRCLE));
                    new TargetIsDeletedValidator().validate(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE);

                    if (roles.contains(Role.LEADER_CIRCLE)) {
                        new UserEqualValidator().validate(
                                user.getId(),
                                circle.getLeader().map(User::getId).orElseThrow(
                                        () -> new UnauthorizedException(
                                                ErrorCode.API_NOT_ALLOWED,
                                                MessageUtil.NOT_CIRCLE_LEADER
                                        )
                                )
                        );
                    }
                },
                () -> new UserRoleValidator().validate(roles, Set.of())
        );
    }

    private BoardResponseDto toBoardResponseDto(Board board, Set<Role> userRoles) {
        List<String> roles = Arrays.asList(board.getCreateRoles().split(","));
        Boolean writable = userRoles.stream()
                .map(Role::getValue)
                .anyMatch(roles::contains);
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
