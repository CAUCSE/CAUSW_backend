package net.causw.app.main.domain.community.post.service.v2;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.asset.file.entity.joinEntity.UserProfileImage;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileReader;
import net.causw.app.main.domain.asset.file.service.v2.implementation.UserProfileImageReader;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.entity.BoardConfig;
import net.causw.app.main.domain.community.board.service.implementation.BoardConfigReader;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.query.PostCursorResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListQuery;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateResult;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostImageManager;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostWriter;
import net.causw.app.main.domain.community.post.service.v2.mapper.PostMapper;
import net.causw.app.main.domain.community.post.service.v2.util.PostCursorManager;
import net.causw.app.main.domain.community.post.service.v2.util.PostValidator;
import net.causw.app.main.domain.community.reaction.service.implementation.FavoritePostReader;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.notification.notification.event.OfficialPostEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockReader;
import net.causw.app.main.shared.exception.errorcode.PostErrorCode;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final PostReader postReader;
	private final PostWriter postWriter;
	private final PostImageManager postImageManager;
	private final BoardReader boardReader;
	private final BoardConfigReader boardConfigReader;
	private final LikePostReader likePostReader;
	private final FavoritePostReader favoritePostReader;
	private final BlockReader userBlockReader;
	private final ApplicationEventPublisher eventPublisher;
	private final UserProfileImageReader userProfileImageReader;
	private final FileReader fileReader;

	/**
	 * 게시글을 생성합니다. 게시글 내용과 첨부 이미지를 저장합니다.
	 *
	 * @param command 생성에 필요한 정보 (작성자, 게시판 ID, 내용, 이미지 파일·메타 등)
	 * @return 생성된 게시글 정보 (게시글 ID, 내용, 이미지 URL 목록 등)
	 */
	@Transactional
	public PostCreateResult create(PostCreateCommand command) {
		User writer = command.writer();

		Board board = boardReader.getById(command.boardId());
		BoardConfig boardConfig = boardConfigReader.getByBoardId(command.boardId());
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(command.boardId());

		PostValidator.validateCreate(writer, board, boardConfig, boardAdminIds, command.isAnonymous());

		// Post 엔티티 생성 (이미지 없이 먼저 생성)
		Post post = PostMapper.fromCreateCommand(command, writer, board, List.of());
		Post savedPost = postWriter.save(post);

		// 공식 공지글인 경우 알림 발송 이벤트
		if (boardConfig.isNotice()) {
			eventPublisher.publishEvent(new OfficialPostEvent(savedPost.getBoard().getId(), savedPost.getId()));
		}

		// 이미지 업로드 및 PostAttachImage 구성 (PostImageManager에 위임)
		List<PostAttachImage> postAttachImages = postImageManager.uploadAndBuildForCreate(
			savedPost, command.imageFiles(), command.imageMetas());

		if (!postAttachImages.isEmpty()) {
			savedPost.updateContentAndImages(savedPost.getContent(), postAttachImages);
		}

		List<String> imageUrls = postAttachImages.stream()
			.map(img -> img.getUuidFile().getFileUrl())
			.toList();

		return PostMapper.toCreateResult(savedPost, imageUrls);
	}

	@Transactional
	public void deletePost(User deleter, String postId) {
		Post post = postReader.findById(postId);
		String boardId = post.getBoard().getId();
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(boardId);
		PostValidator.validateDelete(deleter, post, boardAdminIds);

		// 소프트 삭제 처리
		post.setIsDeleted(true);
	}

	/**
	 * 게시글을 수정합니다. 게시글 내용과 첨부 이미지를 업데이트할 수 있습니다.
	 * <p>
	 * imageMetas의 type=EXISTING 항목은 기존 이미지를 유지하고,
	 * type=NEW 항목은 새 파일을 업로드합니다.
	 * 기존 이미지 중 imageMetas에 포함되지 않은 이미지는 삭제됩니다.
	 *
	 * @param command 수정에 필요한 정보 (게시글 ID, 수정자, 새 내용, 이미지 파일·메타 등)
	 * @return 수정된 게시글 정보 (게시글 ID, 새 내용, 새 이미지 URL 목록 등)
	 */
	@Transactional
	public PostUpdateResult update(PostUpdateCommand command) {
		User updater = command.updater();
		Post post = postReader.findById(command.postId());
		BoardConfig boardConfig = boardConfigReader.getByBoardId(post.getBoard().getId());
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());

		PostValidator.validateUpdate(updater, post, boardAdminIds, boardConfig, command.isAnonymous());

		// 이미지 병합 처리 (PostImageManager에 위임)
		PostImageManager.ImageUpdateResult imageResult = postImageManager.mergeAndBuildForUpdate(
			post, command.newImageFiles(), command.imageMetas());

		// 게시글 업데이트
		Post updatedPost = postWriter.updateContentImagesAndAnonymous(
			post, command.content(), command.isAnonymous(), imageResult.finalImages());

		List<String> imageUrls = imageResult.finalImages().stream()
			.map(img -> img.getUuidFile().getFileUrl())
			.toList();

		PostUpdateResult result = PostMapper.toUpdateResult(updatedPost, imageUrls);

		// 트랜잭션 커밋 후 실제 스토리지 파일 삭제
		postImageManager.deleteOrphanedFiles(imageResult.deletedFileIds());

		return result;
	}

	/**
	 * 게시글 목록을 커서 기반으로 조회합니다.
	 * <br> 게시판 ID 목록이 지정된 경우 해당 게시판들에서, 지정되지 않은 경우 사용자가 접근 가능한 모든 게시판에서 게시글을 조회합니다.
	 * @param query 조회 조건 (게시판 ID 목록, 커서, 페이지 크기, 키워드 등)
	 * @return 게시글 목록 결과 (게시글 리스트 + 다음 커서)
	 */
	public PostListResult getPosts(PostListQuery query) {
		User viewer = query.viewer();
		List<String> requestedBoardIds = query.boardIds();
		String cursor = query.cursor();
		int size = query.size() != null ? query.size() : StaticValue.DEFAULT_POST_PAGE_SIZE; // 기본값 20
		String keyword = query.keyword();

		// 커서 파싱
		PostCursorManager.ParsedCursor parsedCursor = PostCursorManager.parseCursor(cursor);

		List<String> boardIds;
		// 게시판 ID 목록이 지정된 경우
		if (requestedBoardIds != null && !requestedBoardIds.isEmpty()) {
			// 각 게시판에 대한 읽기 권한 검증
			for (String boardId : requestedBoardIds) {
				BoardConfig boardConfig = boardConfigReader.getByBoardId(boardId);
				List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(boardId);

				// ReadScope 검증
				PostValidator.validateRead(viewer, boardConfig, boardAdminIds);
			}

			boardIds = requestedBoardIds;
		} else {
			// 게시판 ID가 지정되지 않은 경우 - 사용자의 AcademicStatus에 따라 접근 가능한 게시판 조회
			boardIds = boardConfigReader.getAccessibleBoardIdsByAcademicStatus(viewer.getAcademicStatus());
		}

		// 접근 가능한 게시판이 없으면 게시글 조회를 건너뛰고 빈 결과를 반환
		if (boardIds.isEmpty()) {
			return PostListResult.of(List.of(), null);
		}

		// 뷰어가 차단한 사용자 조회
		Set<String> blockedUserIds = userBlockReader.findBlockeeUserIdsByBlocker(viewer);

		// 게시글 조회 (Slice 사용)
		Slice<PostCursorResult> slice = postReader.findPostsWithCursor(
			boardIds,
			blockedUserIds,
			parsedCursor.createdAt(),
			parsedCursor.postId(),
			size,
			keyword);

		// Slice에서 content와 hasNext 추출
		List<PostCursorResult> posts = slice.getContent();
		boolean hasNext = slice.hasNext();

		// 다음 커서 생성
		String nextCursor = null;
		if (hasNext && !posts.isEmpty()) {
			PostCursorResult lastPost = posts.get(posts.size() - 1);
			nextCursor = PostCursorManager.createNextCursor(lastPost.createdAt(), lastPost.postId());
		}

		// 게시글 이미지 조회
		List<String> postIds = posts.stream().map(PostCursorResult::postId).toList();
		Map<String, List<String>> postImagesMap = postIds.isEmpty()
			? Map.of()
			: postReader.findPostImagesByPostIds(postIds);

		// 좋아요 여부 배치 조회
		Set<String> likedPostIds = postIds.isEmpty()
			? Set.of()
			: likePostReader.getLikedPostIds(viewer.getId(), postIds);

		// 게시판 설정 배치 조회
		List<String> uniqueBoardIds = posts.stream().map(PostCursorResult::boardId).filter(Objects::nonNull)
			.distinct().toList();
		Map<String, BoardConfig> boardConfigMap = boardConfigReader.getBoardConfigMapByBoardIds(uniqueBoardIds);

		// 작성자 중 Role이 ADMIN인 사용자 ID 조회 (시스템 관리자 판별용)
		List<String> writerIds = posts.stream()
			.map(PostCursorResult::writerId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		Set<String> adminWriterIds = postReader.findAdminUserIds(writerIds);

		// PostListResult로 변환 (PostMapper 사용)
		List<PostListResult.PostItem> postItems = buildPostItems(posts, postImagesMap, likedPostIds, viewer,
			boardConfigMap, adminWriterIds);

		return PostListResult.of(postItems, nextCursor);
	}

	/**
	 * 게시글 단건 조회. 게시글 내용, 첨부 이미지 URL 목록, 좋아요/즐겨찾기/댓글 개수, 사용자의 좋아요/즐겨찾기 여부, 수정/삭제 가능 여부 등을 포함합니다.
	 * @param query 조회 조건 (게시글 ID, 조회 요청 사용자)
	 * @return 게시글 상세 정보 (게시글 ID, 내용, 첨부 이미지 URL 목록, 좋아요/즐겨찾기/댓글 개수, 사용자의 좋아요/즐겨찾기 여부, 수정/삭제 가능 여부 등)
	 */
	public PostDetailResult getPostDetail(PostDetailQuery query) {
		User viewer = query.viewer();
		String postId = query.postId();

		// 게시글 조회
		Post post = postReader.findById(postId);
		Board board = post.getBoard();
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(board.getId());

		// 게시판 접근 권한 검증
		BoardConfig boardConfig = boardConfigReader.getByBoardId(board.getId());

		// ReadScope 검증
		PostValidator.validateRead(viewer, boardConfig, boardAdminIds);

		// 차단한 사용자가 작성한 게시글은 조회 불가
		User writer = post.getWriter();
		if (writer != null && !writer.getId().equals(viewer.getId()) && !boardAdminIds.contains(viewer.getId())
			&& userBlockReader.existsByBlockerAndBlocked(viewer, writer)) {
			throw PostErrorCode.BLOCKED_USER_CONTENT.toBaseException();
		}

		// 게시글 이미지 조회
		List<String> imageUrls = postReader.findPostImages(postId);

		// 좋아요, 즐겨찾기, 댓글 개수 조회
		Long numComment = postReader.countComments(postId);
		Long numLike = likePostReader.countByPostId(postId);
		Long numFavorite = favoritePostReader.countByPostId(postId);

		// 사용자의 좋아요, 즐겨찾기 여부
		Boolean isPostLike = likePostReader.existsByPostIdAndUserId(postId, viewer.getId());
		Boolean isPostFavorite = favoritePostReader.existsByPostIdAndUserId(postId, viewer.getId());

		// 게시글 작성자 여부
		boolean isOwner = post.getWriter().getId().equals(viewer.getId());

		// 수정/삭제 가능 여부 (수정은 작성자만, 삭제는 작성자 + 게시판 관리자 + 시스템 관리자)
		boolean updatable = isOwner;
		boolean deletable = isOwner || boardAdminIds.contains(viewer.getId()) || viewer.getRoles().contains(Role.ADMIN);

		// 닉네임 마스킹 및 공식 배지 여부 판단
		boolean isNotice = boardConfig.isNotice() || post.getIsCrawled();
		boolean isAdmin = post.getWriter() != null && post.getWriter().getRoles().contains(Role.ADMIN);
		boolean isOfficial = isNotice || (isAdmin && !post.getIsAnonymous());

		// 공식 프로필 정보 조회
		String officialNickname = boardConfig.getOfficialNickname();
		String officialImageUrl = null;
		if (boardConfig.getOfficialProfileImageId() != null) {
			UuidFile file = fileReader.findByIdOptional(boardConfig.getOfficialProfileImageId()).orElse(null);
			if (file != null && Boolean.TRUE.equals(file.getIsUsed())) {
				officialImageUrl = file.getFileUrl();
			}
		}

		// 작성자 프로필 이미지 조회
		UserProfileImage writerProfileImage = (!isNotice && post.getWriter() != null)
			? userProfileImageReader.findByUserIdOrNull(post.getWriter().getId())
			: null;

		// PostMapper를 사용하여 PostDetailResult 생성
		return PostMapper.toPostDetailResult(
			post,
			writerProfileImage,
			imageUrls,
			numComment,
			numLike,
			numFavorite,
			isPostLike,
			isPostFavorite,
			isOwner,
			updatable,
			deletable,
			isNotice,
			isOfficial,
			officialNickname,
			officialImageUrl);
	}

	/**
	 * 로그인한 사용자가 댓글을 작성한 게시글 목록을 커서 기반으로 조회합니다.
	 * findPostsWithCursor와 동일한 PostListResponse 형식(커서 포함)으로 반환합니다.
	 *
	 * @param user   조회 요청 사용자 (차단 목록 등에 사용)
	 * @param cursor 커서 (마지막 게시글의 createdAt|postId, null이면 최신부터)
	 * @param size   조회할 개수 (null이면 기본값 사용)
	 * @return 게시글 목록 결과
	 */
	public PostListResult getPostsCommentedByUser(User user, String cursor, Integer size) {
		Set<String> blockedUserIds = userBlockReader.findBlockeeUserIdsByBlocker(user);
		int pageSize = size != null ? size : StaticValue.DEFAULT_POST_PAGE_SIZE;
		PostCursorManager.ParsedCursor parsedCursor = PostCursorManager.parseCursor(cursor);

		Slice<PostCursorResult> slice = postReader.findPostsCommentedByUserWithCursor(
			user.getId(),
			blockedUserIds,
			parsedCursor.createdAt(),
			parsedCursor.postId(),
			pageSize);

		return getPostListResult(slice, user);
	}

	/**
	 * 로그인한 사용자가 작성한 게시글 목록을 커서 기반으로 조회합니다.
	 *
	 * @param user 조회 요청 사용자 (차단 목록 등에 사용)
	 * @param cursor 커서 (마지막 게시글의 createdAt|postId, null이면 최신부터)
	 * @param size 조회할 개수 (null이면 기본값 사용)
	 * @return 게시글 목록 결과
	 */
	public PostListResult getPostsWrittenByUser(User user, String cursor, Integer size) {
		int pageSize = size != null ? size : StaticValue.DEFAULT_POST_PAGE_SIZE;
		PostCursorManager.ParsedCursor parsedCursor = PostCursorManager.parseCursor(cursor);

		Slice<PostCursorResult> slice = postReader.findPostsWrittenByUserWithCursor(
			user.getId(),
			parsedCursor.createdAt(),
			parsedCursor.postId(),
			pageSize);

		return getPostListResult(slice, user);
	}

	/**
	 * 로그인한 사용자가 좋아요를 누른 게시글 목록을 커서 기반으로 조회합니다.
	 * @param user 조회 요청 사용자
	 * @param cursor 커서 (마지막 게시글의 createdAt|postId, null이면 최신부터)
	 * @param size 조회할 개수 (null이면 기본값 사용)
	 * @return 게시글 목록 결과
	 */
	public PostListResult getPostsLikedByUser(User user, String cursor, Integer size) {
		Set<String> blockedUserIds = userBlockReader.findBlockeeUserIdsByBlocker(user);
		int pageSize = size != null ? size : StaticValue.DEFAULT_POST_PAGE_SIZE;
		PostCursorManager.ParsedCursor parsedCursor = PostCursorManager.parseCursor(cursor);

		Slice<PostCursorResult> slice = postReader.findPostsLikedByUserWithCursor(
			user.getId(),
			blockedUserIds,
			parsedCursor.createdAt(),
			parsedCursor.postId(),
			pageSize);

		return getPostListResult(slice, user);
	}

	@NotNull
	private PostListResult getPostListResult(Slice<PostCursorResult> slice, User viewer) {
		List<PostCursorResult> posts = slice.getContent();
		if (posts.isEmpty()) {
			return PostListResult.of(List.of(), null);
		}

		List<String> postIds = posts.stream().map(PostCursorResult::postId).toList();
		Map<String, List<String>> postImagesMap = postReader.findPostImagesByPostIds(postIds);

		Set<String> likedPostIds = likePostReader.getLikedPostIds(viewer.getId(), postIds);

		// 게시판 설정 배치 조회
		List<String> uniqueBoardIds = posts.stream().map(PostCursorResult::boardId).filter(Objects::nonNull)
			.distinct().toList();
		Map<String, BoardConfig> boardConfigMap = boardConfigReader.getBoardConfigMapByBoardIds(uniqueBoardIds);

		// 작성자 중 Role이 ADMIN인 사용자 ID 조회 (시스템 관리자 판별용)
		List<String> writerIds = posts.stream()
			.map(PostCursorResult::writerId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();
		Set<String> adminWriterIds = postReader.findAdminUserIds(writerIds);

		List<PostListResult.PostItem> postItems = buildPostItems(posts, postImagesMap, likedPostIds, viewer,
			boardConfigMap, adminWriterIds);

		String nextCursor = null;
		if (slice.hasNext()) {
			PostCursorResult lastPost = posts.get(posts.size() - 1);
			nextCursor = PostCursorManager.createNextCursor(lastPost.createdAt(), lastPost.postId());
		}

		return PostListResult.of(postItems, nextCursor);
	}

	/**
	 * 게시글 목록(PostCursorResult)을 PostItem 리스트로 변환합니다.
	 * 게시판별 boardAdmin 목록을 통해 공식계정 여부를 판단하고, 좋아요/소유 여부를 포함한 PostItem을 생성합니다.
	 *
	 * @param posts          변환할 게시글 커서 결과 목록
	 * @param postImagesMap  게시글 ID → 이미지 URL 목록 맵
	 * @param likedPostIds   viewer가 좋아요한 게시글 ID 집합
	 * @param viewer         조회 요청 사용자
	 * @param boardConfigMap 게시판 ID → BoardConfig맵
	 * @return PostItem 리스트
	 */
	private List<PostListResult.PostItem> buildPostItems(
		List<PostCursorResult> posts,
		Map<String, List<String>> postImagesMap,
		Set<String> likedPostIds,
		User viewer,
		Map<String, BoardConfig> boardConfigMap,
		Set<String> adminWriterIds) {

		return posts.stream()
			.map(result -> {
				List<String> imageUrls = postImagesMap.getOrDefault(result.postId(), List.of());
				boolean isPostLike = likedPostIds.contains(result.postId());
				boolean isOwner = result.writerId() != null && result.writerId().equals(viewer.getId());

				BoardConfig boardConfig = boardConfigMap.get(result.boardId());

				// 마스킹 및 공식배지 판단
				boolean isNotice = (boardConfig != null && boardConfig.isNotice()) || result.isCrawled();
				boolean isAdmin = result.writerId() != null && adminWriterIds.contains(result.writerId());
				boolean isOfficial = isNotice || (isAdmin && !result.isAnonymous());

				String officialNickname = boardConfig != null ? boardConfig.getOfficialNickname() : null;
				String officialImageUrl = null;
				if (boardConfig != null && boardConfig.getOfficialProfileImageId() != null) {
					UuidFile file = fileReader.findByIdOptional(boardConfig.getOfficialProfileImageId()).orElse(null);
					if (file != null && Boolean.TRUE.equals(file.getIsUsed())) {
						officialImageUrl = file.getFileUrl();
					}
				}

				return PostMapper.toPostListItem(result, imageUrls, isPostLike, isOwner, isNotice, isOfficial,
					officialNickname,
					officialImageUrl);
			})
			.toList();
	}
}