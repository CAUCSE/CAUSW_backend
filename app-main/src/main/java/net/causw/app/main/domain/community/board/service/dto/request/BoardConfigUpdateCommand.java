package net.causw.app.main.domain.community.board.service.dto.request;

import java.util.List;

import net.causw.app.main.domain.community.board.entity.BoardReadScope;
import net.causw.app.main.domain.community.board.entity.BoardVisibility;
import net.causw.app.main.domain.community.board.entity.BoardWriteScope;

import lombok.Builder;

@Builder
public record BoardConfigUpdateCommand(
	String boardId,
	String name,
	String description,
	List<String> adminUserIds,
	boolean isAnonymous,
	BoardReadScope readScope,
	BoardWriteScope writeScope,
	boolean isNotice,
	BoardVisibility visibility) {
}
