package net.causw.application.dto.userAcademicRecordApplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import net.causw.domain.model.enums.AcademicStatus;

import java.util.List;

@Getter
public class CreateUserAcademicRecordApplicationRequestDto {

    @Schema(description = "타겟 학적 상태", defaultValue = "ENROLLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "재학/휴학/졸업/미정")
    private AcademicStatus targetAcademicStatus;

    @Schema(description = "타겟 사용자의 현재 학기", defaultValue = "5", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer targetCompletedSemester;

    @Schema(description = "비고", defaultValue = "학적 정보 신청 비고", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "학적 정보 신청 비고입니다.")
    private String note;

    @Schema(description = "첨부 이미지 url 리스트", defaultValue = "https://causw.net/image/1.jpg, https://causw.net/image/2.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "첨부 이미지 url 리스트입니다.")
    private List<String> attachImageUrlList;
}
