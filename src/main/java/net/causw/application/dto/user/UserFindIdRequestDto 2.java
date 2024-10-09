package net.causw.application.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserFindIdRequestDto {
    @NotBlank(message = "학번은 비어있을 수 없습니다.")
    String studentId;

    @NotBlank(message = "이름은 비어있을 수 없습니다.")
    String name;

    @NotBlank(message = "휴대폰 번호는 비어있을 수 없습니다.") // 번호 사이에 - 없이 받아야 함
    String phoneNumber;
}
