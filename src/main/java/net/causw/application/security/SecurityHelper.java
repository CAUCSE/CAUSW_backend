package net.causw.application.security;

import net.causw.adapter.persistence.user.User;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.RoleGroup;
import net.causw.domain.model.enums.user.UserState;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SecurityHelper {


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

    public static boolean isAcademicRecordCertified(CustomUserDetails userDetails) {
        AcademicStatus academicStatus = userDetails.getUser().getAcademicStatus();

        if (SecurityHelper.hasRoleGroup(userDetails.getAuthorities(), RoleGroup.EXECUTIVES_AND_PROFESSOR)) {
            return true;
        }

        if (academicStatus == null) {
            return false;
        } else return !academicStatus.equals(AcademicStatus.UNDETERMINED);
    }

    public static boolean isStateActive(CustomUserDetails userDetails) {
        return userDetails.getUserState() == UserState.ACTIVE;
    }
}
