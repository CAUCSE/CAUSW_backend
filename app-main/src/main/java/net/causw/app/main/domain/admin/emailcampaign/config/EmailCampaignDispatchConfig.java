package net.causw.app.main.domain.admin.emailcampaign.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailCampaignDispatchConfig {

	/**
	 * 캠페인 간 발송 순서를 직렬화하는 전용 platform-thread executor를 생성한다.
	 * @return 비동기 발송 이벤트 처리 executor
	 */
	@Bean(name = "emailCampaignDispatchExecutor", destroyMethod = "shutdown")
	public ExecutorService emailCampaignDispatchExecutor() {
		return Executors.newSingleThreadExecutor(Thread.ofPlatform().name("email-campaign-dispatcher-", 0).factory());
	}
}
