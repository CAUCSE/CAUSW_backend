package net.causw.app.main.shared.exception.errorcode;

import org.springframework.http.HttpStatus;

import net.causw.app.main.shared.exception.BaseResponseCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum PostLikeMilestoneAchievementErrorCode implements BaseResponseCode {

	POST_LIKE_MILESTONE_ACHIEVEMENT_NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"POST_LIKE_MILESTONE_ACHIEVEMENT_404_001",
		"게시글 좋아요 마일스톤 이력을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public HttpStatus getStatus() {
		return status;
	}
}
