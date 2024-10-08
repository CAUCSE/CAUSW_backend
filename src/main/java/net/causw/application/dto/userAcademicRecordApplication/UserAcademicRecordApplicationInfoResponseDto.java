package net.causw.application.dto.userAcademicRecordApplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.domain.model.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserAcademicRecordApplicationInfoResponseDto {

    @Schema(description = "user 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String userId;

    @Schema(description = "이름", example = "정상제")
    private String userName;

    @Schema(description = "학번", example = "20191234")
    private String studentId;

    @Schema(description = "학적 상태 신청 상태", example = "AWAIT/ACCEPT/REJECT")
    private AcademicRecordRequestStatus academicRecordRequestStatus;

    @Schema(description = "변경 타겟 학적 상태", example = "재학/휴학/졸업/미정")
    private AcademicStatus targetAcademicStatus;

    @Schema(description = "변경 타겟 본 학기 기준 등록 완료 학기 차수", example = "5")
    private Integer targetCompletedSemester;

    @Schema(description = "비고", example = "관리자 추가 비고사항")
    private String note;

    @Schema(description = "첨부 이미지 URL 리스트")
    private List<String> attachedImageUrlList;

    @Schema(description = "거절 사유")
    private String rejectMessage;

}
