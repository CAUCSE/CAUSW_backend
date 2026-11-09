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

	/**
	 * 발송 요청 트랜잭션 커밋 후 전용 executor에서 캠페인 dispatcher를 시작한다.
	 * @param event 커밋된 캠페인 발송 요청 이벤트
	 */
	@Async("emailCampaignDispatchExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(EmailCampaignSendRequestedEvent event) {
		dispatcher.dispatch(event.campaignId());
	}
}
