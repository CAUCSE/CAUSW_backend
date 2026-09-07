package net.causw.app.main.domain.admin.emailcampaign.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailCampaignDispatchConfig {

	@Bean(name = "emailCampaignDispatchExecutor", destroyMethod = "shutdown")
	public ExecutorService emailCampaignDispatchExecutor() {
		return Executors.newSingleThreadExecutor(Thread.ofPlatform().name("email-campaign-dispatcher-", 0).factory());
	}
}
