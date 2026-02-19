package net.causw.app.main.domain.community.post.api.v2.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PostCreateRequest(
	@NotBlank(message = "게시글 내용을 입력해 주세요.") @Schema(description = "게시글 내용", example = "안녕하세요. 학생회입니다. 공지사항입니다.") String content,

	@NotBlank(message = "게시판 id를 입력해 주세요.") @Schema(description = "게시판 id", example = "uuid 형식의 String 값입니다.") String boardId,

	@NotNull(message = "익명글 여부를 선택해 주세요.") @Schema(description = "익명글 여부", example = "False") Boolean isAnonymous) {

}
