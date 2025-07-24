package net.causw.app.main.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardNameCheckRequestDto {
	@NotBlank(message = "게시판 이름은 필수 입력 값입니다.")
	private String name;
}
