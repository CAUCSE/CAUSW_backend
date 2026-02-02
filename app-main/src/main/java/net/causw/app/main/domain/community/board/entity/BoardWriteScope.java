package net.causw.app.main.domain.community.board.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BoardWriteScope {
	ONLY_ADMIN("게시판 관리자만"),
	ALL_USER("모든 사용자");

	private final String description;
}
