package net.causw.app.main.dto.semester;

import net.causw.app.main.domain.model.enums.semester.SemesterType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CurrentSemesterResponseDto {

	@Schema(description = "현재 학기 년도", example = "2024")
	private Integer currentSemesterYear;

	@Schema(description = "학기 타입", example = "1학기/2학기/여름계절/겨울계절")
	private SemesterType currentSemesterType;

}
