package net.causw.app.main.domain.admin.audit.service.implementation;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.admin.audit.entity.AdminAuditLog;
import net.causw.app.main.domain.admin.audit.repository.AdminAuditLogRepository;
import net.causw.app.main.domain.admin.audit.service.dto.AdminAuditLogCreateCommand;
import net.causw.app.main.shared.exception.errorcode.GlobalErrorCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class AdminAuditLogWriter {

	private final AdminAuditLogRepository adminAuditLogRepository;
	private final ObjectMapper objectMapper;

	public void write(AdminAuditLogCreateCommand command) {
		adminAuditLogRepository.save(AdminAuditLog.of(
			command.category(),
			command.actionType(),
			command.actionDescription(),
			command.actorUserId(),
			command.actorEmail(),
			command.actorName(),
			command.actorStudentId(),
			command.targetType(),
			command.targetId(),
			command.targetEmail(),
			command.targetName(),
			command.targetStudentId(),
			command.summary(),
			serializeMetadata(command.metadata())));
	}

	private String serializeMetadata(Map<String, Object> metadata) {
		try {
			return objectMapper.writeValueAsString(metadata == null ? Map.of() : metadata);
		} catch (JsonProcessingException exception) {
			throw GlobalErrorCode.INTERNAL_SERVER_ERROR.toBaseException();
		}
	}
}
