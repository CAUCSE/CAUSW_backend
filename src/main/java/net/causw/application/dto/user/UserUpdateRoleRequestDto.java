package net.causw.application.dto.user;

import io.swagger.annotations.*;
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

    @ApiModelProperty(value = "역할", example = "COMMON")
    private String role;

    @ApiModelProperty(value = "동아리 고유 id값", example = "uuid 형식의 String 값입니다.")
    private String circleId;

    public Role getRole() {
        return Role.of(this.role);
    }

    public Optional<String> getCircleId() {
        return Optional.ofNullable(this.circleId);
    }
}
