package net.causw.app.main.mapper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.model.entity.board.Board;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.entity.uuidFile.joinEntity.PostAttachImage;
import net.causw.app.main.domain.model.enums.user.Role;
import net.causw.app.main.domain.model.enums.uuidFile.FileExtensionType;
import net.causw.app.main.domain.policy.StatusPolicy;
import net.causw.app.main.dto.post.BoardPostsResponseDto;
import net.causw.app.main.dto.post.PostsResponseDto;
import net.causw.app.main.dto.util.dtoMapper.PostDtoMapper;

@Component
public class BoardPostsResponseMapper {

	public BoardPostsResponseDto toBoardPostsResponseDto(
		Board board,
		Set<Role> userRoles,
		Boolean isFavorite,
		Boolean isBoardSubscribed,
		Page<Post> posts,
		Map<String, Long> commentCounts,
		Map<String, Long> likeCounts,
		Map<String, Long> favoriteCounts) {

		List<String> roles = Arrays.asList(board.getCreateRoles().split(","));
		Boolean writable = userRoles.stream()
			.map(Role::getValue)
			.anyMatch(roles::contains);
		Page<PostsResponseDto> postsResponseDtos = posts.map(
			post -> toPostsResponseDto(post, commentCounts, likeCounts, favoriteCounts));

		return PostDtoMapper.INSTANCE.toBoardPostsResponseDto(
			board,
			userRoles,
			writable,
			isFavorite,
			isBoardSubscribed,
			postsResponseDtos
		);
	}

	private PostsResponseDto toPostsResponseDto(Post post,
		Map<String, Long> commentCounts,
		Map<String, Long> likeCounts,
		Map<String, Long> favoriteCounts
	) {
		PostAttachImage postThumbnailFile =
			(post.getPostAttachImageList() == null || post.getPostAttachImageList().isEmpty()) ?
				null :
				post.getPostAttachImageList()
					.stream()
					.filter(postAttachImage ->
						FileExtensionType.IMAGE.getExtensionList()
							.contains(postAttachImage.getUuidFile().getExtension())
					)
					.sorted(Comparator.comparing(PostAttachImage::getCreatedAt)) // 오름차순 정렬
					.findFirst()
					.orElse(null);

		String postId = post.getId();
		PostsResponseDto postsResponseDto = PostDtoMapper.INSTANCE.toPostsResponseDto(
			post,
			commentCounts.getOrDefault(postId, 0L),
			likeCounts.getOrDefault(postId, 0L),
			favoriteCounts.getOrDefault(postId, 0L),
			postThumbnailFile,
			StatusPolicy.isPostVote(post),
			StatusPolicy.isPostForm(post)
		);

		// 화면에 표시할 작성자 닉네임 설정
		User writer = post.getWriter();

		String displayWriterNicName = writer.getDisplayWriterNicName(postsResponseDto.getIsAnonymous());
		postsResponseDto.setDisplayWriterNickname(displayWriterNicName);

		if (postsResponseDto.getIsAnonymous()) {
			postsResponseDto.updateAnonymousWriterInfo();
		}

		return postsResponseDto;
	}
}
