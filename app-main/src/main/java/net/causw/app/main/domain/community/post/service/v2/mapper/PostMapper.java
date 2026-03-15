package net.causw.app.main.domain.community.post.service.v2.mapper;

import java.util.List;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.query.PostCursorResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostDetailResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostListResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateResult;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.shared.dto.ProfileImageDto;
import net.causw.global.constant.StaticValue;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostMapper {

	public static Post fromCreateCommand(PostCreateCommand command, User writer, Board board,
		List<UuidFile> images) {
		return Post.of(null,
			command.content(),
			writer,
			command.isAnonymous(),
			board,
			images);
	}

	public static PostCreateResult toCreateResult(Post post, List<String> images) {
		return PostCreateResult.builder()
			.id(post.getId())
			.content(post.getContent())
			.isAnonymous(post.getIsAnonymous())
			.fileUrlList(images)
			.writerId(post.getWriter().getId())
			.createdAt(post.getCreatedAt())
			.updatedAt(post.getUpdatedAt())
			.boardName(post.getBoard().getName())
			.build();
	}

	public static PostUpdateResult toUpdateResult(Post post, List<String> images) {
		return PostUpdateResult.builder()
			.id(post.getId())
			.content(post.getContent())
			.isAnonymous(post.getIsAnonymous())
			.fileUrlList(images)
			.writerId(post.getWriter().getId())
			.createdAt(post.getCreatedAt())
			.updatedAt(post.getUpdatedAt())
			.boardName(post.getBoard().getName())
			.build();
	}

	/**
	 * PostCursorResult를 PostListResult.PostItem으로 변환합니다.
	 */
	public static PostListResult.PostItem toPostListItem(PostCursorResult result, List<String> imageUrls) {
		String writerNickname = resolveWriterNickname(result);
		ProfileImageDto writerProfileImage = resolveWriterProfileImage(result);

		return PostListResult.PostItem.of(
			result.postId(),
			result.content(),
			result.numComment(),
			result.numLike(),
			result.numFavorite(),
			result.isAnonymous(),
			result.voteId(),
			result.isDeleted(),
			writerNickname,
			writerProfileImage,
			result.createdAt(),
			result.updatedAt(),
			imageUrls,
			result.boardId(),
			result.boardName());
	}

	/**
	 * Post 엔티티와 관련 데이터를 PostDetailResult로 변환합니다.
	 *
	 * @param post 게시글 엔티티
	 * @param imageUrls 첨부 이미지 URL 리스트
	 * @param numComment 댓글 개수
	 * @param numLike 좋아요 개수
	 * @param numFavorite 즐겨찾기 개수
	 * @param isPostLike 사용자의 좋아요 여부
	 * @param isPostFavorite 사용자의 즐겨찾기 여부
	 * @param isOwner 작성자 여부
	 * @param updatable 수정 가능 여부
	 * @param deletable 삭제 가능 여부
	 * @return PostDetailResult
	 */
	public static PostDetailResult toPostDetailResult(
		Post post,
		List<String> imageUrls,
		Long numComment,
		Long numLike,
		Long numFavorite,
		Boolean isPostLike,
		Boolean isPostFavorite,
		boolean isOwner,
		boolean updatable,
		boolean deletable) {

		String displayWriterNickname = resolveWriterNickname(post);
		ProfileImageDto writerProfileImage = resolveWriterProfileImage(post);
		String voteId = resolveVoteId(post);

		return PostDetailResult.builder()
			.id(post.getId())
			.content(post.getContent())
			.isDeleted(post.getIsDeleted())
			.displayWriterNickname(displayWriterNickname)
			.writerProfileImage(writerProfileImage)
			.fileUrlList(imageUrls)
			.numComment(numComment)
			.numLike(numLike)
			.numFavorite(numFavorite)
			.voteId(voteId)
			.isAnonymous(post.getIsAnonymous())
			.isOwner(isOwner)
			.isPostLike(isPostLike)
			.isPostFavorite(isPostFavorite)
			.updatable(updatable)
			.deletable(deletable)
			.createdAt(post.getCreatedAt())
			.updatedAt(post.getUpdatedAt())
			.boardId(post.getBoard().getId())
			.boardName(post.getBoard().getName())
			.build();
	}

	// ── 비식별 처리 헬퍼 ──────────────────────────────────────────────────────────

	private static String resolveWriterNickname(PostCursorResult result) {
		if (isInactiveWriter(result.writerUserState(), !result.hasWriter())) {
			return StaticValue.INACTIVE_USER_NICKNAME;
		}
		if (result.isAnonymous()) {
			return StaticValue.ANONYMOUS_USER_NICKNAME;
		}
		return result.writerNickname();
	}

	private static ProfileImageDto resolveWriterProfileImage(PostCursorResult result) {
		if (isInactiveWriter(result.writerUserState(), !result.hasWriter()) || result.isAnonymous()) {
			return ProfileImageDto.GHOST;
		}
		return ProfileImageDto.of(result.writerProfileImageType(), result.writerProfileImageUrl());
	}

	private static String resolveWriterNickname(Post post) {
		User writer = post.getWriter();
		if (isInactiveWriter(writer)) {
			return StaticValue.INACTIVE_USER_NICKNAME;
		}
		if (post.getIsAnonymous()) {
			return StaticValue.ANONYMOUS_USER_NICKNAME;
		}
		return writer.getNickname();
	}

	private static ProfileImageDto resolveWriterProfileImage(Post post) {
		if (isInactiveWriter(post.getWriter()) || post.getIsAnonymous()) {
			return ProfileImageDto.GHOST;
		}
		return ProfileImageDto.from(post.getWriter());
	}

	private static String resolveVoteId(Post post) {
		if (post.getVote() != null) {
			return post.getVote().getId();
		}
		return null;
	}

	/**
	 * 작성자가 비활성 상태(추방/탈퇴)인지 확인합니다.
	 * <ul>
	 *   <li>state == null: 작성자가 존재하지 않음</li>
	 *   <li>state == DROP: 추방</li>
	 *   <li>isDeleted == true: 탈퇴</li>
	 * </ul>
	 */
	private static boolean isInactiveWriter(UserState state, boolean isDeleted) {
		return state == null || state == UserState.DROP || isDeleted;
	}

	private static boolean isInactiveWriter(User writer) {
		return writer == null || writer.isDeleted() || writer.getState() == UserState.DROP;
	}

}
