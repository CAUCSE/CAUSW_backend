package net.causw.application.dto.userCouncilFee;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserCouncilFeeListResponseDto {

    @Schema(description = "user 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String userId;

    @Schema(description = "이름", example = "정상제")
    private String userName;

    @Schema(description = "학번", example = "20191234")
    private String studentId;

    @Schema(description = "동문네트워크 서비스 가입 여부", example = "true")
    private Boolean isJoinedService;

}
