package net.causw.app.main.domain.admin.monitoring.api.v2.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import net.causw.app.main.domain.user.account.enums.user.Role;
import net.causw.app.main.domain.user.auth.service.SecurityService;
import net.causw.app.main.shared.exception.GlobalV2ExceptionHandler;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ServerErrorAlertTestController.class)
@Import({
	SecurityService.class,
	Role.RoleComponent.class,
	GlobalV2ExceptionHandler.class,
	ServerErrorAlertTestControllerTest.MethodSecurityTestConfig.class
})
class ServerErrorAlertTestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("ADMIN 권한이면 미처리 예외를 발생시켜 HTTP 500을 반환한다")
	void givenAdmin_whenThrowInternalServerError_thenReturnInternalServerError() throws Exception {
		// when & then
		mockMvc.perform(post("/api/v2/admin/system-error-tests/internal-server-error").with(csrf()))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value("G50001"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	@DisplayName("ADMIN 권한이면 서비스 불가 예외를 발생시켜 HTTP 503을 반환한다")
	void givenAdmin_whenThrowServiceUnavailable_thenReturnServiceUnavailable() throws Exception {
		// when & then
		mockMvc.perform(post("/api/v2/admin/system-error-tests/service-unavailable").with(csrf()))
			.andExpect(status().isServiceUnavailable())
			.andExpect(jsonPath("$.code").value("G50301"));
	}

	@TestConfiguration
	@EnableMethodSecurity
	static class MethodSecurityTestConfig {}
}
