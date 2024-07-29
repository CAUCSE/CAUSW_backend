package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {

    @Schema(description = "이메일", example = "yebin@cau.ac.kr")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotBlank(message = "이메일을 입력해 주세요.")
    private String email;

    @Schema(description = "이름", example = "이에빈")
    @NotBlank(message = "이름을 입력해 주세요.")
    private String name;

    @Schema(description = "학번", example = "20209999")
    @NotBlank(message = "학번을 입력해 주세요.")
    private String studentId;

    @Schema(description = "입학년도", example = "2020")
    @NotBlank(message = "입학 년도를 입력해 주세요.")
    private Integer admissionYear;

    @Schema(description = "프로필 이미지 URL", example = "")
    private String profileImage;
}
