package net.causw.app.main.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportedUserResponseDto {
    
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440003")
    private final String userId;
    
    @Schema(description = "사용자 실명", example = "김철수")
    private final String userName;
    
    @Schema(description = "사용자 닉네임", example = "철수킴")
    private final String userNickname;
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile-image.jpg")
    private final String profileImage;
    
    @Schema(description = "총 신고 받은 횟수", example = "5")
    private final Integer totalReportCount;
}