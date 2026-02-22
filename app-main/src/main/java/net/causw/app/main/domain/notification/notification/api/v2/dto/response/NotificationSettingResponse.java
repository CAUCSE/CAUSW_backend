package net.causw.app.main.domain.notification.notification.api.v2.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import net.causw.app.main.domain.notification.notification.service.v2.dto.NotificationSettingResult;

@Schema(description = "알림 설정 조회 응답")
public record NotificationSettingResponse(

	@Schema(description = "커뮤니티 알림 설정")
	CommunitySettings community,

	@Schema(description = "경조사 알림 설정")
	CeremonySettings ceremony,

	@Schema(description = "서비스 공지 알림 설정")
	ServiceSettings service,

	@Schema(description = "공식계정 게시판 구독 목록")
	List<OfficialBoardSetting> officialBoards

) {

	@Schema(description = "커뮤니티 알림 세부 설정")
	public record CommunitySettings(
		@Schema(description = "내 글에 좋아요 알림") boolean likeOnMyPost,
		@Schema(description = "내 글에 댓글 알림") boolean commentOnMyPost,
		@Schema(description = "내 댓글의 대댓글 알림") boolean replyOnMyComment
	) {}

	@Schema(description = "경조사 알림 세부 설정")
	public record CeremonySettings(
		@Schema(description = "경조사 알림 수신 여부") boolean enabled
	) {}

	@Schema(description = "서비스 공지 알림 세부 설정")
	public record ServiceSettings(
		@Schema(description = "서비스 공지 알림 수신 여부") boolean noticeEnabled
	) {}

	@Schema(description = "공식계정 게시판 구독 정보")
	public record OfficialBoardSetting(
		@Schema(description = "게시판 ID") String boardId,
		@Schema(description = "게시판 이름") String name,
		@Schema(description = "구독 여부") boolean subscribed
	) {}

	public static NotificationSettingResponse from(NotificationSettingResult result) {
		CommunitySettings community = new CommunitySettings(
			result.community().likeOnMyPost(),
			result.community().commentOnMyPost(),
			result.community().replyOnMyComment()
		);
		CeremonySettings ceremony = new CeremonySettings(result.ceremony().enabled());
		ServiceSettings service = new ServiceSettings(result.service().noticeEnabled());
		List<OfficialBoardSetting> officialBoards = result.officialBoards().stream()
			.map(b -> new OfficialBoardSetting(b.boardId(), b.name(), b.subscribed()))
			.toList();

		return new NotificationSettingResponse(community, ceremony, service, officialBoards);
	}
}
