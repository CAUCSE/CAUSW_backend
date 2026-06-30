package net.causw.app.main.domain.admin.audit.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.admin.audit.service.implementation.AdminAuditLogWriter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminAuditLogEventListener {

	private final AdminAuditLogWriter adminAuditLogWriter;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(AdminAuditLogEvent event) {
		adminAuditLogWriter.write(event.command());
	}
}
