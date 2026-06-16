package net.causw.app.main.domain.admin.audit.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.admin.audit.entity.AdminAuditLog;
import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.repository.AdminAuditLogRepository;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCreateCommand;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AdminAuditLogWriterTest {

	@Mock
	private AdminAuditLogRepository adminAuditLogRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private AdminAuditLogWriter adminAuditLogWriter;

	@BeforeEach
	void setUp() {
		adminAuditLogWriter = new AdminAuditLogWriter(adminAuditLogRepository, objectMapper);
	}

	@Nested
	@DisplayName("감사 로그 저장")
	class Write {

		@Test
		@DisplayName("유저 추방 감사 로그는 스냅샷과 메타데이터를 공통 감사 로그로 저장한다")
		void givenDropCommand_whenWrite_thenSaveAdminAuditLog() throws Exception {
			// given
			AdminAuditLogCreateCommand command = command(
				"DROP",
				"유저 추방",
				"admin@example.com dropped user target@example.com",
				Map.of(
					"beforeState", "ACTIVE",
					"afterState", "DROP",
					"beforeRoles", "COMMON",
					"afterRoles", "NONE",
					"reason", "운영 정책 위반"));
			given(adminAuditLogRepository.save(any(AdminAuditLog.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			adminAuditLogWriter.write(command);

			// then
			AdminAuditLog savedLog = captureSavedLog();
			assertThat(savedLog.getCategory()).isEqualTo(AdminAuditLogCategory.USER);
			assertThat(savedLog.getActionType()).isEqualTo("DROP");
			assertThat(savedLog.getActionDescription()).isEqualTo("유저 추방");
			assertThat(savedLog.getActorUserId()).isEqualTo("admin-id");
			assertThat(savedLog.getActorEmail()).isEqualTo("admin@example.com");
			assertThat(savedLog.getActorName()).isEqualTo("관리자");
			assertThat(savedLog.getActorStudentId()).isEqualTo("20180001");
			assertThat(savedLog.getTargetType()).isEqualTo("USER");
			assertThat(savedLog.getTargetId()).isEqualTo("target-id");
			assertThat(savedLog.getTargetEmail()).isEqualTo("target@example.com");
			assertThat(savedLog.getTargetName()).isEqualTo("대상자");
			assertThat(savedLog.getTargetStudentId()).isEqualTo("20200001");
			assertThat(savedLog.getSummary()).isEqualTo("admin@example.com dropped user target@example.com");

			Map<String, Object> metadata = readMetadata(savedLog);
			assertThat(metadata)
				.containsEntry("beforeState", "ACTIVE")
				.containsEntry("afterState", "DROP")
				.containsEntry("beforeRoles", "COMMON")
				.containsEntry("afterRoles", "NONE")
				.containsEntry("reason", "운영 정책 위반");
		}

		@Test
		@DisplayName("유저 복구 감사 로그는 이전/이후 상태와 역할 메타데이터를 저장한다")
		void givenRestoreCommand_whenWrite_thenSaveStateAndRoleMetadata() throws Exception {
			// given
			AdminAuditLogCreateCommand command = command(
				"RESTORE",
				"추방 유저 복구",
				"admin@example.com restored user target@example.com",
				Map.of(
					"beforeState", "DROP",
					"afterState", "ACTIVE",
					"beforeRoles", "NONE",
					"afterRoles", "COMMON"));
			given(adminAuditLogRepository.save(any(AdminAuditLog.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			adminAuditLogWriter.write(command);

			// then
			AdminAuditLog savedLog = captureSavedLog();
			assertThat(savedLog.getActionType()).isEqualTo("RESTORE");
			assertThat(savedLog.getSummary()).isEqualTo("admin@example.com restored user target@example.com");
			assertThat(readMetadata(savedLog))
				.containsEntry("beforeState", "DROP")
				.containsEntry("afterState", "ACTIVE")
				.containsEntry("beforeRoles", "NONE")
				.containsEntry("afterRoles", "COMMON");
		}

		@Test
		@DisplayName("유저 권한 변경 감사 로그는 변경 전후 역할 메타데이터를 저장한다")
		void givenRoleChangeCommand_whenWrite_thenSaveRoleMetadata() throws Exception {
			// given
			AdminAuditLogCreateCommand command = command(
				"ROLE_CHANGE",
				"유저 역할 변경",
				"admin@example.com changed roles for user target@example.com",
				Map.of(
					"beforeState", "ACTIVE",
					"afterState", "ACTIVE",
					"beforeRoles", "COMMON",
					"afterRoles", "COMMON,COUNCIL"));
			given(adminAuditLogRepository.save(any(AdminAuditLog.class)))
				.willAnswer(invocation -> invocation.getArgument(0));

			// when
			adminAuditLogWriter.write(command);

			// then
			AdminAuditLog savedLog = captureSavedLog();
			assertThat(savedLog.getActionType()).isEqualTo("ROLE_CHANGE");
			assertThat(savedLog.getSummary()).isEqualTo("admin@example.com changed roles for user target@example.com");
			assertThat(readMetadata(savedLog))
				.containsEntry("beforeRoles", "COMMON")
				.containsEntry("afterRoles", "COMMON,COUNCIL");
		}
	}

	private AdminAuditLogCreateCommand command(
		String actionType,
		String actionDescription,
		String summary,
		Map<String, Object> metadata) {
		return new AdminAuditLogCreateCommand(
			AdminAuditLogCategory.USER,
			actionType,
			actionDescription,
			"admin-id",
			"admin@example.com",
			"관리자",
			"20180001",
			"USER",
			"target-id",
			"target@example.com",
			"대상자",
			"20200001",
			summary,
			metadata);
	}

	private AdminAuditLog captureSavedLog() {
		ArgumentCaptor<AdminAuditLog> captor = ArgumentCaptor.forClass(AdminAuditLog.class);
		verify(adminAuditLogRepository).save(captor.capture());
		return captor.getValue();
	}

	private Map<String, Object> readMetadata(AdminAuditLog log) throws Exception {
		return objectMapper.readValue(log.getMetadataJson(), new TypeReference<>() {});
	}
}
