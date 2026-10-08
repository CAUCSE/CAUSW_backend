package net.causw.app.main.domain.notification.notification.service.dto;

import net.causw.app.main.domain.community.post.enums.PostCategory;

/**
 * 소식 성격별 알림 설정 정보.
 *
 * @param category 게시글 성격
 * @param label    화면 표시용 이름
 * @param enabled  해당 성격 알림 활성화 여부
 */
public record NewsCategorySetting(
	PostCategory category,
	String label,
	boolean enabled) {
}
