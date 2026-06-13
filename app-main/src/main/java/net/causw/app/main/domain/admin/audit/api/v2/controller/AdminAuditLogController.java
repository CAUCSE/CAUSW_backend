package net.causw.app.main.domain.admin.audit.api.v2.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.admin.audit.api.v2.dto.request.AdminAuditLogRequest;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AdminAuditLogResponse;
import net.causw.app.main.domain.admin.audit.api.v2.mapper.AdminAuditLogMapper;
import net.causw.app.main.domain.admin.audit.service.AdminAuditLogService;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/audit-logs")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "Admin Audit Log v2", description = "관리자 감사 로그 API")
public class AdminAuditLogController {

	private final AdminAuditLogService adminAuditLogService;
	private final AdminAuditLogMapper adminAuditLogMapper;

	@Operation(summary = "관리자 감사 로그 목록 조회", description = "관리자가 수행한 주요 관리자 활동 로그를 조회합니다.")
	@GetMapping
	public ApiResponse<PageResponse<AdminAuditLogResponse>> getAuditLogs(
		@ParameterObject AdminAuditLogRequest request,
		@ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable) {
		return ApiResponse.success(
			PageResponse.from(
				adminAuditLogService.getAuditLogs(request, pageable)
					.map(adminAuditLogMapper::toResponse)));
	}
}
