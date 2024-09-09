package net.causw.application.board;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.board.BoardApply;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.repository.*;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.*;
import net.causw.application.dto.post.PostContentDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.util.DtoMapper;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.BoardApplyStatus;
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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final BoardApplyRepository boardApplyRepository;
    private final Validator validator;


    @Transactional(readOnly = true)
    public List<BoardResponseDto> findAllBoard(
            User user
    ) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .validate();

        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
            return boardRepository.findByOrderByCreatedAtAsc().stream()
                    .map(board -> toBoardResponseDto(board, roles))
                    .collect(Collectors.toList());
        } else {
            List<Circle> joinCircles = circleMemberRepository.findByUser_Id(user.getId()).stream()
                    .filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER)
                    .map(CircleMember::getCircle)
                    .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public List<BoardMainResponseDto> mainBoard(
            User user
    ) {
        Set<Role> roles = user.getRoles();

        ValidatorBucket.of()
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .validate();

        List<Board> boards;

        if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
            boards = boardRepository.findByOrderByCreatedAtAsc();
        }else{
            List<Circle> joinCircles = circleMemberRepository.findByUser_Id(user.getId()).stream()
                .filter(circleMember -> circleMember.getStatus() == CircleMemberStatus.MEMBER)
                .map(CircleMember::getCircle)
                .collect(Collectors.toList());

            if (joinCircles.isEmpty()) {
                boards = boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false);
            } else {
                List<String> circleIdList = joinCircles.stream()
                        .map(Circle::getId)
                        .collect(Collectors.toList());

                boards = Stream.concat(
                                boardRepository.findByCircle_IdIsNullAndIsDeletedOrderByCreatedAtAsc(false).stream(),
                                boardRepository.findByCircle_IdInAndIsDeletedFalseOrderByCreatedAtAsc(circleIdList).stream()
                        )
                        .collect(Collectors.toList());
            }
        }

        return boards.stream()
                .map(board -> {
                    List<PostContentDto> recentPosts = postRepository.findTop3ByBoard_IdAndIsDeletedOrderByCreatedAtDesc(board.getId(), false).stream()
                            .map(DtoMapper.INSTANCE::toPostContentDto)
                            .collect(Collectors.toList());
                    return DtoMapper.INSTANCE.toBoardMainResponseDto(board, recentPosts);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardNameCheckResponseDto checkBoardName (
            BoardNameCheckRequestDto boardNameCheckRequestDto
    ) {
        String boardName = boardNameCheckRequestDto.getName();

        return DtoMapper.INSTANCE.toBoardNameCheckResponseDto(boardRepository.existsByName(boardName));
    }

    // 동아리 게시판 생성에서 재사용 예정인데 일단 안쓰므로 주석 처리.
//    @Transactional
//    public BoardResponseDto createBoard(
//            User creator,
//            BoardCreateRequestDto boardCreateRequestDto
//    ) {
//        Set<Role> roles = creator.getRoles();
//
//        ValidatorBucket validatorBucket = ValidatorBucket.of();
//        validatorBucket
//                .consistOf(UserStateValidator.of(creator.getState()))
//                .consistOf(UserRoleIsNoneValidator.of(roles));
//
//        Circle circle = boardCreateRequestDto.getCircleId().map(
//                circleId -> {
//                    Circle newCircle = getCircle(circleId);
//
//                    validatorBucket
//                            .consistOf(TargetIsDeletedValidator.of(newCircle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
//                            //동아리장이거나 관리자만 통과
//                            .consistOf(UserRoleValidator.of(roles,
//                                    Set.of(Role.LEADER_CIRCLE)));
//
//                    //동아리장인 경우와 회장단이 아닌경우에 아래 조건문을 실행한다.
//                    if (roles.contains(Role.LEADER_CIRCLE)) {
//                        validatorBucket
//                                .consistOf(UserEqualValidator.of(
//                                        newCircle.getLeader().map(User::getId).orElseThrow(
//                                                () -> new UnauthorizedException(
//                                                        ErrorCode.API_NOT_ALLOWED,
//                                                        MessageUtil.NOT_CIRCLE_LEADER
//                                                )
//                                        ),
//                                        creator.getId()
//                                ));
//                    }
//
//                    return newCircle;
//                }
//        ).orElseGet(
//                () -> {
//                    validatorBucket
//                            .consistOf(UserRoleValidator.of(roles, Set.of()));
//
//                    return null;
//                }
//        );

//        Board board = Board.of(
//                boardCreateRequestDto.getName(),
//                boardCreateRequestDto.getDescription(),
//                boardCreateRequestDto.getCreateRoleList(),
//                boardCreateRequestDto.getCategory(),
//                circle
//        );
//
////        validatorBucket
////                .consistOf(ConstraintValidator.of(board, this.validator))
////                .validate();
//
//        return toBoardResponseDto(boardRepository.save(board), roles);
//    }

    @Transactional
    public void applyNormalBoard(
            User creator,
            NormalBoardApplyRequestDto normalBoardApplyRequestDto
    ) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(creator.getState()))   // 활성화된 사용자인지 확인
                .consistOf(UserRoleIsNoneValidator.of(creator.getRoles())); // 권한이 없는 사용자인지 확인

        BoardApply newBoardApply = BoardApply.of(
                creator,
                normalBoardApplyRequestDto.getBoardName(),
                normalBoardApplyRequestDto.getDescription(),
                StaticValue.BOARD_NAME_APP_FREE,
                normalBoardApplyRequestDto.getIsAnonymousAllowed()
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(newBoardApply, this.validator))
                .validate();

        boardApplyRepository.save(newBoardApply);
    }

    @Transactional
    public BoardResponseDto createNormalBoard(
            User creator,
            NormalBoardCreateRequestDto normalBoardCreateRequestDto
    ) {

        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(creator.getState()))   // 활성화된 사용자인지 확인
                .consistOf(UserRoleIsNoneValidator.of(creator.getRoles())) // 권한이 없는 사용자인지 확인
                .consistOf(UserRoleValidator.of(creator.getRoles(), Set.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT))); // 권한이 관리자, 학생회장, 부학생회장 중 하나인지 확인


        // 로그인 유저의 권한이 관리자, 학생회장, 부학생회장 중 하나인 경우
        // 아무런 조건 없이 게시판을 생성할 수 있음
        Board newBoard = Board.of(
                normalBoardCreateRequestDto.getBoardName(),
                normalBoardCreateRequestDto.getDescription(),
                normalBoardCreateRequestDto.getCreateRoleList(),
                StaticValue.BOARD_NAME_APP_NOTICE,
                normalBoardCreateRequestDto.getIsAnonymousAllowed(),
                null
        );

        validatorBucket
                .consistOf(ConstraintValidator.of(newBoard, this.validator))
                .validate();

        return toBoardResponseDto(boardRepository.save(newBoard), creator.getRoles());
    }

    @Transactional(readOnly = true)
    public List<NormalBoardAppliesResponseDto> findAllBoardApply(User user) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))   // 활성화된 사용자인지 확인
                .consistOf(UserRoleIsNoneValidator.of(user.getRoles())) // 권한이 없는 사용자인지 확인
                .consistOf(UserRoleValidator.of(user.getRoles(), Set.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT))); // 권한이 관리자, 학생회장, 부학생회장 중 하나인지 확인

        // 관리자, 학생회장, 부학생회장만 게시판 관리 기능 사용 가능
        return this.boardApplyRepository.findAllByAcceptStatus(BoardApplyStatus.AWAIT)
                .stream()
                .map(DtoMapper.INSTANCE::toNormalBoardAppliesResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NormalBoardApplyResponseDto findBoardApplyByBoardName(User user, String applyId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))   // 활성화된 사용자인지 확인
                .consistOf(UserRoleIsNoneValidator.of(user.getRoles())) // 권한이 없는 사용자인지 확인
                .consistOf(UserRoleValidator.of(user.getRoles(), Set.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT))); // 권한이 관리자, 학생회장, 부학생회장 중 하나인지 확인

        // 관리자, 학생회장, 부학생회장만 게시판 관리 기능 사용 가능
        BoardApply boardApply = this.boardApplyRepository.findById(applyId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.APPLY_NOT_FOUND
                ));
        return DtoMapper.INSTANCE.toNormalBoardApplyResponseDto(
                boardApply, DtoMapper.INSTANCE.toUserResponseDto(boardApply.getUser(), null, null));
    }

    @Transactional
    public NormalBoardApplyResponseDto accept(User user, String boardApplyId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))   // 활성화된 사용자인지 확인
                .consistOf(UserRoleIsNoneValidator.of(user.getRoles())) // 권한이 없는 사용자인지 확인
                .consistOf(UserRoleValidator.of(user.getRoles(), Set.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT))); // 권한이 관리자, 학생회장, 부학생회장 중 하나인지 확인

        BoardApply boardApply = this.boardApplyRepository.findById(boardApplyId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.APPLY_NOT_FOUND
                ));

        if (boardApply.getAcceptStatus() == BoardApplyStatus.ACCEPTED) { // 해당 신청이 이미 승인된 경우
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.APPLY_ALREADY_ACCEPTED
            );
        }

        if (boardApply.getAcceptStatus() == BoardApplyStatus.REJECT) { // 해당 신청이 이미 거부된 경우
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.APPLY_ALREADY_REJECTED
            );
        }

        boardApply.updateAcceptStatus(BoardApplyStatus.ACCEPTED); // 해당 boardApply의 상태를 ACCEPTED로 변경
        this.boardApplyRepository.save(boardApply);

        List<String> createRoleList = new ArrayList<>();
        createRoleList.add("ALL"); // 일반 사용자의 게시판 신청은 항상 글 작성 권한이 '상관없음'임
        UserResponseDto userResponseDto = DtoMapper.INSTANCE.toUserResponseDto(boardApply.getUser(), null, null);
        NormalBoardApplyResponseDto normalBoardApplyResponseDto =
                DtoMapper.INSTANCE.toNormalBoardApplyResponseDto(boardApply, userResponseDto);
        Board newBoard = Board.of(
                normalBoardApplyResponseDto.getBoardName(),
                normalBoardApplyResponseDto.getDescription(),
                createRoleList,
                StaticValue.BOARD_NAME_APP_NOTICE,
                normalBoardApplyResponseDto.getIsAnonymousAllowed(),
                null
        );

        this.boardRepository.save(newBoard);

        return DtoMapper.INSTANCE.toNormalBoardApplyResponseDto(boardApply, userResponseDto);
    }

    @Transactional
    public NormalBoardApplyResponseDto reject(User user, String boardApplyId) {
        ValidatorBucket validatorBucket = ValidatorBucket.of();
        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))   // 활성화된 사용자인지 확인
                .consistOf(UserRoleIsNoneValidator.of(user.getRoles())) // 권한이 없는 사용자인지 확인
                .consistOf(UserRoleValidator.of(user.getRoles(), Set.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT))); // 권한이 관리자, 학생회장, 부학생회장 중 하나인지 확인

        BoardApply boardApply = this.boardApplyRepository.findById(boardApplyId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.APPLY_NOT_FOUND
                ));

        if (boardApply.getAcceptStatus() == BoardApplyStatus.ACCEPTED) { // 해당 신청이 이미 승인된 경우
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.APPLY_ALREADY_ACCEPTED
            );
        }

        if (boardApply.getAcceptStatus() == BoardApplyStatus.REJECT) { // 해당 신청이 이미 거부된 경우
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.APPLY_ALREADY_REJECTED
            );
        }

        boardApply.updateAcceptStatus(BoardApplyStatus.REJECT); // 해당 boardApply의 상태를 REJECT로 변경
        this.boardApplyRepository.save(boardApply);

        return DtoMapper.INSTANCE.toNormalBoardApplyResponseDto(
                boardApply,
                DtoMapper.INSTANCE.toUserResponseDto(boardApply.getUser(), null, null));
    }

    @Transactional
    public BoardResponseDto updateBoard(
            User updater,
            String boardId,
            BoardUpdateRequestDto boardUpdateRequestDto
    ) {
        Set<Role> roles = updater.getRoles();
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

        return toBoardResponseDto(boardRepository.save(board), roles);
    }

    @Transactional
    public BoardResponseDto deleteBoard(
            User deleter,
            String boardId
    ) {
        Set<Role> roles = deleter.getRoles();
        Board board = getBoard(boardId);

        ValidatorBucket validatorBucket = initializeValidatorBucket(deleter, board);
        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            roles,
                            Set.of()
                    ));
        }
        validatorBucket.validate();

        board.setIsDeleted(true);
        return toBoardResponseDto(boardRepository.save(board), roles);
    }

    @Transactional
    public BoardResponseDto restoreBoard(
            User restorer,
            String boardId
    ) {
        Set<Role> roles = restorer.getRoles();
        Board board = getBoard(boardId);

        ValidatorBucket validatorBucket = ValidatorBucket.of();

        validatorBucket
                .consistOf(UserStateValidator.of(restorer.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsNotDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD));

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles.ifPresentOrElse(
                circle -> {
                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                            .consistOf(UserRoleValidator.of(roles,
                                    Set.of(Role.LEADER_CIRCLE)));

                    if (roles.contains(Role.LEADER_CIRCLE)) {
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
                        .consistOf(UserRoleValidator.of(roles, Set.of()))
        );

        if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
            validatorBucket
                    .consistOf(UserRoleValidator.of(
                            roles,
                            Set.of()
                    ));
        }
        validatorBucket.validate();

        board.setIsDeleted(false);
        return toBoardResponseDto(boardRepository.save(board), roles);
    }

    private ValidatorBucket initializeValidatorBucket(User user, Board board) {
        Set<Role> roles = user.getRoles();
        ValidatorBucket validatorBucket = ValidatorBucket.of();

        validatorBucket
                .consistOf(UserStateValidator.of(user.getState()))
                .consistOf(UserRoleIsNoneValidator.of(roles))
                .consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD));

        Optional<Circle> circles = Optional.ofNullable(board.getCircle());
        circles.ifPresentOrElse(
                circle -> {
                    validatorBucket
                            .consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
                            .consistOf(UserRoleValidator.of(roles,
                                    Set.of(Role.LEADER_CIRCLE)));

                    if (roles.contains(Role.LEADER_CIRCLE)) {
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
                        .consistOf(UserRoleValidator.of(roles, Set.of()))
        );
        return validatorBucket;
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
