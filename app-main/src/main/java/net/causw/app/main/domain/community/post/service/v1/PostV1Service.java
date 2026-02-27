package net.causw.app.main.domain.community.post.service.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.core.aop.annotation.MeasureTime;
import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.repository.PostAttachImageRepository;
import net.causw.app.main.domain.asset.file.service.v1.UuidFileV1Service;
import net.causw.app.main.domain.campus.circle.entity.Circle;
import net.causw.app.main.domain.campus.circle.entity.CircleMember;
import net.causw.app.main.domain.campus.circle.enums.CircleMemberStatus;
import net.causw.app.main.domain.campus.circle.repository.CircleMemberRepository;
import net.causw.app.main.domain.campus.circle.util.CircleMemberStatusValidator;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.community.board.repository.FavoriteBoardRepository;
import net.causw.app.main.domain.community.comment.repository.CommentRepository;
import net.causw.app.main.domain.community.comment.util.PostNumberOfAttachmentsValidator;
import net.causw.app.main.domain.community.form.api.v1.dto.request.create.FormCreateRequestDto;
import net.causw.app.main.domain.community.form.api.v1.dto.request.create.QuestionCreateRequestDto;
import net.causw.app.main.domain.community.form.api.v1.dto.response.FormResponseDto;
import net.causw.app.main.domain.community.form.api.v1.dto.response.OptionResponseDto;
import net.causw.app.main.domain.community.form.api.v1.dto.response.QuestionResponseDto;
import net.causw.app.main.domain.community.form.api.v1.mapper.FormDtoMapper;
import net.causw.app.main.domain.community.form.entity.Form;
import net.causw.app.main.domain.community.form.entity.FormQuestion;
import net.causw.app.main.domain.community.form.entity.FormQuestionOption;
import net.causw.app.main.domain.community.form.enums.QuestionType;
import net.causw.app.main.domain.community.form.repository.FormRepository;
import net.causw.app.main.domain.community.post.api.v1.dto.BoardPostsResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostCreateRequestDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostCreateResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostCreateWithFormRequestDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostSubscribeResponseDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostUpdateRequestDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostUpdateWithFormRequestDto;
import net.causw.app.main.domain.community.post.api.v1.dto.PostsResponseDto;
import net.causw.app.main.domain.community.post.api.v1.mapper.PostDtoV1Mapper;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.post.repository.query.PostQueryRepository;
import net.causw.app.main.domain.community.reaction.entity.FavoritePost;
import net.causw.app.main.domain.community.reaction.entity.LikePost;
import net.causw.app.main.domain.community.reaction.repository.FavoritePostRepository;
import net.causw.app.main.domain.community.reaction.repository.LikePostRepository;
import net.causw.app.main.domain.community.vote.api.v1.dto.VoteOptionResponseDto;
import net.causw.app.main.domain.community.vote.api.v1.dto.VoteResponseDto;
import net.causw.app.main.domain.community.vote.api.v1.mapper.VoteDtoMapper;
import net.causw.app.main.domain.community.vote.entity.Vote;
import net.causw.app.main.domain.community.vote.entity.VoteOption;
import net.causw.app.main.domain.community.vote.entity.VoteRecord;
import net.causw.app.main.domain.community.vote.repository.VoteRecordRepository;
import net.causw.app.main.domain.notification.notification.entity.UserBoardSubscribe;
import net.causw.app.main.domain.notification.notification.entity.UserPostSubscribe;
import net.causw.app.main.domain.notification.notification.repository.UserBoardSubscribeRepository;
import net.causw.app.main.domain.notification.notification.repository.UserPostSubscribeRepository;
import net.causw.app.main.domain.notification.notification.service.v1.BoardNotificationService;
import net.causw.app.main.domain.user.account.api.v1.dto.UserResponseDto;
import net.causw.app.main.domain.user.account.api.v1.mapper.UserDtoMapper;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.util.UserRoleIsNoneValidator;
import net.causw.app.main.domain.user.account.util.UserRoleValidator;
import net.causw.app.main.domain.user.account.util.UserStateIsDeletedValidator;
import net.causw.app.main.domain.user.account.util.UserStateValidator;
import net.causw.app.main.domain.user.relation.service.v1.UserBlockEntityService;
import net.causw.app.main.shared.StatusPolicy;
import net.causw.app.main.shared.ValidatorBucket;
import net.causw.app.main.shared.pageable.PageableFactory;
import net.causw.app.main.shared.util.ConstraintValidator;
import net.causw.app.main.shared.util.ContentsAdminValidator;
import net.causw.app.main.shared.util.TargetIsDeletedValidator;
import net.causw.app.main.shared.util.TargetIsNotDeletedValidator;
import net.causw.app.main.shared.util.UserEqualValidator;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.ForbiddenException;
import net.causw.global.exception.InternalServerException;
import net.causw.global.exception.UnauthorizedException;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

@MeasureTime
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostV1Service {

	private final PostQueryRepository postQueryRepository;
	private final PostRepository postRepository;
	private final BoardRepository boardRepository;
	private final CircleMemberRepository circleMemberRepository;
	private final VoteRecordRepository voteRecordRepository;
	private final CommentRepository commentRepository;
	private final FavoriteBoardRepository favoriteBoardRepository;
	private final LikePostRepository likePostRepository;
	private final FavoritePostRepository favoritePostRepository;
	private final PageableFactory pageableFactory;
	private final Validator validator;
	private final UuidFileV1Service uuidFileService;
	private final PostAttachImageRepository postAttachImageRepository;
	private final FormRepository formRepository;
	private final BoardNotificationService boardNotificationService;
	private final UserBoardSubscribeRepository userBoardSubscribeRepository;
	private final UserPostSubscribeRepository userPostSubscribeRepository;
	private final UserBlockEntityService userBlockEntityService;
	private final PostEntityService postEntityService;

	public PostResponseDto findPostById(User user, String postId) {
		Post post = getPost(postId);

		ValidatorBucket validatorBucket = initializeValidator(user, post.getBoard());
		validatorBucket.consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));
		validatorBucket.validate();

		boolean existsBlockByUsers = userBlockEntityService.existsBlockByUsers(user, post.getWriter());
		if (existsBlockByUsers) {
			throw new ForbiddenException(ErrorCode.BLOCKED_USERS_CONTENT, MessageUtil.BLOCKED_USERS_CONTENT);
		}

		PostResponseDto postResponseDto = toPostResponseDtoExtended(post, user);
		if (postResponseDto.getIsAnonymous()) {
			postResponseDto.updateAnonymousPost();
		}

		return postResponseDto;
	}

	/**
	 * 특정 게시판의 게시글 목록 조회 (페이징, 검색 지원)
	 * @param user 조회하는 사용자
	 * @param boardId 게시판 ID
	 * @param keyword 검색 키워드 (제목, 내용 검색)
	 * @param pageNum 페이지 번호
	 * @return 게시판 정보와 게시글 목록
	 */
	public BoardPostsResponseDto findAllPost(
		User user,
		String boardId,
		String keyword,
		Integer pageNum) {
		Set<Role> roles = user.getRoles(); // 사용자의 역할 가져오기
		Board board = getBoard(boardId); // 게시판 정보 가져오기

		// 유효성 검사 초기화 및 실행
		ValidatorBucket validatorBucket = initializeValidator(user, board);
		validatorBucket
			.consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD)).validate();

		// 동아리 리더 여부 확인
		// 동아리 리더는 삭제된 게시글도 볼 수 있음
		boolean isCircleLeader = false;
		if (roles.contains(Role.LEADER_CIRCLE)) {
			isCircleLeader = getCircleLeader(board.getCircle()).getId().equals(user.getId());
		}

		// 차단한 사용자 목록 조회 (차단한 사용자의 게시글 제외)
		Set<String> blockedUserIds = userBlockEntityService.findBlockeeUserIdsByBlocker(user);

		// 페이징 객체 생성
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE);

		// 삭제된 게시글 포함 여부 결정
		// 관리자, 학생회장, 동아리 리더는 삭제된 게시글도 볼 수 있음
		boolean includeDeleted = isCircleLeader || roles.contains(Role.ADMIN) || roles.contains(Role.PRESIDENT);

		// 게시글 목록 조회 (필터링 및 페이징 적용)
		Page<PostsResponseDto> posts = postQueryRepository
			.findPostsByBoardWithFilters(boardId, includeDeleted, blockedUserIds, keyword, pageable)
			.map(PostDtoV1Mapper.INSTANCE::toPostsResponseDto);

		// 게시판 정보와 게시글 목록을 DTO로 변환
		return toBoardPostsResponseDto(
			board,
			roles,
			isFavorite(user.getId(), board.getId()), // 즐겨찾기 여부
			isBoardSubscribed(user, board), // 게시판 구독 여부
			posts);
	}

	public BoardPostsResponseDto findAllAppNotice(User user, Integer pageNum) {
		Set<Role> roles = user.getRoles();
		Board board = boardRepository.findAppNotice().orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.BOARD_NOT_FOUND));

		Set<String> blockedUserIds = userBlockEntityService.findBlockeeUserIdsByBlocker(user);
		Pageable pageable = pageableFactory.create(pageNum, StaticValue.DEFAULT_POST_PAGE_SIZE);

		boolean includeDeleted = false;
		Page<PostsResponseDto> posts = postQueryRepository
			.findPostsByBoardWithFilters(board.getId(), includeDeleted, blockedUserIds, null, pageable)
			.map(PostDtoV1Mapper.INSTANCE::toPostsResponseDto);

		return toBoardPostsResponseDto(
			board,
			roles,
			isFavorite(user.getId(), board.getId()),
			isBoardSubscribed(user, board),
			posts);
	}

	/**
	 * 게시글 생성
	 *
	 * 처리 과정:
	 * 1. 게시글 작성 권한 검증 (역할, 동아리 멤버 여부 등)
	 * 2. 게시글 저장
	 * 3. 작성자를 게시글 구독자로 등록
	 * 4. 게시판 구독자들에게 알림 전송 (비동기)
	 *
	 * @param creator 게시글 작성자
	 * @param postCreateRequestDto 게시글 생성 요청 정보 (제목, 내용, 익명 여부 등)
	 * @param attachImageList 첨부 이미지 목록
	 * @return 생성된 게시글 정보
	 */
	//게시글이 생성될 때 발생할 일
	//1. 게시글과 작성자의 구독 매핑 만들기
	//2. 이 게시글이 작성된 게시판의 구독자 확인하기
	//3. 이 게시판의 구독자들에게 sendByBoardIsSubscribed로 푸시 알림 보내기 & 서비스 알람 로그 저장하기
	@Transactional
	public PostCreateResponseDto createPost(User creator, PostCreateRequestDto postCreateRequestDto,
		List<MultipartFile> attachImageList) {
		Set<Role> roles = creator.getRoles();

		// 게시판 조회
		Board board = getBoard(postCreateRequestDto.getBoardId());
		// 게시글 작성 가능한 역할 목록 파싱
		List<String> createRoles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));

		// 첨부 이미지 저장
		List<UuidFile> uuidFileList = (attachImageList == null || attachImageList.isEmpty()) ? new ArrayList<>()
			: uuidFileService.saveFileList(attachImageList, FilePath.POST);

		// 게시글 엔티티 생성
		Post post = Post.of(
			postCreateRequestDto.getTitle(),
			postCreateRequestDto.getContent(),
			creator,
			postCreateRequestDto.getIsAnonymous(),
			postCreateRequestDto.getIsQuestion(),
			board,
			null,
			uuidFileList);

		// 익명 게시글 허용 여부 검증
		validateAnonymousAllowed(board, postCreateRequestDto.getIsAnonymous());

		// 유효성 검사 버킷 초기화
		ValidatorBucket validatorBucket = ValidatorBucket.of();
		validatorBucket
			.consistOf(UserStateValidator.of(creator)) // 사용자 상태 검증
			.consistOf(UserRoleIsNoneValidator.of(roles)) // 역할이 NONE인지 검증
			.consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD)) // 게시판 삭제 여부 검증
			.consistOf(UserRoleValidator.of( // 게시글 작성 권한 검증
				roles,
				createRoles.stream()
					.map(Role::of)
					.collect(Collectors.toSet())));

		// 동아리 게시판인 경우 추가 검증
		Optional<Circle> circles = Optional.ofNullable(board.getCircle());
		circles
			.filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(
				Role.VICE_PRESIDENT))
			.ifPresent(
				circle -> {
					// 동아리 멤버 조회
					CircleMember member = getCircleMember(creator.getId(), circle.getId());

					validatorBucket
						.consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE)) // 동아리 삭제 여부
						.consistOf(CircleMemberStatusValidator.of( // 동아리 멤버 상태 검증
							member.getStatus(),
							List.of(CircleMemberStatus.MEMBER)));

					// 동아리장이 COMMON 역할이 아닌 게시판에 작성하는 경우
					if (roles.contains(Role.LEADER_CIRCLE) && !createRoles.contains("COMMON")) {
						validatorBucket
							.consistOf(UserEqualValidator.of( // 동아리장 본인 확인
								getCircleLeader(circle).getId(),
								creator.getId()));
					}
				});
		validatorBucket
			.consistOf(ConstraintValidator.of(post, this.validator)) // Bean Validation 검증
			.validate();

		// 게시글 저장
		PostCreateResponseDto postCreateResponseDto = toPostCreateResponseDto(postRepository.save(post));

		// 1. 작성자를 게시글 구독자로 등록 (댓글 알림 받기 위함)
		createPostSubscribe(creator, post.getId());

		// 2. 게시판 구독자들에게 알림 전송 (비동기 처리)
		boardNotificationService.sendByBoardIsSubscribed(board, post);

		return postCreateResponseDto;
	}

	@Transactional
	public PostCreateResponseDto createPostWithForm(
		User creator,
		PostCreateWithFormRequestDto postCreateWithFormRequestDto,
		List<MultipartFile> attachImageList) {
		Set<Role> roles = creator.getRoles();

		Board board = getBoard(postCreateWithFormRequestDto.getBoardId());
		List<String> createRoles = new ArrayList<>(Arrays.asList(board.getCreateRoles().split(",")));

		List<UuidFile> uuidFileList = (attachImageList == null || attachImageList.isEmpty()) ? new ArrayList<>()
			: attachImageList.stream()
				.map(multipartFile -> uuidFileService.saveFile(multipartFile, FilePath.POST))
				.toList();

		Form form = generateForm(postCreateWithFormRequestDto.getFormCreateRequestDto());

		Post post = Post.of(
			postCreateWithFormRequestDto.getTitle(),
			postCreateWithFormRequestDto.getContent(),
			creator,
			postCreateWithFormRequestDto.getIsAnonymous(),
			postCreateWithFormRequestDto.getIsQuestion(),
			board,
			form,
			uuidFileList);

		validateAnonymousAllowed(board, postCreateWithFormRequestDto.getIsAnonymous());

		ValidatorBucket validatorBucket = ValidatorBucket.of();
		if (board.getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			validatorBucket
				.consistOf(UserRoleValidator.of(
					roles,
					Set.of()));
		}

		validatorBucket
			.consistOf(UserStateValidator.of(creator))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(PostNumberOfAttachmentsValidator.of(attachImageList))
			.consistOf(TargetIsDeletedValidator.of(board.getIsDeleted(), StaticValue.DOMAIN_BOARD))
			.consistOf(UserRoleValidator.of(
				roles,
				createRoles.stream()
					.map(String::trim) // 공백 제거
					.map(roleString -> {
						try {
							return Role.of(roleString);
						} catch (BadRequestException e) {
							// 잘못된 역할은 무시하고 로그 출력
							System.out.println("Invalid role found: " + roleString);
							return null;
						}
					})
					.filter(Objects::nonNull) // 유효한 역할만 수집
					.collect(Collectors.toSet())))
			.validate();

		Optional<Circle> circles = Optional.ofNullable(board.getCircle());
		circles
			.filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(
				Role.VICE_PRESIDENT))
			.ifPresent(
				circle -> {
					CircleMember member = getCircleMember(creator.getId(), circle.getId());

					validatorBucket
						.consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
						.consistOf(CircleMemberStatusValidator.of(
							member.getStatus(),
							List.of(CircleMemberStatus.MEMBER)));

					if (roles.contains(Role.LEADER_CIRCLE) && !createRoles.contains("COMMON")) {
						validatorBucket
							.consistOf(UserEqualValidator.of(
								getCircleLeader(circle).getId(),
								creator.getId()));
					}
				});
		validatorBucket
			.consistOf(ConstraintValidator.of(post, this.validator))
			.validate();

		return toPostCreateResponseDto(postRepository.save(post));
	}

	/**
	 * 게시글 삭제 (소프트 삭제)
	 * 실제로 데이터를 삭제하지 않고 isDeleted 플래그를 true로 설정
	 * @param deleter 삭제하는 사용자
	 * @param postId 삭제할 게시글 ID
	 */
	@Transactional
	public void deletePost(User deleter, String postId) {
		Post post = getPost(postId);
		Set<Role> roles = deleter.getRoles();

		ValidatorBucket validatorBucket = ValidatorBucket.of();
		// 앱 공지사항 게시판은 특별 권한 검증
		if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			// 관리자 역할이 없고, 게시글의 작성자가 아니면 오류 발생
			if (roles.stream()
				.noneMatch(role -> EnumSet.of(Role.ADMIN, Role.PRESIDENT, Role.VICE_PRESIDENT).contains(role))
				&& !post.getWriter().getId().equals(deleter.getId())) {
				throw new UnauthorizedException(
					ErrorCode.API_NOT_ALLOWED,
					"접근 권한이 없습니다.");
			}
		}
		validatorBucket
			.consistOf(UserStateValidator.of(deleter)) // 사용자 상태 검증
			.consistOf(UserRoleIsNoneValidator.of(roles)) // 역할이 NONE인지 검증
			.consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST)); // 이미 삭제되지 않았는지 검증

		// 동아리 게시판인 경우 추가 검증
		Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
		circles
			.filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(
				Role.VICE_PRESIDENT))
			.ifPresentOrElse(
				circle -> {
					CircleMember member = getCircleMember(deleter.getId(), circle.getId());

					validatorBucket
						.consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
						.consistOf(CircleMemberStatusValidator.of(
							member.getStatus(),
							List.of(CircleMemberStatus.MEMBER)))
						.consistOf(ContentsAdminValidator.of( // 작성자 또는 동아리장인지 검증
							roles,
							deleter.getId(),
							post.getWriter().getId(),
							List.of(Role.LEADER_CIRCLE)));

					// 동아리장이 다른 사람의 글을 삭제하는 경우
					if (roles.contains(Role.LEADER_CIRCLE) && !post.getWriter().getId().equals(deleter.getId())) {
						validatorBucket
							.consistOf(UserEqualValidator.of(
								getCircleLeader(circle).getId(),
								deleter.getId()));
					}
				},
				() -> validatorBucket
					.consistOf(ContentsAdminValidator.of(
						roles,
						deleter.getId(),
						post.getWriter().getId(),
						List.of())));
		validatorBucket.validate();

		// 소프트 삭제 처리
		post.setIsDeleted(true);
	}

	/**
	 * 게시글 수정 (일반 게시글)
	 * @param updater 수정하는 사용자
	 * @param postId 수정할 게시글 ID
	 * @param postUpdateRequestDto 수정할 내용
	 * @param attachImageList 첨부 이미지 목록
	 * @return 수정된 게시글 정보
	 */
	@Transactional
	public PostResponseDto updatePost(
		User updater,
		String postId,
		PostUpdateRequestDto postUpdateRequestDto,
		List<MultipartFile> attachImageList) {
		Set<Role> roles = updater.getRoles();
		Post post = getPost(postId);

		// 권한 검증
		ValidatorBucket validatorBucket = initializeValidator(updater, post.getBoard());
		if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			validatorBucket
				.consistOf(UserRoleValidator.of(
					roles,
					Set.of()));
		}
		validatorBucket
			.consistOf(PostNumberOfAttachmentsValidator.of(attachImageList)) // 첨부파일 개수 검증
			.consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
			.consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST))
			.consistOf(ContentsAdminValidator.of( // 작성자 본인인지 검증
				roles,
				updater.getId(),
				post.getWriter().getId(),
				List.of()))
			.consistOf(ConstraintValidator.of(post, this.validator));
		validatorBucket.validate();

		// 첨부 이미지 처리 (이미지 null로 요청 시 기존 이미지 삭제)
		List<PostAttachImage> postAttachImageList = new ArrayList<>();

		if (!attachImageList.isEmpty()) {
			// 기존 이미지 삭제 후 새 이미지 저장
			postAttachImageList = uuidFileService.updateFileList(
				post.getPostAttachImageList().stream().map(PostAttachImage::getUuidFile).collect(Collectors.toList()),
				attachImageList, FilePath.POST).stream()
				.map(uuidFile -> PostAttachImage.of(post, uuidFile))
				.toList();
		} else {
			// 기존 이미지 모두 삭제
			uuidFileService.deleteFileList(
				post.getPostAttachImageList().stream().map(PostAttachImage::getUuidFile).collect(Collectors.toList()));
		}

		// 기존 첨부 이미지 연결 삭제
		postAttachImageRepository.deleteAll(post.getPostAttachImageList());

		// 기존 폼 삭제
		formRepository.delete(post.getForm());

		// 게시글 업데이트
		post.update(
			postUpdateRequestDto.getTitle(),
			postUpdateRequestDto.getContent(),
			null,
			postAttachImageList);

		return toPostResponseDtoExtended(post, updater);
	}

	@Transactional
	public void updatePostWithForm(
		User updater,
		String postId,
		PostUpdateWithFormRequestDto postUpdateWithFormRequestDto,
		List<MultipartFile> attachImageList) {
		Set<Role> roles = updater.getRoles();
		Post post = getPost(postId);

		ValidatorBucket validatorBucket = initializeValidator(updater, post.getBoard());
		if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			validatorBucket
				.consistOf(UserRoleValidator.of(
					roles,
					Set.of()));
		}
		validatorBucket
			.consistOf(PostNumberOfAttachmentsValidator.of(attachImageList))
			.consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
			.consistOf(TargetIsDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST))
			.consistOf(ContentsAdminValidator.of(
				roles,
				updater.getId(),
				post.getWriter().getId(),
				List.of()))
			.consistOf(ConstraintValidator.of(post, this.validator));
		validatorBucket.validate();

		// post는 이미지가 nullable임 -> 이미지 null로 요청 시 기존 이미지 삭제
		List<PostAttachImage> postAttachImageList = new ArrayList<>();

		if (!attachImageList.isEmpty()) {
			postAttachImageList = uuidFileService.updateFileList(
				post.getPostAttachImageList().stream().map(PostAttachImage::getUuidFile).collect(Collectors.toList()),
				attachImageList, FilePath.POST).stream()
				.map(uuidFile -> PostAttachImage.of(post, uuidFile))
				.toList();
		} else {
			uuidFileService.deleteFileList(
				post.getPostAttachImageList().stream().map(PostAttachImage::getUuidFile).collect(Collectors.toList()));
		}

		postAttachImageRepository.deleteAll(post.getPostAttachImageList());

		formRepository.delete(post.getForm());

		Form form = generateForm(postUpdateWithFormRequestDto.getFormCreateRequestDto());

		post.update(
			postUpdateWithFormRequestDto.getTitle(),
			postUpdateWithFormRequestDto.getContent(),
			form,
			postAttachImageList);
	}

	@Transactional
	public void restorePost(User restorer, String postId) {
		Set<Role> roles = restorer.getRoles();
		Post post = getPost(postId);

		ValidatorBucket validatorBucket = ValidatorBucket.of();
		if (post.getBoard().getCategory().equals(StaticValue.BOARD_NAME_APP_NOTICE)) {
			validatorBucket
				.consistOf(UserRoleValidator.of(
					roles,
					Set.of()));
		}
		validatorBucket
			.consistOf(UserStateValidator.of(restorer))
			.consistOf(UserRoleIsNoneValidator.of(roles))
			.consistOf(TargetIsDeletedValidator.of(post.getBoard().getIsDeleted(), StaticValue.DOMAIN_BOARD))
			.consistOf(TargetIsNotDeletedValidator.of(post.getIsDeleted(), StaticValue.DOMAIN_POST));

		Optional<Circle> circles = Optional.ofNullable(post.getBoard().getCircle());
		circles
			.filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(
				Role.VICE_PRESIDENT))
			.ifPresentOrElse(
				circle -> {
					CircleMember member = getCircleMember(restorer.getId(), circle.getId());

					validatorBucket
						.consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
						.consistOf(CircleMemberStatusValidator.of(
							member.getStatus(),
							List.of(CircleMemberStatus.MEMBER)))
						.consistOf(ContentsAdminValidator.of(
							roles,
							restorer.getId(),
							post.getWriter().getId(),
							List.of(Role.LEADER_CIRCLE)));

					if (roles.contains(Role.LEADER_CIRCLE) && !post.getWriter().getId().equals(restorer.getId())) {
						validatorBucket
							.consistOf(UserEqualValidator.of(
								getCircleLeader(circle).getId(),
								restorer.getId()));
					}
				},
				() -> validatorBucket
					.consistOf(ContentsAdminValidator.of(
						roles,
						restorer.getId(),
						post.getWriter().getId(),
						List.of())));

		validatorBucket
			.consistOf(ContentsAdminValidator.of(
				roles,
				restorer.getId(),
				post.getWriter().getId(),
				List.of(Role.LEADER_CIRCLE)))
			.validate();

		post.setIsDeleted(false);
	}

	/**
	 * 게시글 좋아요 메서드
	 * @param user 좋아요 누른 유저
	 * @param postId 좋아요 누른 게시글 아이디
	 */
	@Transactional
	public void likePost(User user, String postId) {
		Post post = getPost(postId);

		validateWriterNotDeleted(post);

		if (isPostLiked(user, postId)) {
			throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.POST_ALREADY_LIKED);
		}

		LikePost likePost = LikePost.of(post, user);
		likePostRepository.save(likePost);
	}

	/**
	 * 게시글 좋아요 취소 메서드
	 * @param user 좋아요 취소 누른 유저
	 * @param postId 좋아요 취소 누른 게시글 아이디
	 */
	@Transactional
	public void cancelLikePost(final User user, final String postId) {
		Post post = getPost(postId);

		this.validateWriterNotDeleted(post);

		if (!isPostLiked(user, postId)) {
			throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.POST_NOT_LIKED);
		}

		likePostRepository.deleteLikeByPostIdAndUserId(postId, user.getId());
	}

	@Transactional
	public void favoritePost(User user, String postId) {
		Post post = getPost(postId);

		//FIXME : Validator 리팩토링 통합 후 해당 검사 로직을 해당방식으로 수정.
		if (isPostDeleted(post)) {
			throw new BadRequestException(ErrorCode.TARGET_DELETED, MessageUtil.POST_DELETED);
		}

		// 이미 즐겨찾기가 있는지 확인
		if (isPostFavorited(user, postId)) {
			throw new BadRequestException(ErrorCode.ROW_ALREADY_EXIST, MessageUtil.POST_ALREADY_FAVORITED);
		}

		FavoritePost favoritePost = FavoritePost.of(post, user);

		favoritePostRepository.save(favoritePost);
	}

	@Transactional
	public void cancelFavoritePost(User user, String postId) {
		Post post = getPost(postId);

		//FIXME : Validator 리팩토링 통합 후 해당 검사 로직을 해당방식으로 수정.
		if (isPostDeleted(post)) {
			throw new BadRequestException(ErrorCode.TARGET_DELETED, MessageUtil.POST_DELETED);
		}

		// 즐겨찾기 없는지 확인
		if (!isPostFavorited(user, postId)) {
			throw new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.POST_NOT_FAVORITED);
		}

		favoritePostRepository.deleteFavoriteByPostIdAndUserId(postId, user.getId());
	}

	public void createPostSubscribe(User user, String postId) {
		Post post = getPost(postId);

		UserPostSubscribe userPostSubscribe = UserPostSubscribe.of(user, post, true);
		userPostSubscribeRepository.save(userPostSubscribe);
	}

	@Transactional
	public PostSubscribeResponseDto setPostSubscribe(User user, String postId, Boolean isSubscribed) {
		Post post = getPost(postId);

		UserPostSubscribe subscription = userPostSubscribeRepository.findByUserAndPost(user, post)
			.map(existing -> {
				existing.setIsSubscribed(isSubscribed);
				return existing;
			})
			.orElseGet(() -> userPostSubscribeRepository.save(UserPostSubscribe.of(user, post, isSubscribed)));

		return PostDtoV1Mapper.INSTANCE.toPostSubscribeResponseDto(subscription);
	}

	/**
	 * 사용자가 게시글에 좋아요를 눌렀는지 확인
	 */
	private Boolean isPostLiked(User user, String postId) {
		return likePostRepository.existsByPostIdAndUserId(postId, user.getId());
	}

	/**
	 * 사용자가 게시글을 즐겨찾기했는지 확인
	 */
	private Boolean isPostAlreadyFavorite(User user, String postId) {
		return favoritePostRepository.existsByPostIdAndUserId(postId, user.getId());
	}

	/**
	 * 게시글이 삭제되었는지 확인
	 */
	private Boolean isPostDeleted(Post post) {
		return post.getIsDeleted();
	}

	/**
	 * 사용자 권한 및 게시판 접근 권한을 검증하는 ValidatorBucket 초기화
	 * @param user 검증할 사용자
	 * @param board 접근하려는 게시판
	 * @return 초기화된 ValidatorBucket
	 */
	private ValidatorBucket initializeValidator(User user, Board board) {
		Set<Role> roles = user.getRoles();
		ValidatorBucket validatorBucket = ValidatorBucket.of();
		validatorBucket
			.consistOf(UserStateValidator.of(user)) // 사용자 상태 검증
			.consistOf(UserRoleIsNoneValidator.of(roles)); // 역할이 NONE인지 검증

		// 동아리 게시판인 경우 추가 검증
		Optional<Circle> circles = Optional.ofNullable(board.getCircle());
		circles
			.filter(circle -> !roles.contains(Role.ADMIN) && !roles.contains(Role.PRESIDENT) && !roles.contains(
				Role.VICE_PRESIDENT))
			.ifPresent(
				circle -> {
					// 동아리 멤버인지 확인
					CircleMember member = getCircleMember(user.getId(), circle.getId());
					validatorBucket
						.consistOf(TargetIsDeletedValidator.of(circle.getIsDeleted(), StaticValue.DOMAIN_CIRCLE))
						.consistOf(CircleMemberStatusValidator.of( // 동아리 멤버 상태 검증
							member.getStatus(),
							List.of(CircleMemberStatus.MEMBER)));
				});
		return validatorBucket;
	}

	private Form generateForm(FormCreateRequestDto formCreateRequestDto) {
		validFormInfo(formCreateRequestDto);

		List<FormQuestion> formQuestionList = generateFormQuestionList(formCreateRequestDto);

		Form form = Form.createPostForm(
			formCreateRequestDto,
			formQuestionList);

		formQuestionList.forEach(question -> question.setForm(form));

		return form;
	}

	private static void validFormInfo(FormCreateRequestDto formCreateRequestDto) {
		// isAllowedEnrolled가 false이고 isAllowedLeaveOfAbsence가 false인 경우 예외 처리
		if (formCreateRequestDto.getIsAllowedEnrolled()) {
			// isNeedCouncilFeePaid가 null인 경우 예외 처리
			if (formCreateRequestDto.getIsNeedCouncilFeePaid() == null) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					MessageUtil.IS_NEED_COUNCIL_FEE_REQUIRED);
			}

			// enrolledRegisteredSemesterList가 null이거나 비어있는 경우 예외 처리
			if (formCreateRequestDto.getEnrolledRegisteredSemesterList() == null ||
				formCreateRequestDto.getEnrolledRegisteredSemesterList().isEmpty()) {
				throw new BadRequestException(
					ErrorCode.INVALID_PARAMETER,
					MessageUtil.INVALID_REGISTERED_SEMESTER_INFO);
			}
		}

		// isAllowedLeaveOfAbsence가 false이고 isAllowedLeaveOfAbsence가 false인 경우 예외 처리
		if (formCreateRequestDto.getIsAllowedLeaveOfAbsence() &&
			(formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList() == null ||
				formCreateRequestDto.getLeaveOfAbsenceRegisteredSemesterList().isEmpty())) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.INVALID_REGISTERED_SEMESTER_INFO);
		}
	}

	@NotNull
	private List<FormQuestion> generateFormQuestionList(FormCreateRequestDto formCreateRequestDto) {
		// questionCreateRequestDtoList가 null이거나 비어있는 경우 예외 처리
		if (formCreateRequestDto.getQuestionCreateRequestDtoList() == null
			|| formCreateRequestDto.getQuestionCreateRequestDtoList().isEmpty()) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.EMPTY_QUESTION_INFO);
		}

		AtomicReference<Integer> questionNumber = new AtomicReference<>(1);

		return formCreateRequestDto.getQuestionCreateRequestDtoList()
			.stream()
			.map(
				questionCreateRequestDto -> {
					FormQuestion formQuestion;

					// 객관식일 때, isMultiple이 null이거나 optionCreateRequestDtoList가 null이거나 비어있는 경우 예외 처리
					if (questionCreateRequestDto.getQuestionType().equals(QuestionType.OBJECTIVE)) {
						if (questionCreateRequestDto.getIsMultiple() == null ||
							(questionCreateRequestDto.getOptionCreateRequestDtoList() == null ||
								questionCreateRequestDto.getOptionCreateRequestDtoList().isEmpty())) {
							throw new BadRequestException(
								ErrorCode.INVALID_PARAMETER,
								MessageUtil.INVALID_QUESTION_INFO);
						}

						List<FormQuestionOption> formQuestionOptionList = getFormQuestionOptionList(
							questionCreateRequestDto);

						formQuestion = FormQuestion.createObjectiveQuestion(
							questionNumber.getAndSet(questionNumber.get() + 1),
							questionCreateRequestDto,
							formQuestionOptionList);

						formQuestionOptionList.forEach(option -> option.setFormQuestion(formQuestion));
					} else { // 주관식일 때
						formQuestion = FormQuestion.createSubjectQuestion(
							questionNumber.getAndSet(questionNumber.get() + 1),
							questionCreateRequestDto);
					}

					return formQuestion;
				})
			.toList();
	}

	@NotNull
	private static List<FormQuestionOption> getFormQuestionOptionList(
		QuestionCreateRequestDto questionCreateRequestDto) {
		// optionCreateRequestDtoList가 null이거나 비어있는 경우 예외 처리
		if (questionCreateRequestDto.getOptionCreateRequestDtoList() == null
			|| questionCreateRequestDto.getOptionCreateRequestDtoList().isEmpty()) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.EMPTY_OPTION_INFO);
		}

		AtomicReference<Integer> optionNumber = new AtomicReference<>(1);

		return questionCreateRequestDto.getOptionCreateRequestDtoList()
			.stream()
			.map(
				optionCreateRequestDto -> FormQuestionOption.of(
					optionNumber.getAndSet(optionNumber.get() + 1),
					optionCreateRequestDto.getOptionText(),
					null))
			.toList();
	}

	// DtoMapper methods
	private PostCreateResponseDto toPostCreateResponseDto(Post post) {
		return PostDtoV1Mapper.INSTANCE.toPostCreateResponseDto(post);
	}

	private BoardPostsResponseDto toBoardPostsResponseDto(Board board, Set<Role> userRoles, Boolean isFavorite,
		Boolean isBoardSubscribed, Page<PostsResponseDto> post) {
		List<String> roles = Arrays.asList(board.getCreateRoles().split(","));
		Boolean writable = userRoles.stream()
			.map(Role::getValue)
			.anyMatch(roles::contains);
		return PostDtoV1Mapper.INSTANCE.toBoardPostsResponseDto(
			board,
			userRoles,
			writable,
			isFavorite,
			isBoardSubscribed,
			post);
	}

	private PostResponseDto toPostResponseDtoExtended(Post post, User user) {
		PostResponseDto postResponseDto = PostDtoV1Mapper.INSTANCE.toPostResponseDtoExtended(
			post,
			getNumOfComments(post),
			getNumOfPostLikes(post),
			getNumOfPostFavorites(post),
			isPostLiked(user, post.getId()),
			isPostAlreadyFavorite(user, post.getId()),
			StatusPolicy.isPostOwner(post, user),
			StatusPolicy.isUpdatable(post, user, isPostHasComment(post.getId())),
			StatusPolicy.isDeletable(post, user, post.getBoard(), isPostHasComment(post.getId())),
			StatusPolicy.isPostForm(post) ? toFormResponseDto(post.getForm()) : null,
			StatusPolicy.isPostVote(post) ? toVoteResponseDto(post.getVote(), user) : null,
			StatusPolicy.isPostVote(post),
			StatusPolicy.isPostForm(post),
			isPostSubscribed(user, post));

		// 화면에 표시할 작성자 닉네임 설정
		User writer = post.getWriter();
		postResponseDto.setDisplayWriterNickname(
			getDisplayWriterNickname(writer, postResponseDto.getIsAnonymous(), postResponseDto.getWriterNickname()));

		return postResponseDto;
	}

	private Long getNumOfComments(Post post) {
		return postRepository.countAllCommentByPost_Id(post.getId());
	}

	private Long getNumOfPostLikes(Post post) {
		return likePostRepository.countByPostId(post.getId());
	}

	private Long getNumOfPostFavorites(Post post) {
		return favoritePostRepository.countByPostId(post.getId());
	}

	private Boolean isFavorite(String userId, String boardId) {
		return favoriteBoardRepository.findByUser_Id(userId)
			.stream()
			.filter(favoriteBoard -> !favoriteBoard.getBoard().getIsDeleted())
			.anyMatch(favoriteboard -> favoriteboard.getBoard().getId().equals(boardId));
	}

	private Boolean isBoardSubscribed(User user, Board board) {
		return userBoardSubscribeRepository.findByUserAndBoard(user, board)
			.map(UserBoardSubscribe::getIsSubscribed)
			.orElse(false);
	}

	private Boolean isPostSubscribed(User user, Post post) {
		return userPostSubscribeRepository.findByUserAndPost(user, post)
			.map(UserPostSubscribe::getIsSubscribed)
			.orElse(false);
	}

	private Boolean isPostHasComment(String postId) {
		return commentRepository.existsByPostIdAndIsDeletedFalse(postId);
	}

	private Post getPost(String postId) {
		return postRepository.findById(postId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.POST_NOT_FOUND));
	}

	private Board getBoard(String boardId) {
		return boardRepository.findById(boardId).orElseThrow(
			() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST,
				MessageUtil.BOARD_NOT_FOUND));
	}

	private CircleMember getCircleMember(String userId, String circleId) {
		return circleMemberRepository.findByUser_IdAndCircle_Id(userId, circleId).orElseThrow(
			() -> new UnauthorizedException(
				ErrorCode.NOT_MEMBER,
				MessageUtil.CIRCLE_APPLY_INVALID));
	}

	private User getCircleLeader(Circle circle) {
		User leader = circle.getLeader().orElse(null);
		if (leader == null) {
			throw new InternalServerException(
				ErrorCode.INTERNAL_SERVER,
				MessageUtil.CIRCLE_WITHOUT_LEADER);
		}
		return leader;
	}

	private FormResponseDto toFormResponseDto(Form form) {
		return FormDtoMapper.INSTANCE.toFormResponseDto(
			form,
			form.getFormQuestionList().stream()
				.map(this::toQuestionResponseDto)
				.collect(Collectors.toList()));
	}

	private QuestionResponseDto toQuestionResponseDto(FormQuestion formQuestion) {
		return FormDtoMapper.INSTANCE.toQuestionResponseDto(
			formQuestion,
			formQuestion.getFormQuestionOptionList().stream()
				.map(this::toOptionResponseDto)
				.collect(Collectors.toList()));
	}

	private OptionResponseDto toOptionResponseDto(FormQuestionOption formQuestionOption) {
		return FormDtoMapper.INSTANCE.toOptionResponseDto(formQuestionOption);
	}

	private VoteResponseDto toVoteResponseDto(Vote vote, User user) {
		List<VoteOptionResponseDto> voteOptionResponseDtoList = vote.getVoteOptions().stream()
			.sorted(Comparator.comparing(VoteOption::getCreatedAt))
			.map(this::tovoteOptionResponseDto)
			.collect(Collectors.toList());

		Set<String> uniqueUserIds = voteOptionResponseDtoList.stream()
			.flatMap(voteOptionResponseDto -> voteOptionResponseDto.getVoteUsers().stream())
			.map(UserResponseDto::getId)
			.collect(Collectors.toSet());
		Integer totalUserCount = uniqueUserIds.size();
		return VoteDtoMapper.INSTANCE.toVoteResponseDto(
			vote,
			voteOptionResponseDtoList, StatusPolicy.isVoteOwner(vote, user), vote.isEnd(),
			voteRecordRepository.existsByVoteOption_VoteAndUser(vote, user), voteOptionResponseDtoList.stream()
				.mapToInt(VoteOptionResponseDto::getVoteCount)
				.sum(),
			totalUserCount);
	}

	private VoteOptionResponseDto tovoteOptionResponseDto(VoteOption voteOption) {
		List<VoteRecord> voteRecords = voteRecordRepository.findAllByVoteOption(voteOption);
		List<UserResponseDto> userResponseDtos = voteRecords.stream()
			.map(voteRecord -> UserDtoMapper.INSTANCE.toUserResponseDto(voteRecord.getUser(), null, null))
			.collect(Collectors.toList());
		return VoteDtoMapper.INSTANCE.toVoteOptionResponseDto(voteOption, voteRecords.size(), userResponseDtos);
	}

	/**
	 * 게시글의 글쓴이가 삭제된 사용자인지 유효성 검사
	 * @param post 게시글
	 * @throws BadRequestException 작성자가 삭제된 사용자인 경우
	 */
	private void validateWriterNotDeleted(final Post post) {
		ValidatorBucket validatorBucket = ValidatorBucket.of();
		validatorBucket
			.consistOf(UserStateIsDeletedValidator.of(post.getWriter()))
			.validate();
	}

	/**
	 * 익명 글이 허용되지 않는 게시판에서 익명 글 작성 시 예외 처리
	 * @param board  글이 작성될 게시판
	 * @param isAnonymous 익명 글 여부
	 * @throws BadRequestException 익명 글이 허용되지 않는 게시판에서 익명 글 작성 시도 시
	 */
	private void validateAnonymousAllowed(Board board, boolean isAnonymous) {
		if (!board.getIsAnonymousAllowed() && isAnonymous) {
			throw new BadRequestException(
				ErrorCode.INVALID_PARAMETER,
				MessageUtil.ANONYMOUS_NOT_ALLOWED);
		}
	}

	/**
	 * 게시글이 즐겨찾기 되어있는지 확인
	 */
	// 게시글이 즐겨찾기 되어있는지 확인
	private Boolean isPostFavorited(User user, String postId) {
		return favoritePostRepository.existsByPostIdAndUserId(postId, user.getId());
	}

	/**
	 * 화면에 표시할 작성자 닉네임 설정
	 * - 비활성/탈퇴/삭제 유저: "비활성 유저"
	 * - 익명 게시글: "익명"
	 * - 일반 게시글: 원래 닉네임
	 * @param writer 작성자
	 * @param isAnonymous 익명 여부
	 * @param originalNickname 원래 닉네임
	 * @return 화면에 표시할 닉네임
	 */
	// 화면에 표시할 작성자 닉네임 설정 (닉네임 / 비활성 유저 / 익명)
	public String getDisplayWriterNickname(User writer, Boolean isAnonymous, String originalNickname) {
		if (writer != null && (writer.isDeleted() || List.of(UserState.INACTIVE, UserState.DROP)
			.contains(writer.getState()))) {
			return StaticValue.INACTIVE_USER_NICKNAME;
		} else if (Boolean.TRUE.equals(isAnonymous)) {
			return StaticValue.ANONYMOUS_USER_NICKNAME;
		} else {
			return originalNickname;
		}
	}
}
