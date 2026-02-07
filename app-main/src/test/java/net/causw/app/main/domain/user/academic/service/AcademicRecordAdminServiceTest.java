
package net.causw.app.main.domain.user.academic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;

import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.AcademicRecordApplicationErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicRecordRequestStatus;
import net.causw.app.main.domain.user.academic.service.dto.request.AcademicReturnApplicationListCondition;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicReturnApplicationDetailResult;
import net.causw.app.main.domain.user.academic.service.dto.response.AcademicReturnApplicationSummaryResult;
import net.causw.app.main.domain.user.academic.service.implementation.AcademicRecordApplicationReader;
import net.causw.app.main.util.ObjectFixtures;

@ExtendWith(MockitoExtension.class)
class AcademicRecordAdminServiceTest {

    @Mock
    private AcademicRecordApplicationReader applicationReader;

    @InjectMocks
    private AcademicRecordAdminService academicRecordAdminService;

    @Test
    @DisplayName("학적 변경 신청 목록을 조회한다")
    void getApplications_success() {
        // given
        AcademicReturnApplicationListCondition condition =
                new AcademicReturnApplicationListCondition(
                        AcademicRecordRequestStatus.AWAIT,
                        null,
                        null,
                        0,
                        10
                );

        User user = ObjectFixtures.getCertifiedUser();

        UserAcademicRecordApplication application =
                UserAcademicRecordApplication.create(
                        user,
                        AcademicRecordRequestStatus.AWAIT,
                        AcademicStatus.ENROLLED,      // targetAcademicStatus
                        null,                         // targetCompletedSemester
                        "복학 신청합니다"
                );

        Page<UserAcademicRecordApplication> mockPage =
                new PageImpl<>(
                        List.of(application),
                        PageRequest.of(0, 10),
                        1
                );

        when(applicationReader.findReturnApplications(condition))
                .thenReturn(mockPage);

        // when
        Page<AcademicReturnApplicationSummaryResult> result =
                academicRecordAdminService.getApplications(condition);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(applicationReader, times(1))
                .findReturnApplications(condition);
    }

    @Test
    @DisplayName("학적 변경 신청 상세를 조회한다")
    void getApplicationDetail_success() {
        // given
        String applicationId = "application-id";

        User user = ObjectFixtures.getCertifiedUser();

        UserAcademicRecordApplication application =
                UserAcademicRecordApplication.create(
                        user,
                        AcademicRecordRequestStatus.AWAIT,
                        AcademicStatus.ENROLLED,
                        null,
                        "복학 신청합니다"
                );

        when(applicationReader.findById(applicationId))
                .thenReturn(application);

        // when
        AcademicReturnApplicationDetailResult result =
                academicRecordAdminService.getApplicationDetail(applicationId);

        // then
        assertThat(result.userName()).isEqualTo(user.getName());
        assertThat(result.studentId()).isEqualTo(user.getStudentId());
        assertThat(result.targetAcademicStatus()).isEqualTo(AcademicStatus.ENROLLED);
        assertThat(result.requestStatus()).isEqualTo(AcademicRecordRequestStatus.AWAIT);
        assertThat(result.note()).isEqualTo("복학 신청합니다");
        assertThat(result.attachImageUrls()).isEmpty();

        verify(applicationReader, times(1))
                .findById(applicationId);
    }

    @Test
    @DisplayName("존재하지 않는 신청 ID로 상세 조회 시 예외가 발생한다")
    void getApplicationDetail_notFound() {
        // given
        String applicationId = "non-existent-id";

        when(applicationReader.findById(applicationId))
                .thenThrow(AcademicRecordApplicationErrorCode.ACADEMIC_RECORD_APPLICATION_NOT_FOUND.toBaseException());

        // when & then
        assertThatThrownBy(() -> academicRecordAdminService.getApplicationDetail(applicationId))
                .isInstanceOf(BaseRunTimeV2Exception.class);

        verify(applicationReader, times(1))
                .findById(applicationId);
    }
}
