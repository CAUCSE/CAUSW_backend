package net.causw.application.dto.user;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequestDto {

    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Schema(description = "이메일", example = "yebin@cau.ac.kr")
    private String email;

    @NotBlank(message = "이름을 입력해 주세요.")
    @Schema(description = "이름", example = "이예빈")
    private String name;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*()-_?]).{8,20}$",
            message = "비밀번호는 8자 이상 20자 이하이며, 영문, 숫자, 특수문자가 각 1개 이상 포함되어야 합니다."
    )
    @Schema(description = "비밀번호", example = "password00!!")
    private String password;

    @NotBlank(message = "학번을 입력해 주세요.")
    @Schema(description = "학번", example = "20209999")
    private String studentId;

    @NotNull(message = "입학년도를 입력해 주세요.")
    @Schema(description = "입학년도", example = "2020")
    private Integer admissionYear;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Schema(description = "닉네임", example = "푸앙")
    private String nickname;

    @NotBlank(message = "학부 또는 학과를 입력해 주세요.")
    @Schema(description = "학부/학과", example = "소프트웨어학부")
    private String major;

    @Schema(description = "전화번호", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    @Pattern(regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
    private String phoneNumber;

}
