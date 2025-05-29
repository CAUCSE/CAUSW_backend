package net.causw.config.security;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.user.User;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.model.enums.user.RoleGroup;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;
import net.causw.domain.policy.RolePolicy;
import net.causw.domain.policy.UserPolicy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@MeasureTime
@Service("security")
@RequiredArgsConstructor
public class SecurityService {
    public boolean hasRole(String role) {
        return RolePolicy.hasRole(getAuthorities(), Role.of(role));
    }

    public boolean hasRole(Role role) {
        return RolePolicy.hasRole(getAuthorities(), role);
    }

    public boolean hasRoleGroup(RoleGroup roleGroup) {
        return RolePolicy.hasRoleGroup(getAuthorities(), roleGroup);
    }

    public boolean isActiveAndNotNoneUser() {
        return UserPolicy.isStateActive(getUserDetails()) && !RolePolicy.hasRoleOnlyNone(getAuthorities());
    }

    public boolean isAcademicRecordCertified() {
        User user = getUserDetails().getUser();

        if (RolePolicy.hasRoleGroup(getAuthorities(), RoleGroup.EXECUTIVES_AND_PROFESSOR)) {
            return true;
        }

        return UserPolicy.isAcademicRecordCertified(user);
    }

    public boolean isActiveAndNotNoneUserAndAcademicRecordCertified() {
        return isActiveAndNotNoneUser() && isAcademicRecordCertified();
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException(MessageUtil.ACCESS_DENIED);
        }

        return authentication;
    }

    private CustomUserDetails getUserDetails() {
        return (CustomUserDetails) getAuthentication().getPrincipal();
    }

    private Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthentication().getAuthorities();
    }
}
