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

    public boolean isAdminOrPresidentOrVicePresident() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Set<String> userRoleSet = userDetails.getUser().getRoles()
                .stream()
                .map(Role::getValue)
                .collect(Collectors.toSet());

        return userRoleSet.contains(Role.ADMIN.getValue()) ||
                userRoleSet.contains(Role.PRESIDENT.getValue()) ||
                userRoleSet.contains(Role.VICE_PRESIDENT.getValue());
    }

    public boolean isAdminOrPresidentOrVicePresidentOrCircleLeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Set<String> userRoleSet = userDetails.getUser().getRoles()
                .stream()
                .map(Role::getValue)
                .collect(Collectors.toSet());

        return userRoleSet.contains(Role.ADMIN.getValue()) ||
                userRoleSet.contains(Role.PRESIDENT.getValue()) ||
                userRoleSet.contains(Role.VICE_PRESIDENT.getValue()) ||
                userRoleSet.contains(Role.LEADER_CIRCLE.getValue());
    }

    private int convertSemesterToGrade(int semester) {
        return (semester + 1) / 2;
    }
}
