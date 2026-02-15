package net.causw.app.main.domain.community.post.service.v2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.asset.file.entity.joinEntity.PostAttachImage;
import net.causw.app.main.domain.asset.file.enums.FilePath;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileReader;
import net.causw.app.main.domain.asset.file.service.v2.implementation.FileWriter;
import net.causw.app.main.domain.asset.file.service.v2.implementation.PostAttachImageWriter;
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
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostWriter;
import net.causw.app.main.domain.community.post.service.v2.mapper.PostMapper;
import net.causw.app.main.domain.community.post.service.v2.util.PostCursorManager;
import net.causw.app.main.domain.community.post.service.v2.util.PostValidator;
import net.causw.app.main.domain.community.reaction.service.implementation.FavoritePostReader;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.community.vote.service.implementation.VoteWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
	private final PostReader postReader;
	private final PostWriter postWriter;
	private final BoardReader boardReader;
	private final FileWriter fileWriter;
	private final FileReader fileReader;
	private final BoardConfigReader boardConfigReader;
	private final LikePostReader likePostReader;
	private final FavoritePostReader favoritePostReader;
	private final PostAttachImageWriter postAttachImageWriter;
	private final VoteWriter voteWriter;

	@Transactional
	public PostCreateResult create(PostCreateCommand command) {
		User writer = command.writer();

		Board board = boardReader.getById(command.boardId());
		BoardConfig boardConfig = boardConfigReader.getByBoardId(command.boardId());
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(command.boardId());

		PostValidator.validateCreate(writer, board, boardConfig, boardAdminIds, command.isAnonymous());

		List<UuidFile> images;
		if (command.images() != null && !command.images().isEmpty()) {
			images = fileWriter.uploadAndSaveList(command.images(), FilePath.POST);
		} else {
			images = new ArrayList<>();
		}
		Post post = PostMapper.fromCreateCommand(command, writer, board, images);

		Post savedPost = postWriter.save(post);
		return PostMapper.toCreateResult(savedPost, images.stream().map(UuidFile::getFileUrl).toList());
	}

	@Transactional
	public void deletePost(User deleter, String postId) {
		Post post = postReader.findById(postId);
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());
		PostValidator.validateDelete(deleter, post, boardAdminIds);

		// 소프트 삭제 처리
		post.setIsDeleted(true);
	}

	/**
	 * 게시글의 첨부 이미지를 삭제합니다.
	 * PostAttachImage를 먼저 삭제한 후 UuidFile을 삭제합니다.
	 *
	 * @param post 게시글 엔티티
	 */
	private void deletePostImages(Post post) {
		List<PostAttachImage> images = post.getPostAttachImageList();
		if (images != null && !images.isEmpty()) {
			// 1. UuidFile ID 목록을 미리 추출 (삭제 전에)
			List<String> fileIds = images.stream()
				.map(PostAttachImage::getUuidFile)
				.map(UuidFile::getId)
				.toList();

			// 2. PostAttachImage를 즉시 삭제
			postAttachImageWriter.deleteAllInBatch(images);

			// 3. UuidFile 삭제
			List<UuidFile> files = fileReader.findByIds(fileIds);
			fileWriter.deleteList(files);
		}
	}

	@Transactional
	public PostUpdateResult update(PostUpdateCommand command) {
		User updater = command.updater();
		Post post = postReader.findById(command.postId());
		BoardConfig boardConfig = boardConfigReader.getByBoardId(post.getBoard().getId());
		List<String> boardAdminIds = boardConfigReader.getAdminIdsByBoardId(post.getBoard().getId());

		PostValidator.validateUpdate(updater, post, boardAdminIds, boardConfig, command.isAnonymous());

		// 기존 이미지 삭제
		deletePostImages(post);

		// 새 이미지 업로드 및 저장
		List<UuidFile> newImages;
		List<PostAttachImage> newPostAttachImages;
		if (command.images() != null && !command.images().isEmpty()) {
			newImages = fileWriter.uploadAndSaveList(command.images(), FilePath.POST);
			newPostAttachImages = newImages.stream()
				.map(uuidFile -> PostAttachImage.of(post, uuidFile))
				.toList();
		} else {
			newImages = new ArrayList<>();
			newPostAttachImages = new ArrayList<>();
		}

		// 게시글 업데이트
		Post updatedPost = postWriter.updateContentAndImages(post, command.content(), newPostAttachImages);

		return PostMapper.toUpdateResult(updatedPost, newImages.stream().map(UuidFile::getFileUrl).toList());
	}

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

		// 게시글 조회 (Slice 사용)
		Slice<PostCursorResult> slice = postReader.findPostsWithCursor(
			boardIds,
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

		// PostListResult로 변환 (PostMapper 사용)
		List<PostListResult.PostItem> postItems = posts.stream()
			.map(result -> {
				List<String> imageUrls = postImagesMap.getOrDefault(result.postId(), List.of());
				return PostMapper.toPostListItem(result, imageUrls);
			})
			.toList();

		return PostListResult.of(postItems, nextCursor);
	}

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

		// 수정/삭제 가능 여부 (작성자 또는 게시판 관리자)
		boolean updatable = isOwner || boardAdminIds.contains(viewer.getId());
		boolean deletable = isOwner || boardAdminIds.contains(viewer.getId());

		// PostMapper를 사용하여 PostDetailResult 생성
		return PostMapper.toPostDetailResult(
			post,
			imageUrls,
			numComment,
			numLike,
			numFavorite,
			isPostLike,
			isPostFavorite,
			isOwner,
			updatable,
			deletable);
	}

}
