package net.causw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.domain.model.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRoleRequestDto {
    private Role role;
}
