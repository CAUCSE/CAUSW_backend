package net.causw.app.main.domain.community.board.api.v2.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record BoardOrderUpdateRequest(
	@Schema(description = "게시판 정렬 순서대로 id 나열한 배열", example = "[\"board-id-1\", \"board-id-2\"]")
	@NotNull List<String> boardIds) {
}
