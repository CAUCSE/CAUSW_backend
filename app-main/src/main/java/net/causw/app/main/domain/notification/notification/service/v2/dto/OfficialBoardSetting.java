package net.causw.app.main.domain.notification.notification.service.v2.dto;

public record OfficialBoardSetting(
	String boardId,
	String name,
	boolean subscribed
) {}
