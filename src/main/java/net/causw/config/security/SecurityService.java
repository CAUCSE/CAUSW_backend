package net.causw.config.security;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.repository.FormRepository;
import net.causw.adapter.persistence.user.User;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.AcademicStatus;
import net.causw.domain.model.enums.Role;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

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
        return userDetails.getUserState() == UserState.ACTIVE &&
                userDetails.getAuthorities().stream()
                        .noneMatch(authority -> authority.getAuthority().equals("ROLE_NONE"));
    }

    public boolean hasAccessToForm(String formId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Integer userSemester = userDetails.getUser().getCurrentCompletedSemester();

        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.FORM_NOT_FOUND));

        return form.getAllowedGrades().contains(convertSemesterToGrade(userSemester));
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

    public boolean isAdminOrPresidentOrVicePresident() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Set<Role> userRoleSet = userDetails.getUser().getRoles();
        return userRoleSet.contains(Role.ADMIN) ||
                userRoleSet.contains(Role.PRESIDENT) ||
                userRoleSet.contains(Role.VICE_PRESIDENT);
    }

    public boolean isAdminOrPresidentOrVicePresidentOrCircleLeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        Set<Role> userRoleSet = userDetails.getUser().getRoles();

        return userRoleSet.contains(Role.ADMIN) ||
                userRoleSet.contains(Role.PRESIDENT) ||
                userRoleSet.contains(Role.VICE_PRESIDENT) ||
                userRoleSet.contains(Role.LEADER_CIRCLE);
    }

    private int convertSemesterToGrade(int semester) {
        return (semester + 1) / 2;
    }
}
