package net.causw.app.main.domain.community.board.service.dto.request;

import java.util.List;

import lombok.Builder;

@Builder
public record BoardOrderUpdateCommand(
	List<String> boardIds) {
}
