package net.causw.app.main.dto.user;

import net.causw.app.main.domain.model.enums.user.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRoleRequestDto {

	@NotBlank(message = "역할을 선택해 주세요.")
	@Schema(description = "역할", example = "COMMON")
	private String role;

	public Role getRole() {
		return Role.of(this.role);
	}
}
