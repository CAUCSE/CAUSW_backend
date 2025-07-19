package net.causw.application.security;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.user.User;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.aop.annotation.MeasureTime;
import net.causw.domain.model.enums.user.RoleGroup;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 현재 인증된 사용자(SecurityContext 기준)의 권한, 상태 등을 조회하기 위한 Service 클래스
 * <p>
 * 도메인 기반 비즈니스 로직은 {@link SecurityHelper}로 위임하고,
 * 이 클래스에서는 인증 객체 조회, 상태 분기 처리 등의 역할만 수행
 */
@MeasureTime
@Service("security")
@RequiredArgsConstructor
public class SecurityService {

    /**
     * 현재 인증된 사용자가 주어진 Role을 보유하고 있는지 확인
     * @param role 문자열 형태의 역할 (예: "ADMIN", "VICE_PRESIDENT")
     */
    public boolean hasRole(String role) {
        return SecurityHelper.hasRole(getAuthorities(), Role.of(role));
    }

    /**
     * 현재 인증된 사용자가 주어진 Role을 보유하고 있는지 확인
     * @param role Role enum (예: Role.ADMIN, Role.VICE_PRESIDENT)
     */
    public boolean hasRole(Role role) {
        return SecurityHelper.hasRole(getAuthorities(), role);
    }

    /**
     * 현재 인증된 사용자가 주어진 RoleGroup에 속하는 권한을 보유하고 있는지 확인
     * @param roleGroup RoleGroup enum (예: RoleGroup.EXECUTIVES)
     */
    public boolean hasRoleGroup(RoleGroup roleGroup) {
        return SecurityHelper.hasRoleGroup(getAuthorities(), roleGroup);
    }

    /**
     * 현재 인증된 사용자가 활성 상태인지 확인
     * <p>
     * 사용자 상태(UserState)가 ACTIVE고
     * NONE 역할만을 가지고 있지 않을 경우 활성 상태로 판단
     *
     * @return true면 활성 사용자
     */
    public boolean isActiveUser() {
        return SecurityHelper.isStateActive(getUserDetails()) && !SecurityHelper.hasRoleOnlyNone(getAuthorities());
    }

    /**
     * 현재 인증된 사용자가 학적 인증된 사용자(+ 활성 상태)인지 확인
     * <p>
     * 사용자 상태(UserState)가 ACTIVE고
     * NONE 역할만을 가지고 있지 않고
     * 학적 상태(AcademicStatus)가 UNDETERMINED가 아닌 경우 학적 인증된 사용자로 판단
     * <p>
     * 단, 특정 권한 그룹에 속한 경우 학적 상태 검사를 건너뜀
     * 
     * @return true면 학적 인증 사용자
     */
    public boolean isCertifiedUser() {
        return isActiveUser() && SecurityHelper.isAcademicRecordCertified(getUserDetails());
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
