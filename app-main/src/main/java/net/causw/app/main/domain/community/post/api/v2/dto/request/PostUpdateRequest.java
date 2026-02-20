package net.causw.app.main.domain.community.post.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostUpdateRequest(
	@NotNull(message = "익명글 여부를 선택해 주세요.") @Schema(description = "익명글 여부", example = "False") Boolean isAnonymous,

	@NotBlank(message = "게시글 내용을 입력해 주세요.") @Schema(description = "게시글 내용", example = "수정된 게시글 내용입니다.") String content) {
}
