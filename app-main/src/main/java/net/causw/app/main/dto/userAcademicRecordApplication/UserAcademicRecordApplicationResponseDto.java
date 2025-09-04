package net.causw.app.main.dto.userAcademicRecordApplication;

import java.time.LocalDateTime;
import java.util.List;

import net.causw.app.main.domain.model.enums.userAcademicRecord.AcademicStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserAcademicRecordApplicationResponseDto {

	@Schema(description = "변환 타겟 학적 상태", example = "가입/승인/n차 학기 재학 등록/휴학 전환/졸업 전환")
	private AcademicStatus targetAcademicStatus;

	@Schema(description = "유저 작성 특이사항(단, 관리자 임의 수정 시 \"관리자 수정\"이라 기입)", example = "유저 작성 특이사항")
	private String userNote;

	@Schema(description = "첨부 이미지 URL 리스트")
	private List<String> attachedImageUrlList;

	@Schema(description = "변경 날짜", example = "2024-08-01")
	private LocalDateTime changeDate;

}
