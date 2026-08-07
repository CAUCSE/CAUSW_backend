package net.causw.app.main.domain.notification.notification.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.notification.notification.entity.Notification;
import net.causw.app.main.domain.notification.notification.entity.PostLikeMilestoneAchievement;
import net.causw.app.main.domain.notification.notification.enums.NoticeType;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneAchievementStatus;
import net.causw.app.main.domain.notification.notification.enums.PostLikeMilestoneSuppressionReason;
import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;
import net.causw.app.main.domain.notification.notification.event.PostLikeMilestonePushEvent;
import net.causw.app.main.domain.notification.notification.service.dto.PushNotificationData;
import net.causw.app.main.domain.notification.notification.service.dto.UserNotificationSettingMap;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.relation.service.implementation.BlockReader;

@ExtendWith(MockitoExtension.class)
class PostLikeMilestoneNotificationProcessorTest {

	@InjectMocks
	private PostLikeMilestoneNotificationProcessor processor;

	@Mock
	private PostLikeMilestoneAchievementReader achievementReader;
	@Mock
	private PostLikeMilestoneAchievementWriter achievementWriter;
	@Mock
	private NotificationWriter notificationWriter;
	@Mock
	private NotificationSettingReader notificationSettingReader;
	@Mock
	private BlockReader blockReader;
	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Nested
	@DisplayName("게시글 좋아요 마일스톤 알림 처리")
	class ProcessTest {

		@ParameterizedTest(name = "기록된 마일스톤 {0}개 처리")
		@ValueSource(longs = {5, 10, 50, 100, 500, 1000, 2000, 3000})
		@DisplayName("알림·로그·이력 결과를 저장하고 커밋 후 푸시 이벤트를 예약한다")
		void givenValidPendingAchievement_whenProcess_thenPersistAndPublishPushEvent(long milestoneCount) {
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = alivePostWithWriter(postWriter);
			given(post.getId()).willReturn("postId");
			Board board = post.getBoard();
			given(board.getId()).willReturn("boardId");

			PostLikeMilestoneAchievement achievement = pendingAchievement(post, liker);
			given(achievement.getMilestoneCount()).willReturn(milestoneCount);
			given(achievementReader.findById("achievementId")).willReturn(achievement);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, liker)).willReturn(false);
			Notification notification = mock(Notification.class);
			given(notificationWriter.save(any(Notification.class))).willReturn(notification);

			processor.process("achievementId");

			PushNotificationData expectedData = new PushNotificationData(
				NoticeType.COMMUNITY,
				"postId",
				"boardId");
			PostLikeMilestonePushEvent expectedEvent = new PostLikeMilestonePushEvent(
				"postWriterId",
				String.format("게시물 좋아요 %d개 달성", milestoneCount),
				String.format("내 게시글에 좋아요가 %d개 달렸어요.", milestoneCount),
				expectedData);

			InOrder inOrder = inOrder(notificationWriter, achievementWriter, eventPublisher);
			inOrder.verify(notificationWriter).save(any(Notification.class));
			inOrder.verify(notificationWriter).saveLog(postWriter, notification);
			inOrder.verify(achievementWriter).markNotificationCreated(achievement, notification);
			inOrder.verify(eventPublisher).publishEvent(expectedEvent);
			verify(achievementWriter, never()).suppress(any(), any());
		}

		@Test
		@DisplayName("알림 로그 저장이 실패하면 푸시 이벤트를 예약하지 않는다")
		void givenNotificationLogFailure_whenProcess_thenDoNotPublishPushEvent() {
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = alivePostWithWriter(postWriter);
			given(post.getId()).willReturn("postId");
			Board board = post.getBoard();
			given(board.getId()).willReturn("boardId");
			PostLikeMilestoneAchievement achievement = pendingAchievement(post, liker);
			given(achievement.getMilestoneCount()).willReturn(5L);
			given(achievementReader.findById("achievementId")).willReturn(achievement);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, liker)).willReturn(false);
			Notification notification = mock(Notification.class);
			given(notificationWriter.save(any(Notification.class))).willReturn(notification);
			willThrow(new RuntimeException("notification log save failed"))
				.given(notificationWriter).saveLog(postWriter, notification);

			assertThatThrownBy(() -> processor.process("achievementId"))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("notification log save failed");

			verify(achievementWriter, never()).markNotificationCreated(any(), any());
			verifyNoInteractions(eventPublisher);
		}

		@Test
		@DisplayName("이미 처리된 이력은 다시 처리하지 않는다")
		void givenCompletedAchievement_whenProcess_thenSkip() {
			PostLikeMilestoneAchievement achievement = mock(PostLikeMilestoneAchievement.class);
			given(achievement.getStatus()).willReturn(PostLikeMilestoneAchievementStatus.NOTIFICATION_CREATED);
			given(achievementReader.findById("achievementId")).willReturn(achievement);

			processor.process("achievementId");

			verifyNoInteractions(achievementWriter, notificationWriter, notificationSettingReader, blockReader,
				eventPublisher);
		}

		@Test
		@DisplayName("삭제된 게시글은 TARGET_UNAVAILABLE로 소비한다")
		void givenDeletedPost_whenProcess_thenSuppressAsTargetUnavailable() {
			User postWriter = mock(User.class);
			User liker = mock(User.class);
			Post post = mock(Post.class);
			given(post.getWriter()).willReturn(postWriter);
			given(post.getIsDeleted()).willReturn(true);
			PostLikeMilestoneAchievement achievement = pendingAchievement(post, liker);
			given(achievementReader.findById("achievementId")).willReturn(achievement);

			processor.process("achievementId");

			verify(achievementWriter).suppress(
				achievement,
				PostLikeMilestoneSuppressionReason.TARGET_UNAVAILABLE);
			verifyNoInteractions(notificationWriter, notificationSettingReader, blockReader, eventPublisher);
		}

		@Test
		@DisplayName("좋아요 사용자가 없으면 TARGET_UNAVAILABLE로 소비한다")
		void givenMissingLiker_whenProcess_thenSuppressAsTargetUnavailable() {
			User postWriter = mock(User.class);
			Post post = mock(Post.class);
			given(post.getWriter()).willReturn(postWriter);
			PostLikeMilestoneAchievement achievement = pendingAchievement(post, null);
			given(achievementReader.findById("achievementId")).willReturn(achievement);

			processor.process("achievementId");

			verify(achievementWriter).suppress(
				achievement,
				PostLikeMilestoneSuppressionReason.TARGET_UNAVAILABLE);
			verifyNoInteractions(notificationWriter, notificationSettingReader, blockReader, eventPublisher);
		}

		@ParameterizedTest(name = "작성자 상태: {0}")
		@ValueSource(strings = {"inactive", "dropped"})
		@DisplayName("탈퇴 또는 추방된 작성자는 TARGET_UNAVAILABLE로 소비한다")
		void givenUnavailableWriter_whenProcess_thenSuppressAsTargetUnavailable(String writerState) {
			User postWriter = mock(User.class);
			if ("inactive".equals(writerState)) {
				given(postWriter.isInactive()).willReturn(true);
			} else {
				given(postWriter.isDropped()).willReturn(true);
			}
			User liker = mock(User.class);
			Post post = alivePostWithWriter(postWriter);
			PostLikeMilestoneAchievement achievement = pendingAchievement(post, liker);
			given(achievementReader.findById("achievementId")).willReturn(achievement);

			processor.process("achievementId");

			verify(achievementWriter).suppress(
				achievement,
				PostLikeMilestoneSuppressionReason.TARGET_UNAVAILABLE);
			verifyNoInteractions(notificationWriter, notificationSettingReader, blockReader, eventPublisher);
		}

		@Test
		@DisplayName("작성자의 본인 좋아요는 SELF_LIKE로 소비한다")
		void givenSelfLike_whenProcess_thenSuppressAsSelfLike() {
			User postWriter = userWithId("userId");
			Post post = alivePostWithWriter(postWriter);
			PostLikeMilestoneAchievement achievement = pendingAchievement(post, postWriter);
			given(achievementReader.findById("achievementId")).willReturn(achievement);

			processor.process("achievementId");

			verify(achievementWriter).suppress(achievement, PostLikeMilestoneSuppressionReason.SELF_LIKE);
			verifyNoInteractions(notificationWriter, notificationSettingReader, blockReader, eventPublisher);
		}

		@Test
		@DisplayName("좋아요 알림 설정 OFF는 SETTING_DISABLED로 소비한다")
		void givenNotificationDisabled_whenProcess_thenSuppressAsSettingDisabled() {
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = alivePostWithWriter(postWriter);
			PostLikeMilestoneAchievement achievement = pendingAchievement(post, liker);
			given(achievementReader.findById("achievementId")).willReturn(achievement);
			given(notificationSettingReader.findSettingMap("postWriterId"))
				.willReturn(settingMapWith(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST, false));

			processor.process("achievementId");

			verify(achievementWriter).suppress(
				achievement,
				PostLikeMilestoneSuppressionReason.SETTING_DISABLED);
			verifyNoInteractions(notificationWriter, blockReader, eventPublisher);
		}

		@Test
		@DisplayName("작성자가 좋아요 사용자를 차단한 경우 BLOCKED로 소비한다")
		void givenBlockedLiker_whenProcess_thenSuppressAsBlocked() {
			User postWriter = userWithId("postWriterId");
			User liker = userWithId("likerId");
			Post post = alivePostWithWriter(postWriter);
			PostLikeMilestoneAchievement achievement = pendingAchievement(post, liker);
			given(achievementReader.findById("achievementId")).willReturn(achievement);
			given(notificationSettingReader.findSettingMap("postWriterId")).willReturn(settingMapAllOn());
			given(blockReader.existsByBlockerAndBlocked(postWriter, liker)).willReturn(true);

			processor.process("achievementId");

			verify(achievementWriter).suppress(achievement, PostLikeMilestoneSuppressionReason.BLOCKED);
			verifyNoInteractions(notificationWriter, eventPublisher);
		}
	}

	@Test
	@DisplayName("알림 처리는 이력 기록과 분리된 독립 트랜잭션에서 실행된다")
	void process_shouldUseRequiresNewTransaction() throws NoSuchMethodException {
		Method processMethod = PostLikeMilestoneNotificationProcessor.class
			.getMethod("process", String.class);

		Transactional annotation = processMethod.getAnnotation(Transactional.class);

		assertThat(annotation).isNotNull();
		assertThat(annotation.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
	}

	private User userWithId(String id) {
		User user = mock(User.class);
		given(user.getId()).willReturn(id);
		return user;
	}

	private Post alivePostWithWriter(User writer) {
		Board board = mock(Board.class);
		given(board.getIsDeleted()).willReturn(false);
		Post post = mock(Post.class);
		given(post.getIsDeleted()).willReturn(false);
		given(post.getBoard()).willReturn(board);
		given(post.getWriter()).willReturn(writer);
		return post;
	}

	private PostLikeMilestoneAchievement pendingAchievement(Post post, User triggerUser) {
		PostLikeMilestoneAchievement achievement = mock(PostLikeMilestoneAchievement.class);
		given(achievement.getStatus()).willReturn(PostLikeMilestoneAchievementStatus.PENDING);
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
