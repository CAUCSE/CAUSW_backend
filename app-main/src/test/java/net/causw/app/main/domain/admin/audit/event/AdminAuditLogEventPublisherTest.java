package net.causw.app.main.domain.admin.audit.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCreateCommand;
import net.causw.app.main.domain.asset.locker.entity.Locker;
import net.causw.app.main.domain.asset.locker.entity.LockerLocation;
import net.causw.app.main.domain.user.academic.entity.userAcademicRecord.UserAcademicRecordApplication;
import net.causw.app.main.domain.user.academic.enums.userAcademicRecord.AcademicStatus;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.entity.user.UserAdmission;
import net.causw.app.main.domain.user.account.enums.user.Department;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminAuditLogEventPublisher 단위 테스트")
class AdminAuditLogEventPublisherTest {

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private AdminAuditLogEventPublisher adminAuditLogEventPublisher;

	@Test
	@DisplayName("사물함 배정 이벤트는 LOCKER/ASSIGN 감사 로그 명령을 발행한다")
	void givenLockerAssignSnapshot_whenPublishLockerAssign_thenPublishLockerAssignAuditEvent() {
		// given
		User admin = user("admin-id", "admin@cau.ac.kr", "관리자", "20200000", null);
		User assignee = user("user-id", "user@cau.ac.kr", "사용자", "20201234", null);
		LocalDateTime expireDate = LocalDateTime.of(2026, 7, 15, 0, 0);
		LocalDateTime expiredAt = LocalDateTime.of(2026, 8, 15, 0, 0);
		Locker locker = locker("locker-id", 12L, "SECOND", expireDate);

		// when
		adminAuditLogEventPublisher.publishLockerAssign(locker, admin, assignee, expiredAt);

		// then
		AdminAuditLogCreateCommand command = captureCommand();
		assertThat(command.category()).isEqualTo(AdminAuditLogCategory.LOCKER);
		assertThat(command.actionType()).isEqualTo("ASSIGN");
		assertThat(command.actorUserId()).isEqualTo("admin-id");
		assertThat(command.targetType()).isEqualTo("LOCKER");
		assertThat(command.targetId()).isEqualTo("locker-id");
		assertThat(command.targetEmail()).isEqualTo("user@cau.ac.kr");
		assertThat(command.summary()).isEqualTo("SECOND-12 사물함을 사용자에게 배정했습니다.");
		assertThat(command.metadata())
			.containsEntry("lockerId", "locker-id")
			.containsEntry("lockerNumber", 12L)
			.containsEntry("lockerLocationName", "SECOND")
			.containsEntry("expireDate", expireDate)
			.containsEntry("expiredAt", expiredAt);
	}

	@Test
	@DisplayName("재학인증 거절 이벤트는 ACADEMIC/ADMISSION_REJECT 감사 로그 명령을 발행한다")
	void givenAdmissionRejectSnapshot_whenPublishAdmissionReject_thenPublishAdmissionRejectAuditEvent() {
		// given
		User admin = user("admin-id", "admin@cau.ac.kr", "관리자", "20200000", null);
		User targetUser = user("target-id", "target@cau.ac.kr", "대상자", "20209999", null);
		UserAdmission admission = admission("admission-id", targetUser);

		// when
		adminAuditLogEventPublisher.publishAdmissionReject(admission, admin, "서류 식별 불가");

		// then
		AdminAuditLogCreateCommand command = captureCommand();
		assertThat(command.category()).isEqualTo(AdminAuditLogCategory.ACADEMIC);
		assertThat(command.actionType()).isEqualTo("ADMISSION_REJECT");
		assertThat(command.targetType()).isEqualTo("USER");
		assertThat(command.targetId()).isEqualTo("target-id");
		assertThat(command.summary()).isEqualTo("대상자의 재학인증 신청을 거절했습니다.");
		assertThat(command.metadata())
			.containsEntry("admissionId", "admission-id")
			.containsEntry("requestedAcademicStatus", AcademicStatus.ENROLLED)
			.containsEntry("requestedStudentId", "20209999")
			.containsEntry("requestedAdmissionYear", 2020)
			.containsEntry("requestedDepartment", Department.SCHOOL_OF_SW)
			.containsEntry("requestedGraduationYear", 2024)
			.containsEntry("rejectReason", "서류 식별 불가");
	}

	@Test
	@DisplayName("학적변경 반려 이벤트는 ACADEMIC/ACADEMIC_RECORD_REJECT 감사 로그 명령을 발행한다")
	void givenAcademicRecordRejectSnapshot_whenPublishAcademicRecordReject_thenPublishAcademicRecordRejectAuditEvent() {
		// given
		User admin = user("admin-id", "admin@cau.ac.kr", "관리자", "20200000", null);
		User targetUser = user("target-id", "target@cau.ac.kr", "대상자", "20209999", AcademicStatus.ENROLLED);
		UserAcademicRecordApplication application = academicRecordApplication("application-id", targetUser);

		// when
		adminAuditLogEventPublisher.publishAcademicRecordReject(admin, application, "증빙 부족");

		// then
		AdminAuditLogCreateCommand command = captureCommand();
		assertThat(command.category()).isEqualTo(AdminAuditLogCategory.ACADEMIC);
		assertThat(command.actionType()).isEqualTo("ACADEMIC_RECORD_REJECT");
		assertThat(command.targetType()).isEqualTo("USER");
		assertThat(command.targetId()).isEqualTo("target-id");
		assertThat(command.summary()).isEqualTo("대상자의 학적변경 신청을 반려했습니다.");
		assertThat(command.metadata())
			.containsEntry("applicationId", "application-id")
			.containsEntry("beforeAcademicStatus", AcademicStatus.ENROLLED)
			.containsEntry("targetAcademicStatus", AcademicStatus.GRADUATED)
			.containsEntry("note", "졸업 처리 요청")
			.containsEntry("rejectReason", "증빙 부족");
	}

	private AdminAuditLogCreateCommand captureCommand() {
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertThat(captor.getValue()).isInstanceOf(AdminAuditLogEvent.class);
		return ((AdminAuditLogEvent)captor.getValue()).command();
	}

	private User user(String id, String email, String name, String studentId, AcademicStatus academicStatus) {
		User user = org.mockito.Mockito.mock(User.class);
		org.mockito.Mockito.lenient().when(user.getId()).thenReturn(id);
		when(user.getEmail()).thenReturn(email);
		when(user.getName()).thenReturn(name);
		when(user.getStudentId()).thenReturn(studentId);
		if (academicStatus != null) {
			when(user.getAcademicStatus()).thenReturn(academicStatus);
		}
		return user;
	}

	private Locker locker(String id, Long number, String locationName, LocalDateTime expireDate) {
		LockerLocation location = org.mockito.Mockito.mock(LockerLocation.class);
		when(location.getName()).thenReturn(locationName);

		Locker locker = org.mockito.Mockito.mock(Locker.class);
		when(locker.getId()).thenReturn(id);
		when(locker.getLockerNumber()).thenReturn(number);
		when(locker.getLocation()).thenReturn(location);
		when(locker.getExpireDate()).thenReturn(expireDate);
		return locker;
	}

	private UserAdmission admission(String id, User targetUser) {
		UserAdmission admission = org.mockito.Mockito.mock(UserAdmission.class);
		when(admission.getId()).thenReturn(id);
		when(admission.getUser()).thenReturn(targetUser);
		when(admission.getRequestedAcademicStatus()).thenReturn(AcademicStatus.ENROLLED);
		when(admission.getRequestedStudentId()).thenReturn("20209999");
		when(admission.getRequestedAdmissionYear()).thenReturn(2020);
		when(admission.getRequestedDepartment()).thenReturn(Department.SCHOOL_OF_SW);
		when(admission.getRequestedGraduationYear()).thenReturn(2024);
		return admission;
	}

	private UserAcademicRecordApplication academicRecordApplication(String id, User targetUser) {
		UserAcademicRecordApplication application = org.mockito.Mockito.mock(UserAcademicRecordApplication.class);
		when(application.getId()).thenReturn(id);
		when(application.getUser()).thenReturn(targetUser);
		when(application.getTargetAcademicStatus()).thenReturn(AcademicStatus.GRADUATED);
		when(application.getNote()).thenReturn("졸업 처리 요청");
		return application;
	}
}
