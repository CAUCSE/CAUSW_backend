package net.causw.app.main.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserFindIdRequestDto {
	@NotBlank(message = "전화번호는 비어있을 수 없습니다.")
	private String phoneNumber;

	@NotBlank(message = "이름은 비어있을 수 없습니다.")
	private String name;

}
