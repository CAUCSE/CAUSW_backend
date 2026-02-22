package net.causw.app.main.domain.notification.notification.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import net.causw.app.main.domain.notification.notification.service.v2.dto.UpdateNotificationSettingCommand;

@Schema(description = "알림 설정 수정 요청 (null 필드는 변경하지 않음)")
public record UpdateNotificationSettingRequest(

	@Schema(description = "커뮤니티 알림 설정")
	CommunitySettings community,

	@Schema(description = "경조사 알림 설정")
	CeremonySettings ceremony,

	@Schema(description = "서비스 공지 알림 설정")
	ServiceSettings service

) {

	@Schema(description = "커뮤니티 알림 세부 설정")
	public record CommunitySettings(
		@Schema(description = "내 글에 좋아요 알림") Boolean likeOnMyPost,
		@Schema(description = "내 글에 댓글 알림") Boolean commentOnMyPost,
		@Schema(description = "내 댓글의 대댓글 알림") Boolean replyOnMyComment
	) {}

	@Schema(description = "경조사 알림 세부 설정")
	public record CeremonySettings(
		@Schema(description = "경조사 알림 수신 여부") Boolean enabled
	) {}

	@Schema(description = "서비스 공지 알림 세부 설정")
	public record ServiceSettings(
		@Schema(description = "서비스 공지 알림 수신 여부") Boolean noticeEnabled
	) {}

	public UpdateNotificationSettingCommand toCommand() {
		Boolean likeOnMyPost = community != null ? community.likeOnMyPost() : null;
		Boolean commentOnMyPost = community != null ? community.commentOnMyPost() : null;
		Boolean replyOnMyComment = community != null ? community.replyOnMyComment() : null;
		Boolean ceremonyEnabled = ceremony != null ? ceremony.enabled() : null;
		Boolean serviceNoticeEnabled = service != null ? service.noticeEnabled() : null;

		return new UpdateNotificationSettingCommand(
			likeOnMyPost, commentOnMyPost, replyOnMyComment,
			ceremonyEnabled, serviceNoticeEnabled
		);
	}
}
