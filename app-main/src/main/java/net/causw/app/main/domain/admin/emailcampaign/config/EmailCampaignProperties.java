package net.causw.app.main.domain.admin.emailcampaign.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.email-campaign")
public class EmailCampaignProperties {

	private boolean enabled = false;
	private String region = "ap-northeast-2";
	private String fromAddress;
	private String replyToAddress;
	private int batchSize = 50;
	private int maxConcurrency = 5;
	private double maxSendRate = 1.0;
	private int maxAttempts = 3;
	private long initialBackoffSeconds = 30;
	private long claimTimeoutSeconds = 300;
	private long recoveryIntervalMs = 30000;
}
