package net.causw.app.main.domain.admin.monitoring.api.v2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.core.aop.annotation.RequireAdminRole;
import net.causw.app.main.core.aop.enums.AdminTarget;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.ServiceUnavailableException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v2/admin/system-error-tests")
@PreAuthorize("@security.hasAnyRole(@Role.ADMIN, @Role.SYSTEM_ADMIN)")
@RequireAdminRole(target = AdminTarget.SYSTEM_ONLY)
@Tag(name = "System Error Alert Test v2", description = "서버 오류 알림 검증 API")
public class ServerErrorAlertTestController {

	@PostMapping("/internal-server-error")
	@Operation(summary = "HTTP 500 알림 테스트", description = "미처리 서버 예외를 발생시켜 오류 알림을 검증합니다.")
	public ApiResponse<Void> throwInternalServerError() {
		throw new IllegalStateException("Alert test: internal server error");
	}

	@PostMapping("/service-unavailable")
	@Operation(summary = "HTTP 503 알림 테스트", description = "서비스 불가 예외를 발생시켜 오류 알림을 검증합니다.")
	public ApiResponse<Void> throwServiceUnavailable() {
		throw new ServiceUnavailableException(ErrorCode.SERVICE_UNAVAILABLE, "Alert test: service unavailable");
	}
}
