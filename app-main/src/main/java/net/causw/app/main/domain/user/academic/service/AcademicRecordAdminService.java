package net.causw.app.main.domain.user.academic.service;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.service.dto.request.AcademicReturnApplicationListCondition;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicReturnApplicationDetailResult;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicReturnApplicationSummaryResult;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationReader;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationWriter;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordLogCreator;
import net.causw.app.main.domain.user.account.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademicRecordAdminService {

    private final AcademicRecordApplicationReader applicationReader;
    private final AcademicRecordApplicationWriter applicationWriter;
    private final AcademicRecordLogCreator logCreator;

    @Transactional(readOnly = true)
    public Page<AcademicReturnApplicationSummaryResult> getApplications(AcademicReturnApplicationListCondition condition) {
        return applicationReader.findReturnApplications(condition)
                .map(AcademicReturnApplicationSummaryResult::from);
    }

    @Transactional(readOnly = true)
    public AcademicReturnApplicationDetailResult getApplicationDetail(String applicationId) {
        return AcademicReturnApplicationDetailResult.from(
                applicationReader.findById(applicationId)
        );
    }

    @Transactional
    public void approve(User admin, String applicationId) {
        UserAcademicRecordApplication application = applicationReader.findById(applicationId);
        applicationWriter.approve(application);
        logCreator.createFromApplication(admin, application);
    }

    @Transactional
    public void reject(User admin, String applicationId, String rejectReason) {
        UserAcademicRecordApplication application = applicationReader.findById(applicationId);
        applicationWriter.reject(application, rejectReason);
        logCreator.createFromApplication(admin, application);
    }
}
