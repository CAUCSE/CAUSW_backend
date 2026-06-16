package net.causw.app.main.domain.admin.audit.api.v2.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import net.causw.app.main.domain.admin.audit.api.v2.dto.request.AdminAuditLogRequest;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AdminAuditLogResponse;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AuditActorResponse;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AuditTargetResponse;
import net.causw.app.main.domain.admin.audit.api.v2.mapper.AdminAuditLogMapper;
import net.causw.app.main.domain.admin.audit.enums.AdminAuditLogCategory;
import net.causw.app.main.domain.admin.audit.service.AdminAuditLogService;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCondition;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;
import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.auth.service.SecurityService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminAuditLogController.class)
@Import({SecurityService.class, Role.RoleComponent.class, AdminAuditLogControllerTest.MethodSecurityTestConfig.class})
class AdminAuditLogControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AdminAuditLogService adminAuditLogService;

	@MockBean
	private AdminAuditLogMapper adminAuditLogMapper;

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("ADMIN 권한이면 감사 로그 목록을 조회하고 요청 파라미터를 바인딩한다")
	void givenAdmin_whenGetAuditLogs_thenReturnOkAndBindRequest() throws Exception {
		// given
		AdminAuditLogItem item = item();
		Page<AdminAuditLogItem> page = new PageImpl<>(java.util.List.of(item));
		AdminAuditLogCondition condition = new AdminAuditLogCondition(
			null,
			null,
			AdminAuditLogCategory.USER,
			"DROP",
			"admin");

		given(adminAuditLogMapper.toCondition(org.mockito.ArgumentMatchers.any(AdminAuditLogRequest.class))).willReturn(condition);
		given(adminAuditLogService.getAuditLogs(any(AdminAuditLogCondition.class), any(Pageable.class)))
			.willReturn(page);
		given(adminAuditLogMapper.toResponse(item)).willReturn(response());

		// when
		mockMvc.perform(get("/api/v2/admin/audit-logs")
			.param("category", "USER")
			.param("actionType", "DROP")
			.param("keyword", "admin")
			.param("page", "0")
			.param("size", "10"))
			.andExpect(status().isOk());

		// then
		ArgumentCaptor<AdminAuditLogCondition> conditionCaptor = ArgumentCaptor.forClass(AdminAuditLogCondition.class);
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(adminAuditLogService).getAuditLogs(conditionCaptor.capture(), pageableCaptor.capture());
		org.assertj.core.api.Assertions.assertThat(conditionCaptor.getValue().category()).isEqualTo(AdminAuditLogCategory.USER);
		org.assertj.core.api.Assertions.assertThat(conditionCaptor.getValue().actionType()).isEqualTo("DROP");
		org.assertj.core.api.Assertions.assertThat(conditionCaptor.getValue().keyword()).isEqualTo("admin");
		org.assertj.core.api.Assertions.assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
	}

	@TestConfiguration
	@EnableMethodSecurity
	static class MethodSecurityTestConfig {}

	private AdminAuditLogItem item() {
		return new AdminAuditLogItem(
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
			"{\"reason\":\"운영 정책 위반\"}",
			LocalDateTime.of(2026, 6, 13, 10, 30));
	}

	private AdminAuditLogResponse response() {
		return new AdminAuditLogResponse(
			"log-id",
			AdminAuditLogCategory.USER,
			"DROP",
			"유저 추방",
			new AuditActorResponse("admin-id", "admin@causw.net", "관리자", "20190001"),
			new AuditTargetResponse("USER", "user-id", "user@causw.net", "대상자", "20200001"),
			"admin@causw.net dropped user user@causw.net",
			Map.of("reason", "운영 정책 위반"),
			LocalDateTime.of(2026, 6, 13, 10, 30));
	}
}
