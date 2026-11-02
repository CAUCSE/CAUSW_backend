package net.causw.app.main.domain.notification.notification.enums;

import java.util.Arrays;
import java.util.Optional;

import net.causw.app.main.domain.community.post.enums.PostCategory;

import lombok.Getter;

@Getter
public enum UserNotificationSettingKey {

	COMMUNITY_LIKE_ON_MY_POST("내 글에 좋아요", true),
	COMMUNITY_COMMENT_ON_MY_POST("내 글에 댓글", true),
	COMMUNITY_REPLY_ON_MY_COMMENT("내 댓글의 대댓글", true),
	CEREMONY_NOTIFICATION_ENABLED("경조사 알림", true),
	SERVICE_NOTICE_ENABLED("서비스 공지 알림", true),

	// 소식 탭 전체 토글. 꺼져 있으면 채널·성격 설정과 무관하게 소식 알림을 받지 않는다.
	NEWS_NOTIFICATION_ENABLED("소식 알림 받기", true),

	// 소식 성격별 알림. PostCategory.ETC는 사용자 설정 대상이 아니다.
	NEWS_CATEGORY_RECRUIT("채용", true, PostCategory.RECRUIT),
	NEWS_CATEGORY_ACADEMIC("학사", true, PostCategory.ACADEMIC),
	NEWS_CATEGORY_EVENT_LECTURE("행사·특강", true, PostCategory.EVENT_LECTURE),
	NEWS_CATEGORY_EXTERNAL_ACTIVITY("대외활동", true, PostCategory.EXTERNAL_ACTIVITY),
	NEWS_CATEGORY_RESEARCH("연구", true, PostCategory.RESEARCH);

	private final String label;
	private final boolean defaultEnabled;
	private final PostCategory newsCategory;

	UserNotificationSettingKey(String label, boolean defaultEnabled) {
		this(label, defaultEnabled, null);
	}

	UserNotificationSettingKey(String label, boolean defaultEnabled, PostCategory newsCategory) {
		this.label = label;
		this.defaultEnabled = defaultEnabled;
		this.newsCategory = newsCategory;
	}

	/**
	 * 게시글 성격에 대응하는 소식 성격별 알림 설정 키를 반환한다.
	 *
	 * @param category 게시글 성격 (null 가능)
	 * @return 대응하는 설정 키. 미분류(null)이거나 설정 대상이 아닌 성격(ETC)이면 empty
	 */
	public static Optional<UserNotificationSettingKey> fromNewsCategory(PostCategory category) {
		if (category == null) {
			return Optional.empty();
		}
		return Arrays.stream(values())
			.filter(key -> key.newsCategory == category)
			.findFirst();
	}

	/**
	 * 소식 성격별 알림 설정 키인지 여부.
	 */
	public boolean isNewsCategory() {
		return newsCategory != null;
	}
}
