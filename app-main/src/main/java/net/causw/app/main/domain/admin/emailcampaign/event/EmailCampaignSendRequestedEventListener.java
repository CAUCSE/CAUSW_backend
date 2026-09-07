package net.causw.app.main.domain.admin.emailcampaign.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import net.causw.app.main.domain.admin.emailcampaign.service.implementation.EmailCampaignDispatcher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailCampaignSendRequestedEventListener {

	private final EmailCampaignDispatcher dispatcher;

	@Async("emailCampaignDispatchExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(EmailCampaignSendRequestedEvent event) {
		dispatcher.dispatch(event.campaignId());
	}
}
