package net.causw.app.main.domain.community.post.service.v2.mapper;

import java.util.List;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.query.PostCursorResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
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

}
