package net.causw.app.main.domain.user.academic.service;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.user.academic.service.dto.request.AcademicReturnApplicationListCondition;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicReturnApplicationSummaryResult;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationReader;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademicRecordAdminService {

    private final AcademicRecordApplicationReader applicationReader;

    @Transactional(readOnly = true)
    public Page<AcademicReturnApplicationSummaryResult> getApplications(AcademicReturnApplicationListCondition condition) {
        return applicationReader.findReturnApplications(condition)
                .map(AcademicReturnApplicationSummaryResult::from);
    }
}
