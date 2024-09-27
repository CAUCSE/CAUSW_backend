package net.causw.application.dto.userAcademicRecordApplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.enums.semester.SemesterType;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CurrentUserAcademicRecordApplicationResponseDto {

    @Schema(description = "현재 학기 년도", example = "2024")
    private Integer currentSemesterYear;

    @Schema(description = "학기 타입", example = "1학기/2학기/여름계절/겨울계절")
    private SemesterType currentSemesterType;

    @Schema(description = "신청 상태(true: 거절됨 / false: 대기 중", example = "true")
    private Boolean isRejected;

    @Schema(description = "거절 사유", example = "거절 사유")
    private String rejectMessage;

    @Schema(description = "학적 상태", example = "재학/휴학/졸업/미정")
    private String targetAcademicStatus;

    @Schema(description = "본 학기 기준 등록 완료 학기 차수", example = "5")
    private Integer targetCompletedSemester;

    @Schema(description = "유저 작성 특이사항", example = "유저 작성 특이사항")
    private String userNote;

    @Schema(description = "첨부 이미지 URL 리스트")
    private List<String> attachedImageUrlList;

}
