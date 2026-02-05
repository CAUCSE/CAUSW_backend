package net.causw.app.main.domain.user.academic.service.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

public record AcademicReturnApplicationListCondition(
        AcademicRecordRequestStatus requestStatus,
        Department department,
        String keyword,
        int page,
        int size
) {
}

