package net.causw.app.main.domain.community.post.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.community.post.repository.query.PostQueryRepository;
import net.causw.app.main.domain.community.post.repository.query.PostReadQueryContext;
import net.causw.app.main.domain.integration.crawled.repository.CrawledPostImageRepository;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class PostReaderTest {

	@InjectMocks
	private PostReader postReader;

	@Mock
	private PostRepository postRepository;

	@Mock
	private PostQueryRepository postQueryRepository;

	@Mock
	private CrawledPostImageRepository crawledPostImageRepository;

	@Test
	@DisplayName("일반 게시글 목록 조회 컨텍스트는 실제 사용자의 관리자 여부와 읽기 범위를 반영한다")
	void findPostsWithCursor_shouldCreateReadContextFromViewer() {
		User systemAdmin = ObjectFixtures.getCertifiedUserWithId("admin-id");
		systemAdmin.setRoles(Set.of(Role.ADMIN));
		systemAdmin.setAcademicStatus(AcademicStatus.GRADUATED);
		Set<String> blockedUserIds = Set.of("blocked-user-id");
		ArgumentCaptor<PostReadQueryContext> contextCaptor = ArgumentCaptor.forClass(PostReadQueryContext.class);

		postReader.findPostsWithCursor(null, systemAdmin, blockedUserIds, null, null, 20, null);

		verify(postQueryRepository).findPostsWithCursor(
			isNull(), contextCaptor.capture(), isNull(), isNull(), eq(20), isNull());
		assertReadContext(
			contextCaptor.getValue(),
			systemAdmin,
			true,
			Set.of(BoardReadScope.BOTH, BoardReadScope.GRADUATED),
			blockedUserIds);
	}

	@Test
	@DisplayName("모든 개인화 목록은 동일한 사용자 기반 읽기 컨텍스트를 사용한다")
	void personalizedPostQueries_shouldUseSameViewerReadContext() {
		User viewer = ObjectFixtures.getCertifiedUserWithId("viewer-id");
		viewer.setRoles(Set.of(Role.COMMON));
		viewer.setAcademicStatus(AcademicStatus.ENROLLED);
		Set<String> blockedUserIds = Set.of("blocked-user-id");
		ArgumentCaptor<PostReadQueryContext> commentedContextCaptor = ArgumentCaptor
			.forClass(PostReadQueryContext.class);
		ArgumentCaptor<PostReadQueryContext> writtenContextCaptor = ArgumentCaptor.forClass(PostReadQueryContext.class);
		ArgumentCaptor<PostReadQueryContext> likedContextCaptor = ArgumentCaptor.forClass(PostReadQueryContext.class);

		postReader.findPostsCommentedByUserWithCursor(viewer, blockedUserIds, null, null, 20);
		postReader.findPostsWrittenByUserWithCursor(viewer, blockedUserIds, null, null, 20);
		postReader.findPostsLikedByUserWithCursor(viewer, blockedUserIds, null, null, 20);

		verify(postQueryRepository).findPostsCommentedByUserWithCursor(
			commentedContextCaptor.capture(), isNull(), isNull(), eq(20));
		verify(postQueryRepository).findPostsWrittenByUserWithCursor(
			writtenContextCaptor.capture(), isNull(), isNull(), eq(20));
		verify(postQueryRepository).findPostsLikedByUserWithCursor(
			likedContextCaptor.capture(), isNull(), isNull(), eq(20));

		for (PostReadQueryContext context : List.of(
			commentedContextCaptor.getValue(),
			writtenContextCaptor.getValue(),
			likedContextCaptor.getValue())) {
			assertReadContext(
				context,
				viewer,
				false,
				Set.of(BoardReadScope.BOTH, BoardReadScope.ENROLLED),
				blockedUserIds);
		}
	}

	private static void assertReadContext(
		PostReadQueryContext context,
		User viewer,
		boolean systemAdmin,
		Set<BoardReadScope> readableScopes,
		Set<String> blockedUserIds) {

		assertThat(context.viewerId()).isEqualTo(viewer.getId());
		assertThat(context.systemAdmin()).isEqualTo(systemAdmin);
		assertThat(context.readableScopes()).isEqualTo(readableScopes);
		assertThat(context.blockedWriterIds()).isEqualTo(blockedUserIds);
	}
}
