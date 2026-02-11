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
	 * 익명 게시판인 경우 닉네임을 "익명"으로, 프로필 사진을 null로 설정합니다.
	 */
	public static PostListResult.PostItem toPostListItem(PostCursorResult result, List<String> imageUrls) {
		String writerNickname = result.isAnonymous() ? StaticValue.ANONYMOUS_USER_NICKNAME : result.writerNickname();
		String writerProfileImageUrl = result.isAnonymous() ? null : result.writerProfileImageUrl();

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
			writerProfileImageUrl,
			result.createdAt(),
			result.updatedAt(),
			imageUrls);
	}

	/**
	 * Post 엔티티와 관련 데이터를 PostDetailResult로 변환합니다.
	 * 익명 게시글인 경우 작성자 정보를 보호합니다.
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

		// 작성자 닉네임 및 프로필 이미지 (익명 처리)
		String displayWriterNickname;
		String writerProfileImage;
		if (post.getIsAnonymous()) {
			displayWriterNickname = StaticValue.ANONYMOUS_USER_NICKNAME;
			writerProfileImage = null;
		} else {
			displayWriterNickname = post.getWriter().getNickname();
			writerProfileImage = post.getWriter().getUserProfileImage() != null
				? post.getWriter().getUserProfileImage().getUuidFile().getFileUrl()
				: null;
		}

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
			.isAnonymous(post.getIsAnonymous())
			.isOwner(isOwner)
			.isPostLike(isPostLike)
			.isPostFavorite(isPostFavorite)
			.updatable(updatable)
			.deletable(deletable)
			.createdAt(post.getCreatedAt())
			.updatedAt(post.getUpdatedAt())
			.boardName(post.getBoard().getName())
			.build();
	}

}
