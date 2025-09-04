package net.causw.app.main.dto.board;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardAppliesResponseDto {
	@Schema(description = "게시판 신청 id 값", example = "uuid 형식의 String 값입니다.")
	private String id;

	@NotBlank(message = "게시판 이름은 필수 입력 값입니다.")
	@Schema(description = "게시판 이름", example = "백준 질문 게시판")
	private String boardName;
}
