package net.causw.app.main.domain.notification.notification.service.v2.dto;

import java.util.List;

public record NotificationSettingResult(
	CommunitySettings community,
	CeremonySettings ceremony,
	ServiceSettings service,
	List<OfficialBoardSetting> officialBoards
) {

	public record CommunitySettings(
		boolean likeOnMyPost,
		boolean commentOnMyPost,
		boolean replyOnMyComment
	) {}

	public record CeremonySettings(boolean enabled) {}

	public record ServiceSettings(boolean noticeEnabled) {}

	public record OfficialBoardSetting(
		String boardId,
		String name,
		boolean subscribed
	) {}
}
