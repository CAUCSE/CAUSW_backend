package net.causw.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class UserFindIdRequestDto {
    // TODO : private으로 안한 특별한 이유가 있는가?
    @NotBlank(message = "학번은 비어있을 수 없습니다.")
    String studentId;

    @NotBlank(message = "이름은 비어있을 수 없습니다.")
    String name;

    @Pattern(
            regexp = "^01(?:0|1|[6-9])-(\\d{3}|\\d{4})-\\d{4}$",
            message = "전화번호 형식에 맞지 않습니다."
    )
    @NotBlank(message = "전화번호는 비어있을 수 없습니다.")
    String phoneNumber;

}
