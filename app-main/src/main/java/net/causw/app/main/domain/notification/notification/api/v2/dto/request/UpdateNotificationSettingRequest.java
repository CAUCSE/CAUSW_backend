package net.causw.app.main.domain.notification.notification.api.v2.dto.request;

import java.util.Map;

import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.notification.notification.service.dto.UpdateNotificationSettingCommand;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 설정 수정 요청 (null 필드는 변경하지 않음)")
public record UpdateNotificationSettingRequest(
	@Schema(description = "커뮤니티 알림 설정") CommunitySettings community,
	@Schema(description = "경조사 알림 설정") CeremonySettings ceremony,
	@Schema(description = "서비스 공지 알림 설정") ServiceSettings service,
	@Schema(description = "소식 알림 설정") NewsSettings news

) {

	@Schema(description = "커뮤니티 알림 세부 설정")
	public record CommunitySettings(
		@Schema(description = "내 글에 좋아요 알림") Boolean likeOnMyPost,
		@Schema(description = "내 글에 댓글 알림") Boolean commentOnMyPost,
		@Schema(description = "내 댓글의 대댓글 알림") Boolean replyOnMyComment) {
	}

	@Schema(description = "경조사 알림 세부 설정")
	public record CeremonySettings(
		@Schema(description = "경조사 알림 수신 여부") Boolean enabled) {
	}

	@Schema(description = "서비스 공지 알림 세부 설정")
	public record ServiceSettings(
		@Schema(description = "서비스 공지 알림 수신 여부") Boolean noticeEnabled) {
	}

	@Schema(description = "소식 알림 세부 설정. 채널별 구독은 PATCH /api/v2/notification-settings/boards/{boardId}로 수정합니다.")
	public record NewsSettings(
		@Schema(description = "소식 알림 받기 (전체 토글)") Boolean enabled,
		@Schema(description = "성격별 알림 수신 여부. 포함된 성격만 변경하며, ETC는 설정할 수 없습니다.", example = "{\"RECRUIT\": false, \"ACADEMIC\": true}") Map<PostCategory, Boolean> categories) {
	}

	public UpdateNotificationSettingCommand toCommand() {
		Boolean likeOnMyPost = community != null ? community.likeOnMyPost() : null;
		Boolean commentOnMyPost = community != null ? community.commentOnMyPost() : null;
		Boolean replyOnMyComment = community != null ? community.replyOnMyComment() : null;
		Boolean ceremonyEnabled = ceremony != null ? ceremony.enabled() : null;
		Boolean serviceNoticeEnabled = service != null ? service.noticeEnabled() : null;
		Boolean newsEnabled = news != null ? news.enabled() : null;
		Map<PostCategory, Boolean> newsCategories = news != null ? news.categories() : null;

		return new UpdateNotificationSettingCommand(
			likeOnMyPost, commentOnMyPost, replyOnMyComment,
			ceremonyEnabled, serviceNoticeEnabled,
			newsEnabled, newsCategories);
	}
}
