package net.causw.app.main.domain.admin.audit.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.audit.service.implementation.AdminAuditLogWriter;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AdminAuditLogEventListener {

	private final AdminAuditLogWriter adminAuditLogWriter;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handle(AdminAuditLogEvent event) {
		adminAuditLogWriter.write(event.command());
	}
}
