package net.causw.app.main.domain.admin.audit.event;

import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCreateCommand;
import net.causw.app.main.domain.admin.audit.service.implementation.AdminAuditLogWriter;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminAuditLogEventListener 단위 테스트")
class AdminAuditLogEventListenerTest {

	@Mock
	private AdminAuditLogWriter adminAuditLogWriter;

	@InjectMocks
	private AdminAuditLogEventListener adminAuditLogEventListener;

	@Test
	@DisplayName("감사 로그 이벤트를 수신하면 공통 감사 로그 writer에 생성 명령을 전달한다")
	void givenAdminAuditLogEvent_whenHandle_thenWriteAuditLog() {
		// given
		AdminAuditLogCreateCommand command = new AdminAuditLogCreateCommand(
			AdminAuditLogCategory.LOCKER,
			"ASSIGN",
			"사물함 배정",
			"admin-id",
			"admin@cau.ac.kr",
			"관리자",
			"20200000",
			"LOCKER",
			"locker-id",
			"user@cau.ac.kr",
			"사용자",
			"20201234",
			"사물함을 배정했습니다.",
			Map.of("사물함ID", "locker-id"));

		// when
		adminAuditLogEventListener.handle(new AdminAuditLogEvent(command));

		// then
		verify(adminAuditLogWriter).write(command);
	}
}
