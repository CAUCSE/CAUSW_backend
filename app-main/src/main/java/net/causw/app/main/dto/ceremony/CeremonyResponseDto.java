package net.causw.app.main.dto.ceremony;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyCategory;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyState;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CeremonyResponseDto {

    @Schema(description = "경조사 id", example = "uuid 형식의 String 값입니다.")
    private String id;

    @Schema(description = "행사 설명", example = "연례 졸업식")
    private String description;

    @Schema(description = "행사 시작 날짜", example = "2025-05-01")
    private LocalDate startDate;

    @Schema(description = "행사 종료 날짜", example = "2025-05-02")
    private LocalDate endDate;

    @Schema(description = "행사 카테고리", example = "GRADUATION")
    private CeremonyCategory category;

    @Schema(description = "신청한 경조사 상태", example = "AWAIT")
    private CeremonyState ceremonyState;

    @Schema(description = "첨부 이미지 URL 리스트")
    private List<String> attachedImageUrlList;

    @Schema(description = "경조사 거부 사유")
    private String note;

    @Schema(description = "경조사 신청자 학번")
    private String applicantStudentId;

    @Schema(description = "경조사 신청자 이름")
    private String applicantName;

    @Schema(description = "경조사 제목")
    private String title;

}
