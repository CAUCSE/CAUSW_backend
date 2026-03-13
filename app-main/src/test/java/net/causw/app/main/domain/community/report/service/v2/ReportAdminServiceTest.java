package net.causw.app.main.domain.community.report.service.v2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.domain.community.report.enums.ReportReason;
import net.causw.app.main.domain.community.report.repository.projection.ReportedCommentNativeProjection;
import net.causw.app.main.domain.community.report.repository.projection.ReportedPostNativeProjection;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedCommentSummaryResult;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedPostSummaryResult;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedUserListCondition;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedUserSummaryResult;
import net.causw.app.main.domain.community.report.service.v2.implementation.CommentReportReader;
import net.causw.app.main.domain.community.report.service.v2.implementation.PostReportReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.enums.user.UserState;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.UserErrorCode;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class ReportAdminServiceTest {

	@Mock
	private CommentReportReader commentReportReader;

	@Mock
	private PostReportReader postReportReader;

	@Mock
	private UserReader userReader;

	@InjectMocks
	private ReportAdminService reportAdminService;

	@Test
	@DisplayName("신고된 회원 목록 조회 시 조건에 맞는 페이징 결과를 반환한다")
	void givenCondition_whenGetReportedUserList_thenReturnPagedResult() {
		// given
		ReportedUserListCondition condition = new ReportedUserListCondition("홍길동", UserState.ACTIVE, null);
		Pageable pageable = PageRequest.of(0, 10);

		User user1 = ObjectFixtures.getCertifiedUserWithId("user-1");
		User user2 = ObjectFixtures.getCertifiedUserWithId("user-2");
		Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

		when(userReader.findReportedUserList(condition.keyword(), condition.state(), condition.academicStatus(),
			pageable))
			.thenReturn(userPage);

		// when
		Page<ReportedUserSummaryResult> result = reportAdminService.getReportedUserList(condition, pageable);

		// then
		assertThat(result.getContent()).hasSize(2)
			.extracting(ReportedUserSummaryResult::userId)
			.containsExactly("user-1", "user-2");
		verify(userReader).findReportedUserList(
			condition.keyword(),
			condition.state(),
			condition.academicStatus(),
			pageable);
	}

	@Nested
	@DisplayName("특정 회원 신고 게시글 조회")
	class GetReportedPostListByUser {

		@Test
		@DisplayName("회원이 존재하면 신고된 게시글 목록을 반환한다")
		void givenValidUserId_whenGetReportedPostListByUser_thenReturnPagedResult() {
			// given
			String userId = "user-1";
			Pageable pageable = PageRequest.of(0, 10);
			User user = ObjectFixtures.getCertifiedUserWithId(userId);

			ReportedPostNativeProjection projection = mock(ReportedPostNativeProjection.class);
			when(projection.getReportId()).thenReturn("report-1");
			when(projection.getPostId()).thenReturn("post-1");
			when(projection.getPostTitle()).thenReturn("신고된 게시글");
			when(projection.getWriterName()).thenReturn("작성자");
			when(projection.getWriterState()).thenReturn(UserState.ACTIVE);
			when(projection.getReportReason()).thenReturn(ReportReason.SPAM_AD.name());
			when(projection.getReportCreatedAt()).thenReturn(LocalDateTime.of(2026, 3, 5, 12, 0));
			when(projection.getBoardName()).thenReturn("자유게시판");
			when(projection.getBoardId()).thenReturn("board-1");

			Page<ReportedPostNativeProjection> nativePage = new PageImpl<>(List.of(projection), pageable, 1);
			when(userReader.findUserById(userId)).thenReturn(user);
			when(postReportReader.findPostReportsByUserId(userId, pageable)).thenReturn(nativePage);

			// when
			Page<ReportedPostSummaryResult> result = reportAdminService.getReportedPostListByUser(userId, pageable);

			// then
			assertThat(result.getContent()).hasSize(1);
			ReportedPostSummaryResult item = result.getContent().get(0);
			assertThat(item.reportId()).isEqualTo("report-1");
			assertThat(item.reportReasonDescription()).isEqualTo(ReportReason.SPAM_AD.getDescription());
			assertThat(item.url()).isEqualTo("/board/board-1/post-1");

			verify(userReader).findUserById(userId);
			verify(postReportReader).findPostReportsByUserId(userId, pageable);
		}

		@Test
		@DisplayName("회원이 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다")
		void givenInvalidUserId_whenGetReportedPostListByUser_thenThrowUserNotFound() {
			// given
			String invalidUserId = "invalid-user-id";
			Pageable pageable = PageRequest.of(0, 10);

			when(userReader.findUserById(invalidUserId))
				.thenThrow(UserErrorCode.USER_NOT_FOUND.toBaseException());

			// when & then
			assertThatThrownBy(() -> reportAdminService.getReportedPostListByUser(invalidUserId, pageable))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_FOUND);

			verify(userReader).findUserById(invalidUserId);
			verifyNoInteractions(postReportReader);
		}
	}

	@Nested
	@DisplayName("특정 회원 신고 댓글 조회")
	class GetReportedCommentListByUser {

		@Test
		@DisplayName("회원이 존재하면 신고된 댓글 목록을 반환한다")
		void givenValidUserId_whenGetReportedCommentListByUser_thenReturnPagedResult() {
			// given
			String userId = "user-1";
			Pageable pageable = PageRequest.of(0, 10);
			User user = ObjectFixtures.getCertifiedUserWithId(userId);

			ReportedCommentNativeProjection projection = mock(ReportedCommentNativeProjection.class);
			when(projection.getReportId()).thenReturn("report-1");
			when(projection.getContentId()).thenReturn("comment-1");
			when(projection.getContent()).thenReturn("신고된 댓글");
			when(projection.getPostTitle()).thenReturn("원글 제목");
			when(projection.getPostId()).thenReturn("post-1");
			when(projection.getBoardId()).thenReturn("board-1");
			when(projection.getWriterName()).thenReturn("작성자");
			when(projection.getWriterState()).thenReturn(UserState.ACTIVE);
			when(projection.getReportReason()).thenReturn(ReportReason.ABUSE_LANGUAGE.name());
			when(projection.getReportCreatedAt()).thenReturn(LocalDateTime.of(2026, 3, 5, 12, 0));

			Page<ReportedCommentNativeProjection> nativePage = new PageImpl<>(List.of(projection), pageable, 1);
			when(userReader.findUserById(userId)).thenReturn(user);
			when(commentReportReader.findCombinedCommentReportsByUserId(userId, pageable)).thenReturn(nativePage);

			// when
			Page<ReportedCommentSummaryResult> result = reportAdminService.getReportedCommentListByUser(userId,
				pageable);

			// then
			assertThat(result.getContent()).hasSize(1);
			ReportedCommentSummaryResult item = result.getContent().get(0);
			assertThat(item.commentId()).isEqualTo("comment-1");
			assertThat(item.reportReasonDescription()).isEqualTo(ReportReason.ABUSE_LANGUAGE.getDescription());
			assertThat(item.url()).isEqualTo("/board/board-1/post-1");

			verify(userReader).findUserById(userId);
			verify(commentReportReader).findCombinedCommentReportsByUserId(userId, pageable);
		}

		@Test
		@DisplayName("회원이 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다")
		void givenInvalidUserId_whenGetReportedCommentListByUser_thenThrowUserNotFound() {
			// given
			String invalidUserId = "invalid-user-id";
			Pageable pageable = PageRequest.of(0, 10);

			when(userReader.findUserById(invalidUserId))
				.thenThrow(UserErrorCode.USER_NOT_FOUND.toBaseException());

			// when & then
			assertThatThrownBy(() -> reportAdminService.getReportedCommentListByUser(invalidUserId, pageable))
				.isInstanceOf(BaseRunTimeV2Exception.class)
				.extracting(e -> ((BaseRunTimeV2Exception)e).getErrorCode())
				.isEqualTo(UserErrorCode.USER_NOT_FOUND);

			verify(userReader).findUserById(invalidUserId);
			verifyNoInteractions(commentReportReader);
		}
	}
}
