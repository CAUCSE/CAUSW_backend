package net.causw.app.main.dto.vote;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateVoteRequestDto {
	@NotEmpty(message = "투표제목이 필요합니다.")
	private String title;

	@NotNull(message = "익명 여부는 필수값입니다.")
	private Boolean allowAnonymous;

	@NotNull(message = "복수 선택 가능 여부는 필수값입니다.")
	private Boolean allowMultiple;

	@NotNull(message = "옵션 리스트는 비어 있을 수 없습니다.")
	@Size(min = 1, message = "최소 하나 이상의 옵션이 있어야 합니다.")
	private List<String> options;

	@NotNull(message = "postId는 필수 입력 값입니다.")
	private String postId;
}
