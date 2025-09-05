package net.causw.app.main.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAcademicStatusNoteUpdateDto {

	@Schema(description = "사용자 학적 상태에 대한 비고", example = "비고입니다.")
	private String note;
}
