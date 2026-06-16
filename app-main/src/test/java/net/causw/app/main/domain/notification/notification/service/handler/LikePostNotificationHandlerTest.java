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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.reaction.service.implementation.LikePostReader;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.PostLikedEvent;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;

@ExtendWith(MockitoExtension.class)
class LikePostNotificationHandlerTest {

	@InjectMocks
	private LikePostNotificationHandler handler;

	@Mock
	private PostReader postReader;
	@Mock
	private UserReader userReader;
	@Mock
	private LikePostReader likePostReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;
	@Mock
	private NotificationSettingReader notificationSettingReader;
	@Mock
	private BlockReader blockReader;

	@Nested
	@DisplayName("게시글 좋아요 알림 (handle)")
	class HandleTest {

		@ParameterizedTest(name = "좋아요 수 {0}개 달성 시 알림 발송")
		@ValueSource(longs = {5, 10, 50, 100, 500, 1000, 2000, 3000})
		@DisplayName("성공: 마일스톤 도달 시 알림 저장 및 푸시 발송")
		void givenMilestoneCount_whenHandle_thenSendNotification(long likeCount) {
			// given
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = postWithWriter(postWriter);
			// 성공 경로에서만 사용되는 stub
			given(post.getId()).willReturn("postId");
			Board board = mock(Board.class);
			given(board.getId()).willReturn("boardId");
			given(post.getBoard()).willReturn(board);

			given(postReader.findById("postId")).willReturn(post);
			given(userReader.findUserById("likerId")).willReturn(liker);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, liker)).willReturn(false);
			given(likePostReader.countByPostId("postId")).willReturn(likeCount);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			// when
			handler.handle(new PostLikedEvent("postId", "likerId"));

			// then
			verify(notificationWriter).save(any());
			verify(notificationPushSender).sendToUser(any(), any(), any());
			verify(notificationWriter).saveLog(any(), any());
		}

		@ParameterizedTest(name = "좋아요 수 {0}개 - 마일스톤 미도달 시 알림 미발송")
		@ValueSource(longs = {1, 2, 3, 4, 6, 9, 11, 49, 51, 99, 101, 499, 501, 999, 1001})
		@DisplayName("스킵: 마일스톤 미도달 시 알림 미발송")
		void givenNonMilestoneCount_whenHandle_thenSkip(long likeCount) {
			// given
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = postWithWriter(postWriter);
			given(post.getId()).willReturn("postId");

			given(postReader.findById("postId")).willReturn(post);
			given(userReader.findUserById("likerId")).willReturn(liker);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, liker)).willReturn(false);
			given(likePostReader.countByPostId("postId")).willReturn(likeCount);

			// when
			handler.handle(new PostLikedEvent("postId", "likerId"));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 게시글 작성자가 좋아요를 누른 경우 알림 미발송")
		void givenPostWriterLiked_whenHandle_thenSkip() {
			// given
			User postWriter = userWithId("userId");
			Post post = postWithWriter(postWriter);

			given(postReader.findById("postId")).willReturn(post);
			given(userReader.findUserById("userId")).willReturn(postWriter);

			// when
			handler.handle(new PostLikedEvent("postId", "userId"));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 게시글 작성자가 좋아요 알림 설정 OFF면 알림 미발송")
		void givenLikeNotificationDisabled_whenHandle_thenSkip() {
			// given
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = postWithWriter(postWriter);

			given(postReader.findById("postId")).willReturn(post);
			given(userReader.findUserById("likerId")).willReturn(liker);
			given(notificationSettingReader.findSettingMap("postWriterId"))
				.willReturn(settingMapWith(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST, false));

			// when
			handler.handle(new PostLikedEvent("postId", "likerId"));

			// then
			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 게시글 작성자가 좋아요 누른 유저를 차단한 경우 알림 미발송")
		void givenPostWriterBlockedLiker_whenHandle_thenSkip() {
			// given
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = postWithWriter(postWriter);

			given(postReader.findById("postId")).willReturn(post);
			given(userReader.findUserById("likerId")).willReturn(liker);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, liker)).willReturn(true);

			// when
			handler.handle(new PostLikedEvent("postId", "likerId"));

			// then
			verify(notificationWriter, never()).save(any());
		}
	}

	// ─────────────────────────────────────────────────
	// 헬퍼
	// ─────────────────────────────────────────────────

	private User userWithId(String id) {
		User user = mock(User.class);
		given(user.getId()).willReturn(id);
		return user;
	}

	/** getWriter()만 stub */
	private Post postWithWriter(User writer) {
		Post post = mock(Post.class);
		given(post.getWriter()).willReturn(writer);
		return post;
	}

	private UserNotificationSettingMap settingMapAllOn() {
		return UserNotificationSettingMap.ofFull(Map.of());
	}

	private UserNotificationSettingMap settingMapWith(UserNotificationSettingKey key, boolean value) {
		return UserNotificationSettingMap.ofFull(Map.of(key, value));
	}
}
