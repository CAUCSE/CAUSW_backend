package net.causw.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserFindPasswordRequestDto {
    @NotBlank(message = "이메일을 입력해 주세요.")
    private String email;
    @NotBlank(message = "이름을 입력해 주세요.")
    private String name;
    @NotBlank(message = "학번을 입력해 주세요.")
    private String studentId;
}
