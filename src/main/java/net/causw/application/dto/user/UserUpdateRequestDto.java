package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequestDto {

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Schema(description = "닉네임", example = "푸앙")
    private String nickname;

    @NotBlank(message = "전화번호를 입력해 주세요.")
    @Schema(description = "전화번호", example = "01012345678")
    @Pattern(regexp = "^01(?:0|1|[6-9])(\\d{3}|\\d{4})\\d{4}$", message = "전화번호 형식에 맞지 않습니다.")
    private String phoneNumber;

}
