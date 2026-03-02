package net.causw.app.main.domain.notification.notification.service.dto;

/**
 * 공지사항 게시판 알림 구독 설정 정보.
 *
 * @param boardId    게시판 ID
 * @param name       게시판 이름
 * @param subscribed 유저의 해당 게시판 알림 구독 여부
 */
public record OfficialBoardSetting(
	String boardId,
	String name,
	boolean subscribed) {
}