package net.causw.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.enums.Role;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRoleRequestDto {

    @Schema(description = "역할", example = "COMMON")
    private String role;

    @Schema(description = "동아리 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String circleId;

    public Role getRoleEnum() {
        return Role.of(this.role);
    }

    public Optional<String> getCircleId() {
        return Optional.ofNullable(this.circleId);
    }
}
