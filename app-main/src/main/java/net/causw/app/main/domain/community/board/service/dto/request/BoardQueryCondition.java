package net.causw.app.main.domain.community.board.service.dto.request;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;

public record BoardQueryCondition(
	String keyword,
	Boolean isAnonymous,
	BoardWriteScope writeScope,
	BoardReadScope readScope,
	Boolean isNotice
) {
}
