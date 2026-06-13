package net.causw.app.main.domain.admin.audit.api.v2.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;
import net.causw.app.main.domain.user.account.enums.user.UserAdminActionType;
import net.causw.app.main.domain.user.account.enums.user.UserState;

class AdminAuditLogMapperTest {

	private final AdminAuditLogMapper mapper = new AdminAuditLogMapper();

	@Nested
	@DisplayName("감사 로그 응답 변환 (toResponse)")
	class ToResponseTest {

		@Test
		@DisplayName("성공: 추방 로그를 공통 감사 로그 응답으로 변환한다")
		void givenDropLogItem_whenToResponse_thenReturnAuditLogResponse() {
			// given
			LocalDateTime createdAt = LocalDateTime.of(2026, 6, 13, 10, 30);
			AdminAuditLogItem item = new AdminAuditLogItem(
				"log-id",
				AdminAuditLogCategory.USER,
				"admin-id",
				"admin@causw.net",
				"user-id",
				"user@causw.net",
				UserAdminActionType.DROP,
				UserState.ACTIVE,
				UserState.DROP,
				"COMMON",
				"COMMON",
				"운영 정책 위반",
				createdAt);

			// when
			var response = mapper.toResponse(item);

			// then
			assertThat(response.id()).isEqualTo("log-id");
			assertThat(response.category()).isEqualTo(AdminAuditLogCategory.USER);
			assertThat(response.actionType()).isEqualTo("DROP");
			assertThat(response.actionDescription()).isEqualTo("유저 추방");
			assertThat(response.actor().userId()).isEqualTo("admin-id");
			assertThat(response.actor().email()).isEqualTo("admin@causw.net");
			assertThat(response.target().type()).isEqualTo("USER");
			assertThat(response.target().id()).isEqualTo("user-id");
			assertThat(response.target().email()).isEqualTo("user@causw.net");
			assertThat(response.summary()).isEqualTo("admin@causw.net dropped user user@causw.net");
			assertThat(response.metadata())
				.containsEntry("beforeState", "ACTIVE")
				.containsEntry("afterState", "DROP")
				.containsEntry("beforeRoles", "COMMON")
				.containsEntry("afterRoles", "COMMON")
				.containsEntry("reason", "운영 정책 위반");
			assertThat(response.createdAt()).isEqualTo(createdAt);
		}

		@Test
		@DisplayName("성공: 복구 로그 요약 문구를 생성한다")
		void givenRestoreLogItem_whenToResponse_thenReturnRestoreSummary() {
			// given
			AdminAuditLogItem item = new AdminAuditLogItem(
				"log-id",
				AdminAuditLogCategory.USER,
				"admin-id",
				"admin@causw.net",
				"user-id",
				"user@causw.net",
				UserAdminActionType.RESTORE,
				UserState.DROP,
				UserState.ACTIVE,
				"COMMON",
				"COMMON",
				null,
				LocalDateTime.of(2026, 6, 13, 10, 30));

			// when
			var response = mapper.toResponse(item);

			// then
			assertThat(response.summary()).isEqualTo("admin@causw.net restored user user@causw.net");
			assertThat(response.metadata()).containsEntry("reason", null);
		}

		@Test
		@DisplayName("성공: 권한 변경 로그 요약 문구를 생성한다")
		void givenRoleChangeLogItem_whenToResponse_thenReturnRoleChangeSummary() {
			// given
			AdminAuditLogItem item = new AdminAuditLogItem(
				"log-id",
				AdminAuditLogCategory.USER,
				"admin-id",
				"admin@causw.net",
				"user-id",
				"user@causw.net",
				UserAdminActionType.ROLE_CHANGE,
				UserState.ACTIVE,
				UserState.ACTIVE,
				"COMMON",
				"COUNCIL_LEADER",
				null,
				LocalDateTime.of(2026, 6, 13, 10, 30));

			// when
			var response = mapper.toResponse(item);

			// then
			assertThat(response.summary()).isEqualTo("admin@causw.net changed roles for user user@causw.net");
			assertThat(response.metadata())
				.containsEntry("beforeRoles", "COMMON")
				.containsEntry("afterRoles", "COUNCIL_LEADER");
		}
	}
}
