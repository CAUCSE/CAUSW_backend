package net.causw.app.main.domain.community.board.api.v2.dto.request;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;

public record BoardSearchCondition(
	String keyword,
	Boolean isAnonymous,
	BoardWriteScope writeScope,
	BoardReadScope readScope,
	Boolean isNotice
) {
}
