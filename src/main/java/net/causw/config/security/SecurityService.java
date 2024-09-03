package net.causw.config.security;

import lombok.RequiredArgsConstructor;
import net.causw.adapter.persistence.form.Form;
import net.causw.adapter.persistence.repository.FormRepository;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.ErrorCode;
import net.causw.domain.model.enums.UserState;
import net.causw.domain.model.util.MessageUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    private int convertSemesterToGrade(int semester) {
        return (semester + 1) / 2;
    }
}
