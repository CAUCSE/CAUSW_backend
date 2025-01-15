package net.causw.config.security;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.repository.form.FormRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.model.enums.userAcademicRecord.AcademicStatus;
import net.causw.domain.model.enums.user.Role;
import net.causw.domain.model.enums.user.UserState;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityService {
    private final FormRepository formRepository;

    public boolean isActiveAndNotNoneUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        boolean isJustRoleNone = userDetails.getAuthorities().stream()
                .allMatch(authority -> authority.getAuthority().equals("ROLE_NONE"));

        return userDetails.getUserState() == UserState.ACTIVE && !isJustRoleNone;
    }

    public boolean isAcademicRecordCertified() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        Set<Role> userRoleSet = user.getRoles();
        if (userRoleSet.contains(Role.ADMIN) ||
                userRoleSet.contains(Role.PROFESSOR) ||
                userRoleSet.contains(Role.PRESIDENT) ||
                userRoleSet.contains(Role.VICE_PRESIDENT)
        ) {
            return true;
        }

        AcademicStatus academicStatus = user.getAcademicStatus();

        if (academicStatus == null) {
            return false;
        } else return !academicStatus.equals(AcademicStatus.UNDETERMINED);
    }

    public boolean isActiveAndNotNoneUserAndAcademicRecordCertified() {
        return isActiveAndNotNoneUser() && isAcademicRecordCertified();
    }

    public boolean isAdmin() {
        Set<String> userRoleSet = getUserRoleSet();

        if (userRoleSet != null) {
            return userRoleSet.contains(Role.ADMIN.getValue());
        }
        return false;
    }

    public boolean isAdminOrPresidentOrVicePresident() {
        Set<String> userRoleSet = getUserRoleSet();

        if (userRoleSet != null) {
            return userRoleSet.contains(Role.ADMIN.getValue()) ||
                    userRoleSet.contains(Role.PRESIDENT.getValue()) ||
                    userRoleSet.contains(Role.VICE_PRESIDENT.getValue());
        }
        return false;
    }

    public boolean isAdminOrPresidentOrVicePresidentOrCircleLeader() {
        Set<String> userRoleSet = getUserRoleSet();

        if (userRoleSet != null) {
            return userRoleSet.contains(Role.ADMIN.getValue()) ||
                    userRoleSet.contains(Role.PRESIDENT.getValue()) ||
                    userRoleSet.contains(Role.VICE_PRESIDENT.getValue()) ||
                    userRoleSet.contains(Role.LEADER_CIRCLE.getValue());
        }
        return false;
    }

    public boolean isCircleLeader() {
        Set<String> userRoleSet = getUserRoleSet();

        if (userRoleSet != null) {
            return userRoleSet.contains(Role.LEADER_CIRCLE.getValue());
        }
        return false;
    }

    public boolean isAbleToLeave() {
        Set<String> userRoleSet = getUserRoleSet();

        if (userRoleSet != null) {
            return userRoleSet.contains(Role.COMMON.getValue()) ||
                    userRoleSet.contains(Role.PROFESSOR.getValue());
        }
        return false;
    }

    public boolean isSpecialPrivileged() {
        Set<String> userRoleSet = getUserRoleSet();

        if (userRoleSet != null) {
            return userRoleSet.contains(Role.ADMIN.getValue()) ||
                    userRoleSet.contains(Role.PRESIDENT.getValue()) ||
                    userRoleSet.contains(Role.VICE_PRESIDENT.getValue()) ||
                    userRoleSet.contains(Role.COUNCIL.getValue()) ||
                    userRoleSet.contains(Role.LEADER_CIRCLE.getValue()) ||
                    userRoleSet.contains(Role.LEADER_1.getValue()) ||
                    userRoleSet.contains(Role.LEADER_2.getValue()) ||
                    userRoleSet.contains(Role.LEADER_3.getValue()) ||
                    userRoleSet.contains(Role.LEADER_4.getValue()) ||
                    userRoleSet.contains(Role.LEADER_ALUMNI.getValue());
        }
        return false;
    }

    private Set<String> getUserRoleSet() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser().getRoles()
                .stream()
                .map(Role::getValue)
                .collect(Collectors.toSet());
    }

}
