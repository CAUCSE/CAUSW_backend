package net.causw.application.dto.user;

import lombok.Data;
import net.causw.domain.model.Role;

import java.util.Optional;

@Data
public class UserUpdateRoleRequestDto {
    private String role;
    private String circleId;

    public Role getRole() {
        return Role.of(this.role);
    }
    public Optional<String> getCircleId() {
        return Optional.ofNullable(this.circleId);
    }
}
