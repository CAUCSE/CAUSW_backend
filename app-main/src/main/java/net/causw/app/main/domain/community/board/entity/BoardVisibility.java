package net.causw.app.main.domain.community.board.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BoardVisibility {
	VISIBLE("보임"),
	HIDDEN("안 보임");

	private final String description;
}
