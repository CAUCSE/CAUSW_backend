package net.causw.app.main.domain.community.board.service.v1;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.domain.campus.circle.api.v1.dto.CircleResponseDto;
import net.causw.app.main.domain.campus.circle.api.v1.mapper.CircleDtoMapper;
import net.causw.app.main.domain.campus.circle.entity.Circle;
import net.causw.app.main.domain.campus.circle.entity.CircleMember;
import net.causw.app.main.domain.campus.circle.enums.CircleMemberStatus;
import net.causw.app.main.domain.campus.circle.repository.CircleMemberRepository;
import net.causw.app.main.domain.campus.circle.repository.CircleRepository;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardAppliesResponseDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardApplyRequestDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardApplyResponseDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardCreateRequestDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardMainResponseDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardNameCheckRequestDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardNameCheckResponseDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardResponseDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardSubscribeResponseDto;
import net.causw.app.main.domain.community.board.api.v1.dto.BoardUpdateRequestDto;
import net.causw.app.main.domain.community.board.api.v1.mapper.BoardDtoMapper;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardApply;
import net.causw.app.main.domain.community.board.entity.BoardApplyStatus;
import net.causw.app.main.domain.community.board.repository.BoardApplyRepository;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.community.post.api.v1.dto.PostContentDto;
import net.causw.app.main.domain.community.post.api.v1.mapper.PostDtoV1Mapper;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.post.service.v1.PostEntityService;
import net.causw.app.main.domain.community.post.service.v1.PostV1Service;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.academic.event.AcademicStatusChangeEvent;
import net.causw.app.main.domain.user.academic.event.CertifiedUserCreatedEvent;
import net.causw.app.main.domain.user.account.api.v1.dto.UserResponseDto;
import net.causw.app.main.domain.user.account.api.v1.mapper.UserDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.RoleGroup;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.app.main.domain.user.account.util.UserRoleIsNoneValidator;
import net.causw.app.main.domain.user.account.util.UserRoleValidator;
import net.causw.app.main.domain.user.account.util.UserStateValidator;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.util.ConstraintValidator;
import net.causw.app.main.shared.util.TargetIsDeletedValidator;
import net.causw.app.main.shared.util.TargetIsNotDeletedValidator;
import net.causw.app.main.shared.util.UserEqualValidator;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.UnauthorizedException;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@MeasureTime
@Service
@RequiredArgsConstructor
public class BoardV1Service {
	private final PostRepository postRepository;
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	private final CircleRepository circleRepository;
	private final CircleMemberRepository circleMemberRepository;
	private final UserBoardSubscribeRepository userBoardSubscribeRepository;
	private final BoardApplyRepository boardApplyRepository;
	private final Validator validator;
	private final PostV1Service postV1Service;
	private final UserBlockEntityService userBlockEntityService;
	private final PostEntityService postEntityService;

	@Transactional(readOnly = true)
	public List<BoardResponseDto> findAllBoard(
		User user) {
		Set<Role> roles = user.getRoles();
		AcademicStatus academicStatus = user.getAcademicStatus();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.validate();

		if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
			return boardRepository.findByOrderByCreatedAtAsc().stream()
				.map(board -> toBoardResponseDto(board, roles))
				.collect(Collectors.toList());
		} else if (academicStatus.equals(AcademicStatus.GRADUATED)) {
			return boardRepository.findByIsAlumniTrueAndIsDeletedFalseOrderByCreatedAtAsc().stream()
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
		User user) {
		Set<Role> roles = user.getRoles();
		AcademicStatus academicStatus = user.getAcademicStatus();

		ValidatorBucket.of()
			.consistOf(UserStateValidator.of(user.getState()))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.validate();

		List<Board> boards;

		if (roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT)) {
			boards = boardRepository.findByOrderByCreatedAtAsc();
		} else if (academicStatus.equals(AcademicStatus.GRADUATED)) {
			boards = boardRepository.findByIsAlumniTrueAndIsDeletedFalseOrderByCreatedAtAsc();
		} else {
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
					boardRepository.findByCircle_IdInAndIsDeletedFalseOrderByCreatedAtAsc(circleIdList).stream())
					.collect(Collectors.toList());
			}
		}

		Set<String> blockedUserIds = userBlockEntityService.findBlockeeUserIdsByBlocker(user);

		return boards.stream()
			.map(board -> {
				List<PostContentDto> recentPosts = postEntityService.findPostsByBoardWithFilters(
					board.getId(),
					false,
					blockedUserIds,
					null,
					PageRequest.of(0, 2))
					.getContent()
					.stream()
					.map(post -> {
						PostContentDto postContentDto = PostDtoV1Mapper.INSTANCE.toPostContentDto(post);
						postContentDto.updateAnonymousPostContent();

						// 화면에 표시될 작성자 닉네임 설정
						User writer = post.getWriter();
						postContentDto.setDisplayWriterNickname(
							postV1Service.getDisplayWriterNickname(writer, postContentDto.getIsAnonymous(),
								postContentDto.getWriterNickname()));

						return postContentDto;
					})
					.collect(Collectors.toList());
				return BoardDtoMapper.INSTANCE.toBoardMainResponseDto(board, recentPosts);
			})
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public BoardNameCheckResponseDto checkBoardName(
		BoardNameCheckRequestDto boardNameCheckRequestDto) {
		String boardName = boardNameCheckRequestDto.getName();

		return BoardDtoMapper.INSTANCE.toBoardNameCheckResponseDto(boardRepository.existsByName(boardName));
	}

	@Transactional
	public void applyBoard(
		User creator,
		BoardApplyRequestDto boardApplyRequestDto) {
		if (boardRepository.existsByName(boardApplyRequestDto.getBoardName())) {
			throw new BadRequestException(
				ErrorCode.ROW_ALREADY_EXIST,
				MessageUtil.BOARD_NAME_ALREADY_EXISTS);
		}

		BoardApply newBoardApply = BoardApply.of(
			creator,
			boardApplyRequestDto.getBoardName(),
			boardApplyRequestDto.getDescription(),
			StaticValue.BOARD_NAME_APP_FREE,
			boardApplyRequestDto.getIsAnonymousAllowed(),
			getCircle(boardApplyRequestDto.getCircleId()));

		boardApplyRepository.save(newBoardApply);
	}

	@Transactional
	public BoardResponseDto createNoticeBoard(
		User creator,
		BoardCreateRequestDto boardCreateRequestDto) {
		if (boardRepository.existsByName(boardCreateRequestDto.getBoardName())) {
			throw new BadRequestException(
				ErrorCode.ROW_ALREADY_EXIST,
				MessageUtil.BOARD_NAME_ALREADY_EXISTS);
		}

		if (!StaticValue.BOARD_NAME_APP_NOTICE.equals(boardCreateRequestDto.getBoardCategory())) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.INVALID_BOARD_CATEGORY);
		}

		Board newBoard = boardRepository.save(
			Board.createNoticeBoard(
				boardCreateRequestDto.getBoardName(),
				boardCreateRequestDto.getDescription(),
				boardCreateRequestDto.getCreateRoleList(),
				boardCreateRequestDto.getBoardCategory(),
				boardCreateRequestDto.getIsAnonymousAllowed(),
				boardCreateRequestDto.getIsAlumni(),
				getCircle(boardCreateRequestDto.getCircleId())));

		createBoardSubscribe(newBoard.getId());

		return toBoardResponseDto(newBoard, creator.getRoles());
	}

	@Transactional(readOnly = true)
	public List<BoardAppliesResponseDto> findAllBoardApply() {
		// 관리자, 학생회장, 부학생회장만 게시판 관리 기능 사용 가능
		return this.boardApplyRepository.findAllByAcceptStatus(BoardApplyStatus.AWAIT)
			.stream()
			.map(BoardDtoMapper.INSTANCE::toBoardAppliesResponseDto)
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public BoardApplyResponseDto findBoardApplyByApplyId(String applyId) {
		// 관리자, 학생회장, 부학생회장만 게시판 관리 기능 사용 가능
		BoardApply boardApply = this.boardApplyRepository.findById(applyId)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.APPLY_NOT_FOUND));

		return toBoardApplyResponseDto(boardApply);
	}

	@Transactional
	public BoardApplyResponseDto accept(String boardApplyId) {
		BoardApply boardApply = this.boardApplyRepository.findById(boardApplyId)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.APPLY_NOT_FOUND));

		if (boardApply.getAcceptStatus() == BoardApplyStatus.ACCEPTED) { // 해당 신청이 이미 승인된 경우
			throw new BadRequestException(
				ErrorCode.CANNOT_PERFORMED,
				MessageUtil.APPLY_ALREADY_ACCEPTED);
		}

		if (boardApply.getAcceptStatus() == BoardApplyStatus.REJECT) { // 해당 신청이 이미 거부된 경우
			throw new BadRequestException(
				ErrorCode.CANNOT_PERFORMED,
				MessageUtil.APPLY_ALREADY_REJECTED);
		}

		// 게시판명 중복 체크
		if (boardRepository.existsByName(boardApply.getBoardName())) {
			throw new BadRequestException(
				ErrorCode.ROW_ALREADY_EXIST,
				MessageUtil.BOARD_NAME_ALREADY_EXISTS);
		}

		boardApply.updateAcceptStatus(BoardApplyStatus.ACCEPTED); // 해당 boardApply의 상태를 ACCEPTED로 변경
		this.boardApplyRepository.save(boardApply);

		Board newBoard = Board.of( // 일반 게시판 생성
			boardApply.getBoardName(),
			boardApply.getDescription(),
			boardApply.getCategory(),
			boardApply.getIsAnonymousAllowed(),
			Optional.ofNullable(boardApply.getCircle())
				.map(circle -> getCircle(circle.getId()))
				.orElse(null));

		this.boardRepository.save(newBoard);

		return toBoardApplyResponseDto(boardApply);
	}

	@Transactional
	public BoardApplyResponseDto reject(String boardApplyId) {
		BoardApply boardApply = this.boardApplyRepository.findById(boardApplyId)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.APPLY_NOT_FOUND));

		if (boardApply.getAcceptStatus() == BoardApplyStatus.ACCEPTED) { // 해당 신청이 이미 승인된 경우
			throw new BadRequestException(
				ErrorCode.CANNOT_PERFORMED,
				MessageUtil.APPLY_ALREADY_ACCEPTED);
		}

		if (boardApply.getAcceptStatus() == BoardApplyStatus.REJECT) { // 해당 신청이 이미 거부된 경우
			throw new BadRequestException(
				ErrorCode.CANNOT_PERFORMED,
				MessageUtil.APPLY_ALREADY_REJECTED);
		}

		boardApply.updateAcceptStatus(BoardApplyStatus.REJECT); // 해당 boardApply의 상태를 REJECT로 변경
		this.boardApplyRepository.save(boardApply);

		return toBoardApplyResponseDto(boardApply);
	}

	@Transactional
	public BoardResponseDto updateBoard(
		User updater,
		String boardId,
		BoardUpdateRequestDto boardUpdateRequestDto) {
		Set<Role> roles = updater.getRoles();
		Board board = getBoard(boardId);

		ValidatorBucket validatorBucket = initializeValidatorBucket(updater, board);

		board.update(
			boardUpdateRequestDto.getName(),
			boardUpdateRequestDto.getDescription(),
			String.join(",", boardUpdateRequestDto.getCreateRoleList()),
			boardUpdateRequestDto.getCategory());

		validatorBucket
			.consistOf(ConstraintValidator.of(board, this.validator))
			.validate();

		return toBoardResponseDto(boardRepository.save(board), roles);
	}

	@Transactional
	public BoardResponseDto deleteBoard(
		User deleter,
		String boardId) {
		Set<Role> roles = deleter.getRoles();
		Board board = getBoard(boardId);

		ValidatorBucket validatorBucket = initializeValidatorBucket(deleter, board);
		if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			validatorBucket
				.consistOf(UserRoleValidator.of(
					roles,
					Set.of()));
		}
		validatorBucket.validate();

		board.setIsDeleted(true);

		// 해당 게시판의 모든 게시글을 삭제 처리
		postRepository.deleteAllPostsByBoardId(boardId);

		return toBoardResponseDto(boardRepository.save(board), roles);
	}

	@Transactional
	public BoardResponseDto restoreBoard(
		User restorer,
		String boardId) {
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
									MessageUtil.NOT_CIRCLE_LEADER)),
							restorer.getId()));
				}
			},
			() -> validatorBucket
				.consistOf(UserRoleValidator.of(roles, Set.of())));

		if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			validatorBucket
				.consistOf(UserRoleValidator.of(
					roles,
					Set.of()));
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
									MessageUtil.NOT_CIRCLE_LEADER)),
							user.getId()));
				}
			},
			() -> validatorBucket
				.consistOf(UserRoleValidator.of(roles, Set.of())));
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
			circleName);
	}

	private BoardApplyResponseDto toBoardApplyResponseDto(BoardApply boardApply) {
		UserResponseDto userResponseDto = UserDtoMapper.INSTANCE.toUserResponseDto(boardApply.getUser(), null, null);
		CircleResponseDto circleResponseDto = Optional.ofNullable(boardApply.getCircle())
			.map(CircleDtoMapper.INSTANCE::toCircleResponseDto)
			.orElse(null);

		return BoardDtoMapper.INSTANCE.toBoardApplyResponseDto(
			boardApply,
			userResponseDto,
			circleResponseDto);
	}

	private User getUser(String userId) {
		return userRepository.findById(userId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.USER_NOT_FOUND));
	}

	private Board getBoard(String boardId) {
		return boardRepository.findById(boardId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.BOARD_NOT_FOUND));
	}

	private Circle getCircle(String circleId) {
		if (circleId == null || circleId.isEmpty()) {
			return null;
		}

		return circleRepository.findById(circleId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.SMALL_CLUB_NOT_FOUND));
	}

	@Transactional
	public void createBoardSubscribe(String boardId) {
		Board board = getBoard(boardId);

		// 게시판 접근 권한이 있는 인증 사용자 필터링
		List<User> certifiedUsers = userRepository.findAllByState(UserState.ACTIVE).stream()
			.filter(user -> !AcademicStatus.UNDETERMINED.equals(user.getAcademicStatus()) // 학적 인증이 완료된 일반 사용자
				|| RoleGroup.EXECUTIVES_AND_PROFESSOR.getRoles().stream() // 집행부/교수 역할의 사용자
					.anyMatch(user.getRoles()::contains))
			.toList();

		// 구독 생성
		List<UserBoardSubscribe> subscriptions = certifiedUsers.stream()
			.filter(user -> board.getIsAlumni() // 동문회 허용 게시판인 경우 졸업생 구독 허용
				|| user.getAcademicStatus() != AcademicStatus.GRADUATED)
			.map(user -> UserBoardSubscribe.of(user, board, true))
			.collect(Collectors.toList());

		userBoardSubscribeRepository.saveAll(subscriptions);
	}

	@Transactional
	public BoardSubscribeResponseDto setBoardSubscribe(User user, String boardId, Boolean isSubscribed) {
		Board board = getBoard(boardId);

		UserBoardSubscribe subscription = userBoardSubscribeRepository.findByUserAndBoard(user, board)
			.map(existing -> {
				existing.setIsSubscribed(isSubscribed);
				return existing;
			})
			.orElseGet(() -> userBoardSubscribeRepository.save(UserBoardSubscribe.of(user, board, isSubscribed)));

		return BoardDtoMapper.INSTANCE.toBoardSubscribeResponseDto(subscription);
	}

	// 학적 인증 변경사항 커밋 후, 새로운 트랜잭션 시작
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void createNoticeBoardsSubscribe(CertifiedUserCreatedEvent event) {
		User user = getUser(event.userId());

		List<Board> noticeBoards = boardRepository.findAllByCategory(StaticValue.BOARD_NAME_APP_NOTICE);

		// 구독중인 공지 게시판 id 조회
		Set<String> subscribedNoticeBoardIds = userBoardSubscribeRepository.findByUserAndBoardIn(user, noticeBoards)
			.stream()
			.map(subscribe -> subscribe.getBoard().getId())
			.collect(Collectors.toSet());

		// 구독 생성
		List<UserBoardSubscribe> newSubscriptions = noticeBoards.stream()
			.filter(board -> !subscribedNoticeBoardIds.contains(board.getId())) // 이미 구독중인 게시판 제외
			.filter(board -> board.getIsAlumni() // 졸업생인 경우 동문회 허용 게시판만 구독
				|| user.getAcademicStatus() != AcademicStatus.GRADUATED)
			.map(board -> UserBoardSubscribe.of(user, board, true))
			.toList();

		userBoardSubscribeRepository.saveAll(newSubscriptions);
	}

	// 학적 인증 변경사항 커밋 후, 새로운 트랜잭션 시작
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void updateBoardsSubscribe(AcademicStatusChangeEvent event) {
		if (event.oldStatus() != AcademicStatus.GRADUATED
			&& event.newStatus() == AcademicStatus.GRADUATED) {

			User user = getUser(event.userId());

			// 졸업생이 된 경우, 동문회 게시판 외 구독 취소
			userBoardSubscribeRepository.deleteAllByUserAndBoard_IsAlumniFalse(user);
		}
	}
}
