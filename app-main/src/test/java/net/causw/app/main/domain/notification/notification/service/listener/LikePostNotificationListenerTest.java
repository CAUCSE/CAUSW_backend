package net.causw.app.main.domain.notification.notification.service.listener;

import static org.mockito.ArgumentMatchers.*;
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
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestoneNotificationEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationPushSender;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationSettingReader;
import net.causw.app.main.domain.notification.notification.service.implementation.NotificationWriter;
import net.causw.app.main.domain.notification.notification.service.implementation.PostLikeMilestoneAchievementReader;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;

@ExtendWith(MockitoExtension.class)
class LikePostNotificationListenerTest {

	@InjectMocks
	private LikePostNotificationListener handler;

	@Mock
	private PostLikeMilestoneAchievementReader achievementReader;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationPushSender notificationPushSender;
	@Mock
	private NotificationSettingReader notificationSettingReader;
	@Mock
	private BlockReader blockReader;

	@Nested
	@DisplayName("게시글 좋아요 마일스톤 알림 (handle)")
	class HandleTest {

		@ParameterizedTest(name = "기록된 마일스톤 {0}개 알림 발송")
		@ValueSource(longs = {5, 10, 50, 100, 500, 1000, 2000, 3000})
		@DisplayName("성공: 이력에 기록된 마일스톤으로 알림 저장 및 푸시 발송")
		void givenRecordedMilestone_whenHandle_thenSendNotification(long milestoneCount) {
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = postWithWriter(postWriter);
			given(post.getId()).willReturn("postId");
			Board board = mock(Board.class);
			given(board.getId()).willReturn("boardId");
			given(post.getBoard()).willReturn(board);

			PostLikeMilestoneAchievement achievement = achievement(post, liker);
			given(achievement.getMilestoneCount()).willReturn(milestoneCount);
			given(achievementReader.findById("achievementId")).willReturn(achievement);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, liker)).willReturn(false);
			given(notificationWriter.save(any())).willReturn(mock(Notification.class));

			handler.handle(new PostLikeMilestoneNotificationEvent("achievementId"));

			verify(notificationWriter).save(any());
			PushNotificationData expectedData = new PushNotificationData(
				NoticeType.COMMUNITY,
				"postId",
				"boardId");
			verify(notificationPushSender).sendToUser(any(), any(), any(), eq(expectedData));
			verify(notificationWriter).saveLog(any(), any());
		}

		@Test
		@DisplayName("스킵: 게시글 작성자가 좋아요를 누른 경우 알림 미발송")
		void givenPostWriterLiked_whenHandle_thenSkip() {
			User postWriter = userWithId("userId");
			Post post = postWithWriter(postWriter);
			PostLikeMilestoneAchievement achievement = achievement(post, postWriter);
			given(achievementReader.findById("achievementId")).willReturn(achievement);

			handler.handle(new PostLikeMilestoneNotificationEvent("achievementId"));

			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 게시글 작성자가 좋아요 알림 설정 OFF면 알림 미발송")
		void givenLikeNotificationDisabled_whenHandle_thenSkip() {
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = postWithWriter(postWriter);
			PostLikeMilestoneAchievement achievement = achievement(post, liker);
			given(achievementReader.findById("achievementId")).willReturn(achievement);
			given(notificationSettingReader.findSettingMap("postWriterId"))
				.willReturn(settingMapWith(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST, false));

			handler.handle(new PostLikeMilestoneNotificationEvent("achievementId"));

			verify(notificationWriter, never()).save(any());
		}

		@Test
		@DisplayName("스킵: 게시글 작성자가 좋아요 누른 유저를 차단한 경우 알림 미발송")
		void givenPostWriterBlockedLiker_whenHandle_thenSkip() {
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = postWithWriter(postWriter);
			PostLikeMilestoneAchievement achievement = achievement(post, liker);
			given(achievementReader.findById("achievementId")).willReturn(achievement);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, liker)).willReturn(true);

			handler.handle(new PostLikeMilestoneNotificationEvent("achievementId"));

			verify(notificationWriter, never()).save(any());
		}
	}

	private User userWithId(String id) {
		User user = mock(User.class);
		given(user.getId()).willReturn(id);
		return user;
	}

	private Post postWithWriter(User writer) {
		Post post = mock(Post.class);
		given(post.getWriter()).willReturn(writer);
		return post;
	}

	private PostLikeMilestoneAchievement achievement(Post post, User triggerUser) {
		PostLikeMilestoneAchievement achievement = mock(PostLikeMilestoneAchievement.class);
		given(achievement.getPost()).willReturn(post);
		given(achievement.getTriggerUser()).willReturn(triggerUser);
		return achievement;
	}

	private UserNotificationSettingMap settingMapAllOn() {
		return UserNotificationSettingMap.ofFull(Map.of());
	}

	private UserNotificationSettingMap settingMapWith(UserNotificationSettingKey key, boolean value) {
		return UserNotificationSettingMap.ofFull(Map.of(key, value));
	}
}
