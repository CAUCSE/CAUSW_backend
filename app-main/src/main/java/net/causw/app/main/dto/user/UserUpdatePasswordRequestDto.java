package net.causw.app.main.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdatePasswordRequestDto {
	@NotBlank(message = "기존 비밀번호를 입력해 주세요.")
	private String originPassword;
	@NotBlank(message = "새로운 비밀번호를 입력해 주세요.")
	private String updatedPassword;
}
