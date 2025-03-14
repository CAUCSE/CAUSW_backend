package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.enums.user.Role;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRoleRequestDto {

    @NotBlank(message = "역할을 선택해 주세요.")
    @Schema(description = "역할", example = "COMMON")
    private String role;

    //@NotBlank(message = "동아리 id를 입력해 주세요.")
    @Schema(description = "동아리 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String circleId;

    public Role getRole() {
        return Role.of(this.role);
    }

    public Optional<String> getCircleId() {
        return Optional.ofNullable(this.circleId);
    }
}
