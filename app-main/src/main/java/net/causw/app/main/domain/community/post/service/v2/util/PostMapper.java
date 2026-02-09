package net.causw.app.main.domain.community.post.service.v2.util;

import java.util.List;

import net.causw.app.main.domain.asset.file.entity.UuidFile;
import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateCommand;
import net.causw.app.main.domain.community.post.service.v2.dto.PostCreateResult;
import net.causw.app.main.domain.community.post.service.v2.dto.PostUpdateResult;
import net.causw.app.main.domain.user.account.entity.user.User;

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

}
