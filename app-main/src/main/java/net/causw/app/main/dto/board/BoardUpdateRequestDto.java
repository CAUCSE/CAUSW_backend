package net.causw.app.main.dto.board;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoardUpdateRequestDto {

	@NotBlank(message = "게시판 이름을 입력해 주세요.")
	@Schema(description = "게시판 이름", example = "board_example")
	private String name;

	@Schema(description = "게시판 설명", example = "board_description")
	private String description;

	@NotEmpty(message = "게시판 글에 작성할 수 있는 권한 명단을 입력해 주세요.")
	@Schema(description = "게시판에 글을 작성할 수 있는 권한 명단", example = "[ 'ADMIN' ]")
	private List<String> createRoleList;

	@NotBlank(message = "게시판 카테고리를 선택해 주세요.")
	@Schema(description = "게시판 카테고리", example = "APP_NOTICE")
	private String category;
}
