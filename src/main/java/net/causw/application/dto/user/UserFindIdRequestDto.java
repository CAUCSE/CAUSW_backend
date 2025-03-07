package net.causw.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserFindIdRequestDto {
    @NotBlank(message = "학번은 비어있을 수 없습니다.")
    private String studentId;

    @NotBlank(message = "이름은 비어있을 수 없습니다.")
    private String name;

}
