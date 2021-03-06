package net.causw.application.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.Role;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
