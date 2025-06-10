package net.causw.domain.policy.security;

import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.RoleGroup;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class RolePolicy {


    public static boolean hasRole(Collection<? extends GrantedAuthority> authorities, Role role) {
        return authorities.stream()
                .anyMatch(authority -> authority.getAuthority().equals(role.authority()));
    }

    public static boolean hasRoleOnlyNone(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .allMatch(authority -> authority.getAuthority().equals(Role.NONE.authority()));
    }

    public static boolean hasRoleGroup(Collection<? extends GrantedAuthority> authorities, RoleGroup roleGroup) {
        return roleGroup
                .getRoles()
                .stream()
                .map(Role::authority)
                .anyMatch(role -> authorities
                        .stream()
                        .anyMatch(authority -> authority.getAuthority().equals(role)));
    }
}
