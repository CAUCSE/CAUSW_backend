package net.causw.app.main.domain.notification.notification.api.v2.dto.response;

import java.util.List;

import net.causw.app.main.domain.community.post.enums.PostCategory;
import net.causw.app.main.domain.notification.notification.service.dto.BoardSubscribeSetting;
import net.causw.app.main.domain.notification.notification.service.dto.NotificationSettingResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 설정 조회 응답")
public record NotificationSettingResponse(

	@Schema(description = "커뮤니티 알림 설정") CommunitySettings community,

	@Schema(description = "경조사 알림 설정") CeremonySettings ceremony,

	@Schema(description = "서비스 공지 알림 설정") ServiceSettings service,

	@Schema(description = "공식계정 게시판 구독 목록. 기존 앱 호환용이며 news.channels와 동일합니다.", deprecated = true) List<BoardSetting> officialBoards,

	@Schema(description = "소식 알림 설정") NewsSettings news,

	@Schema(description = "소통 게시판 구독 목록 (설정하지 않은 게시판은 구독하지 않은 상태)") List<BoardSetting> communityBoards

) {

	@Schema(description = "커뮤니티 알림 세부 설정")
	public record CommunitySettings(
		@Schema(description = "내 글에 좋아요 알림") boolean likeOnMyPost,
		@Schema(description = "내 글에 댓글 알림") boolean commentOnMyPost,
		@Schema(description = "내 댓글의 대댓글 알림") boolean replyOnMyComment) {
	}

	@Schema(description = "경조사 알림 세부 설정")
	public record CeremonySettings(
		@Schema(description = "경조사 알림 수신 여부") boolean enabled) {
	}

	@Schema(description = "서비스 공지 알림 세부 설정")
	public record ServiceSettings(
		@Schema(description = "서비스 공지 알림 수신 여부") boolean noticeEnabled) {
	}

	@Schema(description = "소식 알림 세부 설정. enabled가 true이고, 채널 또는 성격 알림 중 하나라도 켜져 있으면 알림을 받습니다.")
	public record NewsSettings(
		@Schema(description = "소식 알림 받기 (전체 토글)") boolean enabled,
		@Schema(description = "소식 채널(게시판)별 구독 목록") List<BoardSetting> channels,
		@Schema(description = "소식 성격별 알림 목록") List<NewsCategorySetting> categories) {
	}

	@Schema(description = "게시판 구독 정보")
	public record BoardSetting(
		@Schema(description = "게시판 ID") String boardId,
		@Schema(description = "게시판 이름") String name,
		@Schema(description = "구독 여부") boolean subscribed) {
	}

	@Schema(description = "소식 성격별 알림 정보")
	public record NewsCategorySetting(
		@Schema(description = "게시글 성격") PostCategory category,
		@Schema(description = "화면 표시용 이름", example = "채용") String label,
		@Schema(description = "알림 수신 여부") boolean enabled) {
	}

	public static NotificationSettingResponse from(NotificationSettingResult result) {
		CommunitySettings community = new CommunitySettings(
			result.community().likeOnMyPost(),
			result.community().commentOnMyPost(),
			result.community().replyOnMyComment());
		CeremonySettings ceremony = new CeremonySettings(result.ceremony().enabled());
		ServiceSettings service = new ServiceSettings(result.service().noticeEnabled());
		List<BoardSetting> newsChannels = toBoardSettings(result.news().channels());
		NewsSettings news = new NewsSettings(
			result.news().enabled(),
			newsChannels,
			result.news().categories().stream()
				.map(c -> new NewsCategorySetting(c.category(), c.label(), c.enabled()))
				.toList());

		return new NotificationSettingResponse(
			community, ceremony, service, newsChannels, news, toBoardSettings(result.communityBoards()));
	}

	private static List<BoardSetting> toBoardSettings(List<BoardSubscribeSetting> settings) {
		return settings.stream()
			.map(b -> new BoardSetting(b.boardId(), b.name(), b.subscribed()))
			.toList();
	}
}
