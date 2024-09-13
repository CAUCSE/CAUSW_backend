package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.enums.AcademicStatus;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {

    @NotBlank(message = "이름을 입력해 주세요.")
    @Schema(description = "이름", example = "이에빈")
    private String name;

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

    @NotNull(message = "학적 상태를 선택해 주세요.")
    @Schema(description = "학적상태", example = "ENROLLED")
    private AcademicStatus academicStatus;

    @NotNull(message = "현재 등록 완료된 학기를 선택해 주세요.")
    @Schema(description = "현재 등록 완료된 학기", example = "6(3학년 2학기)")
    private Integer currentCompletedSemester;

    @NotNull(message = "졸업시기 년을 선택해 주세요.")
    @Schema(description = "졸업시기 년", example = "2026")
    private Integer graduationYear;

    @NotNull(message = "졸업시기 월을 선택해 주세요.")
    @Schema(description = "졸업시기 월", example = "2")
    private Integer graduationMonth;

    @NotBlank(message = "전화번호를 입력해 주세요.")
    @Schema(description = "전화번호", example = "01012345678")
    @Pattern(regexp = "^01(?:0|1|[6-9])(\\d{3}|\\d{4})\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
    private String phoneNumber;
}
