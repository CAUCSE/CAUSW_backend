package net.causw.app.main.domain.community.comment.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.repository.query.CommentMetaQueryResult;
import net.causw.app.main.domain.community.comment.service.dto.CommentMeta;
import net.causw.app.main.domain.user.account.entity.user.User;

@ExtendWith(MockitoExtension.class)
class CommentMetaReaderTest {

	@InjectMocks
	private CommentMetaReader commentMetaReader;

	@Mock
	private LikeCommentReader likeCommentReader;

	@Test
	@DisplayName("루트 댓글과 답글 메타를 통합 ID 목록으로 한 번에 조회한다")
	void givenRootAndChildComments_whenFetch_thenQueryMetaOnce() {
		// given
		Comment rootComment = comment("root-comment-id");
		Comment childComment = comment("child-comment-id");
		given(rootComment.getChildCommentList()).willReturn(List.of(childComment));

		List<String> expectedIds = List.of("root-comment-id", "child-comment-id");
		given(likeCommentReader.getCommentMetaQueryResults(
			eq("viewer-id"),
			eq(Set.of("child-writer-id")),
			argThat(ids -> ids.containsAll(expectedIds))))
			.willReturn(List.of(
				new CommentMetaQueryResult("root-comment-id", null, 3L, false, false),
				new CommentMetaQueryResult("child-comment-id", "root-comment-id", 2L, true, true)));

		// when
		Map<String, CommentMeta> result = commentMetaReader.fetch(
			"viewer-id", Set.of("child-writer-id"), List.of(rootComment));

		// then
		CommentMeta meta = result.get("root-comment-id");
		assertThat(meta.numLike()).isEqualTo(3L);
		assertThat(meta.isLiked()).isFalse();
		assertThat(meta.isBlocked()).isFalse();
		assertThat(meta.childLikeCounts()).containsEntry("child-comment-id", 2L);
		assertThat(meta.likedChildIds()).containsExactly("child-comment-id");
		assertThat(meta.blockedChildIds()).containsExactly("child-comment-id");
		then(likeCommentReader).should().getCommentMetaQueryResults("viewer-id", Set.of("child-writer-id"),
			expectedIds);
	}

	@Test
	@DisplayName("단일 댓글과 답글 메타도 통합 ID 목록으로 한 번에 조회한다")
	void givenCommentWithChildren_whenFetchForComment_thenQueryMetaOnce() {
		// given
		User viewer = user("viewer-id");
		Comment rootComment = comment("root-comment-id");
		Comment childComment = comment("child-comment-id");
		given(rootComment.getChildCommentList()).willReturn(List.of(childComment));

		List<String> expectedIds = List.of("root-comment-id", "child-comment-id");
		given(likeCommentReader.getCommentMetaQueryResults(
			eq("viewer-id"),
			eq(Set.of("child-writer-id")),
			argThat(ids -> ids.containsAll(expectedIds))))
			.willReturn(List.of(
				new CommentMetaQueryResult("root-comment-id", null, 3L, true, false),
				new CommentMetaQueryResult("child-comment-id", "root-comment-id", 2L, false, true)));

		// when
		CommentMeta meta = commentMetaReader.fetchForComment(viewer, rootComment, Set.of("child-writer-id"));

		// then
		assertThat(meta.numLike()).isEqualTo(3L);
		assertThat(meta.isLiked()).isTrue();
		assertThat(meta.isBlocked()).isFalse();
		assertThat(meta.childLikeCounts()).containsEntry("child-comment-id", 2L);
		assertThat(meta.likedChildIds()).isEmpty();
		assertThat(meta.blockedChildIds()).containsExactly("child-comment-id");
		then(likeCommentReader).should().getCommentMetaQueryResults("viewer-id", Set.of("child-writer-id"),
			expectedIds);
	}

	private Comment comment(String id) {
		Comment comment = mock(Comment.class);
		given(comment.getId()).willReturn(id);
		return comment;
	}

	private User user(String id) {
		User user = mock(User.class);
		given(user.getId()).willReturn(id);
		return user;
	}
}
