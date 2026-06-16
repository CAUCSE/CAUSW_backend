package net.causw.app.main.domain.admin.audit.api.v2.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.audit.api.v2.dto.request.AdminAuditLogRequest;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AdminAuditLogResponse;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AuditActorResponse;
import net.causw.app.main.domain.admin.audit.api.v2.dto.response.AuditTargetResponse;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCondition;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogItem;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminAuditLogMapper {

	private final ObjectMapper objectMapper;

	/**
	 * 관리자 감사 로그 서비스 DTO를 API 응답 DTO로 변환
	 * @param item 관리자 감사 로그 서비스 DTO
	 * @return 관리자 감사 로그 API 응답 DTO
	 */
	public AdminAuditLogResponse toResponse(AdminAuditLogItem item) {
		return new AdminAuditLogResponse(
			item.id(),
			item.category(),
			item.actionType(),
			item.actionDescription(),
			new AuditActorResponse(item.actorUserId(), item.actorEmail(), item.actorName(), item.actorStudentId()),
			new AuditTargetResponse(
				item.targetType(),
				item.targetId(),
				item.targetEmail(),
				item.targetName(),
				item.targetStudentId()),
			item.summary(),
			toMetadata(item.metadataJson()),
			item.createdAt());
	}

	private Map<String, Object> toMetadata(String metadataJson) {
		if (metadataJson == null || metadataJson.isBlank()) {
			return new LinkedHashMap<>();
		}
		try {
			return objectMapper.readValue(metadataJson, new TypeReference<LinkedHashMap<String, Object>>() {});
		} catch (JsonProcessingException e) {
			throw GlobalErrorCode.INTERNAL_SERVER_ERROR.toBaseException();
		}
	}

	public AdminAuditLogCondition toCondition(AdminAuditLogRequest request) {
		return new AdminAuditLogCondition(
			request.from(),
			request.to(),
			request.category(),
			request.actionType(),
			request.keyword());
	}
}
