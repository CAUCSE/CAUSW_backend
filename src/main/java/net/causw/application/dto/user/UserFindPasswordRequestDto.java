package net.causw.application.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserFindPasswordRequestDto {
    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotBlank(message = "이메일을 입력해 주세요.")
    private String email;
    @NotBlank(message = "이름을 입력해 주세요.")
    private String name;
    @NotBlank(message = "학번을 입력해 주세요.")
    private String studentId;
    @Pattern(
            regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$",
            message = "전화번호 형식에 맞지 않습니다."
    )
    @NotBlank(message = "전화번호는 비어있을 수 없습니다.")
    private String phoneNumber;
}
