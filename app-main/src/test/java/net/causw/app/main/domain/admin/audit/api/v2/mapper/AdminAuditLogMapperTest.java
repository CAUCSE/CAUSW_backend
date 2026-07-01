package net.causw.app.main.domain.admin.audit.api.v2.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;

import tools.jackson.databind.ObjectMapper;

class AdminAuditLogMapperTest {

	private final AdminAuditLogMapper mapper = new AdminAuditLogMapper(new ObjectMapper());

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
				"DROP",
				"유저 추방",
				"admin-id",
				"admin@causw.net",
				"관리자",
				"20190001",
				"USER",
				"user-id",
				"user@causw.net",
				"대상자",
				"20200001",
				"admin@causw.net dropped user user@causw.net",
				"{\"beforeState\":\"ACTIVE\",\"afterState\":\"DROP\",\"beforeRoles\":\"COMMON\",\"afterRoles\":\"COMMON\",\"reason\":\"운영 정책 위반\"}",
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
			assertThat(response.actor().name()).isEqualTo("관리자");
			assertThat(response.actor().studentId()).isEqualTo("20190001");
			assertThat(response.target().type()).isEqualTo("USER");
			assertThat(response.target().id()).isEqualTo("user-id");
			assertThat(response.target().email()).isEqualTo("user@causw.net");
			assertThat(response.target().name()).isEqualTo("대상자");
			assertThat(response.target().studentId()).isEqualTo("20200001");
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
				"RESTORE",
				"추방 유저 복구",
				"admin-id",
				"admin@causw.net",
				"관리자",
				"20190001",
				"USER",
				"user-id",
				"user@causw.net",
				"대상자",
				"20200001",
				"admin@causw.net restored user user@causw.net",
				"{\"beforeState\":\"DROP\",\"afterState\":\"ACTIVE\",\"beforeRoles\":\"COMMON\",\"afterRoles\":\"COMMON\",\"reason\":null}",
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
				"ROLE_CHANGE",
				"유저 역할 변경",
				"admin-id",
				"admin@causw.net",
				"관리자",
				"20190001",
				"USER",
				"user-id",
				"user@causw.net",
				"대상자",
				"20200001",
				"admin@causw.net changed roles for user user@causw.net",
				"{\"beforeState\":\"ACTIVE\",\"afterState\":\"ACTIVE\",\"beforeRoles\":\"COMMON\",\"afterRoles\":\"COUNCIL_LEADER\",\"reason\":null}",
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
