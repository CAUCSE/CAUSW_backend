package net.causw.app.main.domain.admin.audit.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.audit.service.implementation.AdminAuditLogWriter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminAuditLogEventListener {

	private final AdminAuditLogWriter adminAuditLogWriter;

	@EventListener
	public void handle(AdminAuditLogEvent event) {
		adminAuditLogWriter.write(event.command());
	}
}
