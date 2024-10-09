package net.causw.application.dto.userAcademicRecordApplication;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserAcademicRecordApplicationListResponseDto {
    @Schema(description = "user 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String userId;

    @Schema(description = "이름", example = "정상제")
    private String userName;

    @Schema(description = "학번", example = "20191234")
    private String studentId;

    @Schema(description = "학적증명서 신청 id값", example = "uuid 형식의 String 값입니다.")
    private String userAcademicRecordApplicationId;
}
