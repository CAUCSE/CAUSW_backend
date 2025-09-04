package net.causw.app.main.dto.ceremony;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import net.causw.app.main.domain.model.enums.ceremony.CeremonyCategory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateCeremonyRequestDto {

	@Schema(description = "행사 설명", requiredMode = Schema.RequiredMode.REQUIRED, example = "연례 졸업식")
	@NotNull(message = "설명은 필수 입력 값입니다.")
	private String description;

	@Schema(description = "행사 시작 날짜", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-05-01")
	@NotNull(message = "시작 날짜는 필수 입력 값입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@Schema(description = "행사 종료 날짜", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-05-02")
	@NotNull(message = "종료 날짜는 필수 입력 값입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	@Schema(description = "행사 카테고리", requiredMode = Schema.RequiredMode.REQUIRED, example = "MARRIAGE")
	@NotNull(message = "카테고리는 필수 입력 값입니다.")
	private CeremonyCategory category;

	@Schema(description = "모든 학번에게 알림 전송 여부", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
	@NotNull(message = "전체 알림 전송 여부는 필수 입력 값입니다.")
	private Boolean isSetAll;

	@Schema(description = "알림 대상 학번", requiredMode = Schema.RequiredMode.REQUIRED, example = "[19, 21, 22]")
	private List<String> targetAdmissionYears;

	// 시작날짜가 종료날짜보다 이전인지 검증
	@AssertTrue(message = "시작 날짜는 종료 날짜보다 이전이거나 같아야 합니다.")
	private boolean isValidDateRange() {
		if (startDate == null || endDate == null) {
			return true; // null 체크는 @NotNull에서 처리
		}
		return !startDate.isAfter(endDate);
	}
}
