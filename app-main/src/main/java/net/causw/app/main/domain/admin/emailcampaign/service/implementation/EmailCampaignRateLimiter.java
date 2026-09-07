package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import org.springframework.stereotype.Component;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;

import com.google.common.util.concurrent.RateLimiter;

@Component
public class EmailCampaignRateLimiter {

	private final RateLimiter rateLimiter;

	@SuppressWarnings("UnstableApiUsage")
	public EmailCampaignRateLimiter(EmailCampaignProperties properties) {
		double permitsPerSecond = properties.getMaxSendRate() > 0 ? properties.getMaxSendRate() : 1.0;
		this.rateLimiter = RateLimiter.create(permitsPerSecond);
	}

	@SuppressWarnings("UnstableApiUsage")
	public void acquire() {
		rateLimiter.acquire();
	}
}
