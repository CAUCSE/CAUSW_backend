package net.causw.app.main.domain.user.academic.service.implementation;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.repository.userAcademicRecord.UserAcademicRecordApplicationRepository;
import net.causw.app.main.domain.user.academic.repository.userAcademicRecord.query.UserAcademicRecordApplicationQueryRepository;
import net.causw.app.main.domain.user.academic.service.dto.request.AcademicReturnApplicationListCondition;
import net.causw.app.main.shared.exception.errorcode.AcademicRecordApplicationErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AcademicRecordApplicationReader {

    private final UserAcademicRecordApplicationRepository applicationRepository;
    private final UserAcademicRecordApplicationQueryRepository applicationQueryRepository;

    public Page<UserAcademicRecordApplication> findReturnApplications(
            AcademicReturnApplicationListCondition condition
    ) {
        PageRequest pageRequest = PageRequest.of(
                condition.page(),
                condition.size()
        );

        return applicationQueryRepository.searchReturnApplications(
                condition.requestStatus(),
                condition.department(),
                condition.keyword(),
                pageRequest
        );
    }

    public UserAcademicRecordApplication findById(String applicationId) {
        return applicationRepository.findById(applicationId)
                .orElseThrow(AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_FOUND::toBaseException);
    }
}
