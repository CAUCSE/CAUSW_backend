package net.causw.app.main.domain.user.academic.api.v2.dto.request;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.account.enums.user.Department;

public record AcademicReturnApplicationListRequest(
        AcademicRecordRequestStatus requestStatus,
        Department department,
        String keyword,
        Integer page,
        Integer size
) {
}

