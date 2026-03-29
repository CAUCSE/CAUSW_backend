package net.causw.app.main.domain.notification.notification.service.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.implementation.ChildCommentReader;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.CommentChildCommentCreatedEvent;
import net.causw.app.main.domain.notification.notification.event.PostCommentCreatedEvent;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.v2.implementation.BlockReader;

@ExtendWith(MockitoExtension.class)
class CommentNotificationHandlerTest {

	@InjectMocks
	private CommentNotificationHandler handler;

	@Mock
	private PostReader postReader;
	@Mock
	private CommentReader commentReader;
	@Mock
	private ChildCommentReader childCommentReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;
	@Mock
	private NotificationSettingReader notificationSettingReader;
	@Mock
	private BlockReader blockReader;

	// ─────────────────────────────────────────────────
	// handleComment
	// ─────────────────────────────────────────────────

	@Nested
	@DisplayName("게시글 댓글 알림 (handleComment)")
	class HandleCommentTest {

		@Test
		@DisplayName("성공: 정상 조건에서 알림 저장 및 푸시 발송")
		void givenValidCondition_whenHandleComment_thenSendNotification() {
			// given
			User postWriter = mockUserWithId("postWriterId");
			User commentWriter = mockUserWithId("commentWriterId");
			Post post = mockPost(postWriter, false);
			Comment comment = mockComment(commentWriter, "댓글 내용");

			given(postReader.findById("postId")).willReturn(post);
			given(commentReader.getComment("commentId")).willReturn(comment);
			given(notificationSettingReader.findSettingMap("postWriterId"))
				.willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, commentWriter)).willReturn(false);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleComment(new PostCommentCreatedEvent("postId", "commentId"));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUser(any(), any(), any());
			verify(notificationWriter).saveLog(any(), any());
		}

		@Test
		@DisplayName("스킵: 댓글 작성자와 게시글 작성자가 동일하면 알림 미발송")
		void givenSameWriter_whenHandleComment_thenSkip() {
			// given
			User sameUser = mockUserWithId("userId");
			Post post = mockPost(sameUser, false);
			Comment comment = mockComment(sameUser, "댓글");

			given(postReader.findById("postId")).willReturn(post);
			given(commentReader.getComment("commentId")).willReturn(comment);

			// when
			handler.handleComment(new PostCommentCreatedEvent("postId", "commentId"));

			// then
			verify(notificationWriter, never()).save(any());
			verify(notificationPushSender, never()).sendToUser(any(), any(), any());
		}

		@Test
		@DisplayName("스킵: 게시글 작성자가 댓글 알림 설정 OFF면 알림 미발송")
		void givenCommentNotificationDisabled_whenHandleComment_thenSkip() {
			// given
			User postWriter = mockUserWithId("postWriterId");
			User commentWriter = mockUserWithId("commentWriterId");
			Post post = mockPost(postWriter, false);
			Comment comment = mockComment(commentWriter, "댓글");

			given(postReader.findById("postId")).willReturn(post);
			given(commentReader.getComment("commentId")).willReturn(comment);
			given(notificationSettingReader.findSettingMap("postWriterId"))
				.willReturn(settingMapWith(UserNotificationSettingKey.COMMUNITY_COMMENT_ON_MY_POST, false));

			// when
			handler.handleComment(new PostCommentCreatedEvent("postId", "commentId"));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 게시글 작성자가 댓글 작성자를 차단한 경우 알림 미발송")
		void givenPostWriterBlockedCommentWriter_whenHandleComment_thenSkip() {
			// given
			User postWriter = mockUserWithId("postWriterId");
			User commentWriter = mockUserWithId("commentWriterId");
			Post post = mockPost(postWriter, false);
			Comment comment = mockComment(commentWriter, "댓글");

			given(postReader.findById("postId")).willReturn(post);
			given(commentReader.getComment("commentId")).willReturn(comment);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, commentWriter)).willReturn(true);

			// when
			handler.handleComment(new PostCommentCreatedEvent("postId", "commentId"));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("성공: 익명 게시글이면 댓글 작성자 이름 대신 익명 표시")
		void givenAnonymousPost_whenHandleComment_thenUseAnonymousDisplayName() {
			// given
			User postWriter = mockUserWithId("postWriterId");
			User commentWriter = mockUserWithId("commentWriterId");
			Post post = mockPost(postWriter, true); // 익명 게시글
			Comment comment = mockComment(commentWriter, "댓글 내용");

			given(postReader.findById("postId")).willReturn(post);
			given(commentReader.getComment("commentId")).willReturn(comment);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, commentWriter)).willReturn(false);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleComment(new PostCommentCreatedEvent("postId", "commentId"));

			// then
			verify(notificationWriter).save(any());
		}
	}

	// ─────────────────────────────────────────────────
	// handleChildComment
	// ─────────────────────────────────────────────────

	@Nested
	@DisplayName("대댓글 알림 (handleChildComment)")
	class HandleChildCommentTest {

		@Test
		@DisplayName("성공: 정상 조건에서 알림 저장 및 푸시 발송")
		void givenValidCondition_whenHandleChildComment_thenSendNotification() {
			// given
			User commentWriter = mockUserWithId("commentWriterId");
			User childCommentWriter = mockUserWithId("childCommentWriterId");
			Post post = mockPost(mock(User.class), false);
			Comment comment = mockCommentWithPost(commentWriter, "원댓글", post);
			ChildComment childComment = mockChildComment(childCommentWriter, "대댓글 내용");

			given(commentReader.getComment("commentId")).willReturn(comment);
			given(childCommentReader.findById("childCommentId")).willReturn(childComment);
			given(notificationSettingReader.findSettingMap("commentWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(commentWriter, childCommentWriter)).willReturn(false);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handleChildComment(new CommentChildCommentCreatedEvent("commentId", "childCommentId"));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUser(any(), any(), any());
			verify(notificationWriter).saveLog(any(), any());
		}

		@Test
		@DisplayName("스킵: 대댓글 작성자와 댓글 작성자가 동일하면 알림 미발송")
		void givenSameWriter_whenHandleChildComment_thenSkip() {
			// given
			User sameUser = mockUserWithId("userId");
			Post post = mockPost(mock(User.class), false);
			Comment comment = mockCommentWithPost(sameUser, "원댓글", post);
			ChildComment childComment = mockChildComment(sameUser, "대댓글");

			given(commentReader.getComment("commentId")).willReturn(comment);
			given(childCommentReader.findById("childCommentId")).willReturn(childComment);

			// when
			handler.handleChildComment(new CommentChildCommentCreatedEvent("commentId", "childCommentId"));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 댓글 작성자가 대댓글 알림 설정 OFF면 알림 미발송")
		void givenReplyNotificationDisabled_whenHandleChildComment_thenSkip() {
			// given
			User commentWriter = mockUserWithId("commentWriterId");
			User childCommentWriter = mockUserWithId("childCommentWriterId");
			Post post = mockPost(mock(User.class), false);
			Comment comment = mockCommentWithPost(commentWriter, "원댓글", post);
			ChildComment childComment = mockChildComment(childCommentWriter, "대댓글");

			given(commentReader.getComment("commentId")).willReturn(comment);
			given(childCommentReader.findById("childCommentId")).willReturn(childComment);
			given(notificationSettingReader.findSettingMap("commentWriterId"))
				.willReturn(settingMapWith(UserNotificationSettingKey.COMMUNITY_REPLY_ON_MY_COMMENT, false));

			// when
			handler.handleChildComment(new CommentChildCommentCreatedEvent("commentId", "childCommentId"));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 댓글 작성자가 대댓글 작성자를 차단한 경우 알림 미발송")
		void givenCommentWriterBlockedChildCommentWriter_whenHandleChildComment_thenSkip() {
			// given
			User commentWriter = mockUserWithId("commentWriterId");
			User childCommentWriter = mockUserWithId("childCommentWriterId");
			Post post = mockPost(mock(User.class), false);
			Comment comment = mockCommentWithPost(commentWriter, "원댓글", post);
			ChildComment childComment = mockChildComment(childCommentWriter, "대댓글");

			given(commentReader.getComment("commentId")).willReturn(comment);
			given(childCommentReader.findById("childCommentId")).willReturn(childComment);
			given(notificationSettingReader.findSettingMap("commentWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(commentWriter, childCommentWriter)).willReturn(true);

			// when
			handler.handleChildComment(new CommentChildCommentCreatedEvent("commentId", "childCommentId"));

			// then
			verify(notificationWriter, never()).save(any());
		}
	}

	// ─────────────────────────────────────────────────
	// 헬퍼
	// ─────────────────────────────────────────────────

	private User mockUserWithId(String id) {
		User user = mock(User.class);
		given(user.getId()).willReturn(id);
		given(user.getNickname()).willReturn("닉네임");
		return user;
	}

	private Post mockPost(User writer, boolean isAnonymous) {
		Post post = mock(Post.class);
		given(post.getWriter()).willReturn(writer);
		given(post.getIsAnonymous()).willReturn(isAnonymous);
		given(post.getId()).willReturn("postId");
		var board = mock(net.causw.app.main.domain.community.board.entity.Board.class);
		given(board.getId()).willReturn("boardId");
		given(post.getBoard()).willReturn(board);
		return post;
	}

	private Comment mockComment(User writer, String content) {
		Post post = mockPost(writer, false);
		return mockCommentWithPost(writer, content, post);
	}

	private Comment mockCommentWithPost(User writer, String content, Post post) {
		Comment comment = mock(Comment.class);
		given(comment.getWriter()).willReturn(writer);
		given(comment.getContent()).willReturn(content);
		given(comment.getPost()).willReturn(post);
		return comment;
	}

	private ChildComment mockChildComment(User writer, String content) {
		ChildComment childComment = mock(ChildComment.class);
		given(childComment.getWriter()).willReturn(writer);
		given(childComment.getContent()).willReturn(content);
		return childComment;
	}

	private UserNotificationSettingMap settingMapAllOn() {
		return UserNotificationSettingMap.ofFull(Map.of());
	}

	private UserNotificationSettingMap settingMapWith(UserNotificationSettingKey key, boolean value) {
		return UserNotificationSettingMap.ofFull(Map.of(key, value));
	}
}
