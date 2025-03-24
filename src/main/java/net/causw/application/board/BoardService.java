package net.causw.application.board;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.board.Board;
import net.causw.adapter.persistence.board.BoardApply;
import net.causw.adapter.persistence.circle.Circle;
import net.causw.adapter.persistence.circle.CircleMember;
import net.causw.adapter.persistence.notification.UserBoardSubscribe;
import net.causw.adapter.persistence.repository.board.BoardApplyRepository;
import net.causw.adapter.persistence.repository.board.BoardRepository;
import net.causw.adapter.persistence.repository.circle.CircleMemberRepository;
import net.causw.adapter.persistence.repository.circle.CircleRepository;
import net.causw.adapter.persistence.repository.notification.UserBoardSubscribeRepository;
import net.causw.adapter.persistence.repository.post.PostRepository;
import net.causw.adapter.persistence.repository.user.UserRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.application.dto.board.*;
import net.causw.application.dto.post.PostContentDto;
import net.causw.application.dto.util.dtoMapper.BoardDtoMapper;
import net.causw.application.dto.util.dtoMapper.CircleDtoMapper;
import net.causw.application.dto.util.dtoMapper.PostDtoMapper;
import net.causw.application.dto.util.dtoMapper.UserDtoMapper;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.board.BoardApplyStatus;
import net.causw.domain.model.enums.circle.CircleMemberStatus;
import net.causw.domain.model.enums.user.Role;
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
@MeasureTime
@Service
@RequiredArgsConstructor
public class BoardService {
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final CircleRepository circleRepository;
    private final CircleMemberRepository circleMemberRepository;
    private final UserBoardSubscribeRepository userBoardSubscribeRepository;
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
                .toList();

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
                            .map(PostDtoMapper.INSTANCE::toPostContentDto)
                            .collect(Collectors.toList());
                    return BoardDtoMapper.INSTANCE.toBoardMainResponseDto(board, recentPosts);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BoardNameCheckResponseDto checkBoardName (
            BoardNameCheckRequestDto boardNameCheckRequestDto
    ) {
        String boardName = boardNameCheckRequestDto.getName();

        return BoardDtoMapper.INSTANCE.toBoardNameCheckResponseDto(boardRepository.existsByName(boardName));
    }

    @Transactional
    public void applyNormalBoard(
            User creator,
            NormalBoardApplyRequestDto normalBoardApplyRequestDto
    ) {
        if (boardRepository.existsByName(normalBoardApplyRequestDto.getBoardName())) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    MessageUtil.BOARD_NAME_ALREADY_EXISTS
            );
        }

        Circle circle = ( normalBoardApplyRequestDto.getCircleId() == null || normalBoardApplyRequestDto.getCircleId().isEmpty() ) ?
                null :
                getCircle(normalBoardApplyRequestDto.getCircleId());

        if (creator.getRoles().contains(Role.ADMIN) ||
                creator.getRoles().contains(Role.PRESIDENT) ||
                creator.getRoles().contains(Role.VICE_PRESIDENT)
        ) {
            List<String> createRoleList = new ArrayList<>();
            createRoleList.add("ALL"); // 일반 사용자의 게시판 신청은 항상 글 작성 권한이 '상관없음'임
            Board newBoard = Board.of(
                    normalBoardApplyRequestDto.getBoardName(),
                    normalBoardApplyRequestDto.getDescription(),
                    createRoleList,
                    StaticValue.BOARD_NAME_APP_FREE,
                    normalBoardApplyRequestDto.getIsAnonymousAllowed(),
                    circle
            );

            boardRepository.save(newBoard);

            return;
        }

        BoardApply newBoardApply = BoardApply.of(
                creator,
                normalBoardApplyRequestDto.getBoardName(),
                normalBoardApplyRequestDto.getDescription(),
                StaticValue.BOARD_NAME_APP_FREE,
                normalBoardApplyRequestDto.getIsAnonymousAllowed(),
                circle
        );

        boardApplyRepository.save(newBoardApply);
    }

    @Transactional
    public BoardResponseDto createBoard(
            User creator,
            BoardCreateRequestDto boardCreateRequestDto
    ) {
        if (boardRepository.existsByName(boardCreateRequestDto.getBoardName())) {
            throw new BadRequestException(
                    ErrorCode.ROW_ALREADY_EXIST,
                    MessageUtil.BOARD_NAME_ALREADY_EXISTS
            );
        }

        if ( !(boardCreateRequestDto.getBoardCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE) ||
            boardCreateRequestDto.getBoardCategory().equals(StaticValue.BOARD_NAME_APP_FREE))
        ) {
            throw new BadRequestException(
                    ErrorCode.INVALID_BOARD_CATEGORY,
                    MessageUtil.INVALID_BOARD_CATEGORY
            );
        }


        Board newBoard = Board.of(
                boardCreateRequestDto.getBoardName(),
                boardCreateRequestDto.getDescription(),
                boardCreateRequestDto.getCreateRoleList(),
                boardCreateRequestDto.getBoardCategory(),
                boardCreateRequestDto.getIsAnonymousAllowed(),
                ( boardCreateRequestDto.getCircleId() == null || boardCreateRequestDto.getCircleId().isEmpty() ) ?
                        null :
                        getCircle(boardCreateRequestDto.getCircleId())
        );

        BoardResponseDto boardResponseDto = toBoardResponseDto(boardRepository.save(newBoard), creator.getRoles());

        createBoardSubscribe(newBoard.getId());

        return boardResponseDto;
    }

    @Transactional(readOnly = true)
    public List<NormalBoardAppliesResponseDto> findAllBoardApply() {
        // 관리자, 학생회장, 부학생회장만 게시판 관리 기능 사용 가능
        return this.boardApplyRepository.findAllByAcceptStatus(BoardApplyStatus.AWAIT)
                .stream()
                .map(BoardDtoMapper.INSTANCE::toNormalBoardAppliesResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NormalBoardApplyResponseDto findBoardApplyByApplyId(String applyId) {
        // 관리자, 학생회장, 부학생회장만 게시판 관리 기능 사용 가능
        BoardApply boardApply = this.boardApplyRepository.findById(applyId)
                .orElseThrow(() -> new BadRequestException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.APPLY_NOT_FOUND
                ));

        return BoardDtoMapper.INSTANCE.toNormalBoardApplyResponseDto(
                boardApply,
                UserDtoMapper.INSTANCE.toUserResponseDto(boardApply.getUser(), null, null),
                boardApply.getCircle() == null ? null : CircleDtoMapper.INSTANCE.toCircleResponseDto(boardApply.getCircle())
        );
    }

    @Transactional
    public NormalBoardApplyResponseDto accept(String boardApplyId) {
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

        List<String> createRoleList = new ArrayList<>(Arrays.stream(boardApply.getCreateRoles().split(",")).toList());
        // TODO: Board.of 메서드에서 정상적으로 createRole 받을 수 있도록 수정 필요
        //createRoleList.add("ALL"); // 일반 사용자의 게시판 신청은 항상 글 작성 권한이 '상관없음'임 -> Board.of에서 처리

        Board newBoard = Board.of(
                boardApply.getBoardName(),
                boardApply.getDescription(),
                createRoleList,
                boardApply.getCategory(),
                boardApply.getIsAnonymousAllowed(),
                boardApply.getCircle() == null ? null : getCircle(boardApply.getCircle().getId())
        );

        this.boardRepository.save(newBoard);

        return BoardDtoMapper.INSTANCE.toNormalBoardApplyResponseDto(
                boardApply,
                UserDtoMapper.INSTANCE.toUserResponseDto(boardApply.getUser(), null, null),
                boardApply.getCircle() == null ? null : CircleDtoMapper.INSTANCE.toCircleResponseDto(boardApply.getCircle())
        );
    }

    @Transactional
    public NormalBoardApplyResponseDto reject(String boardApplyId) {
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

        return BoardDtoMapper.INSTANCE.toNormalBoardApplyResponseDto(
                boardApply,
                UserDtoMapper.INSTANCE.toUserResponseDto(boardApply.getUser(), null, null),
                boardApply.getCircle() == null ? null : CircleDtoMapper.INSTANCE.toCircleResponseDto(boardApply.getCircle())
        );
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
        return BoardDtoMapper.INSTANCE.toBoardResponseDto(
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

    @Transactional
    public void createBoardSubscribe(String boardId) {
        // 공지게시판을 만들면 무조건 on으로 설정하게하고
        // 일반 게시판은 굳이 이걸 호출할 필요 없음(생성자도 굳이 할필요 없게 하자)
        // 관리자용 api로 추후에는 공지게시판이 있으면 자동으로 호출되게 하는게 좋을듯
        // 공지게시판은 이 메서드로 한번에 관리를 한다고 치고, 나머지 일반 게시판은 그냥 없으면
        Board board = getBoard(boardId);
        List<User> allUsers = userRepository.findAll();

        List<UserBoardSubscribe> subscriptions = allUsers.stream()
                .map(user -> UserBoardSubscribe.of(user, board, true))
                .collect(Collectors.toList());

        userBoardSubscribeRepository.saveAll(subscriptions);
    }

    //공지 게시판은 무조건 디폴트로 on이어야 하고, 나머지는 그냥 디폴트로 off 여야함
    /*
    하지만 지금 상황은 결국엔 기존 게시판이 있기 때문에 무조건 off 가 아니라 구독 기능이 추가될 때 사용자 전원이 매핑되게 할 필요가 있음
    그럼 그냥 아예 없으면(굳이 사용자가 억지로 키지 않으면) false
    But 공지게시판은 무조건 true로 만들기 (위 createBoardSubscribe를 관리자용 api로 일단 저장해두면 될 것 같음)? 을 할게 아니라
    그냥 공지게시판은 무조건 보내지게 하는것보다 걍 자동으로 공지게시판이랑 모든 유저가 다 매핑되게 하는 api를 만드는게 좋을듯
    일반 게시판은 매핑테이블에 정보가 없으면 그냥 알람이 꺼진 상태이고 만약에 update가 들어왔을때 없다면 그때 생성하게
    * */

    @Transactional
    public BoardSubscribeResponseDto updateBoardSubscribe(User user, String boardId) {
        Board board = getBoard(boardId);

        UserBoardSubscribe subscription = userBoardSubscribeRepository.findByUserAndBoard(user, board)
                .map(existing -> {
                    existing.toggle();
                    return existing;
                })
                .orElseGet(() -> userBoardSubscribeRepository.save(UserBoardSubscribe.of(user, board, true)));

        return BoardDtoMapper.INSTANCE.toBoardSubscribeResponseDto(subscription);
    }

}
