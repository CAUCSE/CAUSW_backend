package net.causw.app.main.domain.notification.notification.service.v2.dto;

import java.util.List;

import net.causw.app.main.domain.notification.notification.enums.UserNotificationSettingKey;

/**
 * 유저의 알림 설정 전체 조회 결과 DTO.
 * 커뮤니티·경조사·서비스 알림 설정과 공지사항 게시판 구독 목록을 포함한다.
 *
 * @param community     커뮤니티 알림 설정
 * @param ceremony      경조사 알림 설정
 * @param service       서비스 알림 설정
 * @param officialBoards 공지사항 게시판별 알림 구독 설정 목록
 */
public record NotificationSettingResult(
	CommunitySettings community,
	CeremonySettings ceremony,
	ServiceSettings service,
	List<OfficialBoardSetting> officialBoards) {

	/**
	 * 커뮤니티 알림 설정.
	 *
	 * @param likeOnMyPost      내 게시글에 좋아요 알림 활성화 여부
	 * @param commentOnMyPost   내 게시글에 댓글 알림 활성화 여부
	 * @param replyOnMyComment  내 댓글에 대댓글 알림 활성화 여부
	 */
	public record CommunitySettings(
		boolean likeOnMyPost,
		boolean commentOnMyPost,
		boolean replyOnMyComment) {
	}

	/**
	 * 경조사 알림 설정.
	 *
	 * @param enabled 경조사 알림 활성화 여부
	 */
	public record CeremonySettings(boolean enabled) {
	}

	/**
	 * 서비스 알림 설정.
	 *
	 * @param noticeEnabled 서비스 공지 알림 활성화 여부
	 */
	public record ServiceSettings(boolean noticeEnabled) {
	}

	/**
	 * {@link UserNotificationSettingMap}과 공지사항 게시판 구독 목록으로 결과 객체를 생성한다.
	 *
	 * @param settingMap     유저 알림 설정 맵
	 * @param officialBoards 공지사항 게시판별 구독 설정 목록
	 * @return {@link NotificationSettingResult}
	 */
	public static NotificationSettingResult from(
		UserNotificationSettingMap settingMap,
		List<OfficialBoardSetting> officialBoards) {
		return new NotificationSettingResult(
			new CommunitySettings(
				settingMap.get(UserNotificationSettingKey.COMMUNITY_LIKE_ON_MY_POST),
				settingMap.get(UserNotificationSettingKey.COMMUNITY_COMMENT_ON_MY_POST),
				settingMap.get(UserNotificationSettingKey.COMMUNITY_REPLY_ON_MY_COMMENT)),
			new CeremonySettings(settingMap.get(UserNotificationSettingKey.CEREMONY_NOTIFICATION_ENABLED)),
			new ServiceSettings(settingMap.get(UserNotificationSettingKey.SERVICE_NOTICE_ENABLED)),
			officialBoards);
	}
}
