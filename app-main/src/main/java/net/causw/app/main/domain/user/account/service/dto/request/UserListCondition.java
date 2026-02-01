package net.causw.app.main.domain.user.account.service.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;
import net.causw.app.main.domain.user.account.enums.user.UserState;

public record UserListCondition(
        String keyword,
        UserState state,
        AcademicStatus academicStatus,
        Department department
) {
}
