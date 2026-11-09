package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;

import com.google.common.util.concurrent.RateLimiter;

@Component
public class EmailCampaignRateLimiter {

	private final RateLimiter rateLimiter;

	/**
	 * 설정된 초당 최대 발송 수로 JVM 로컬 Guava RateLimiter를 생성한다.
	 * @param properties 이메일 캠페인 설정
	 */
	@SuppressWarnings("UnstableApiUsage")
	public EmailCampaignRateLimiter(EmailCampaignProperties properties) {
		double permitsPerSecond = properties.getMaxSendRate() > 0 ? properties.getMaxSendRate() : 1.0;
		this.rateLimiter = RateLimiter.create(permitsPerSecond);
	}

	/**
	 * 다음 SES 요청을 위한 permit을 얻을 때까지 현재 스레드를 대기시킨다.
	 */
	@SuppressWarnings("UnstableApiUsage")
	public void acquire() {
		rateLimiter.acquire();
	}
}
