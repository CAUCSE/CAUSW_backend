package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {

    @Schema(description = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @Schema(description = "이름", example = "이에빈")
    private String name;

    @Schema(description = "학번", example = "20209999")
    private String studentId;

    @Schema(description = "입학년도", example = "2020")
    private Integer admissionYear;

    @Schema(description = "프로필 이미지 URL", example = "")
    private List<String> profileImages;
}
