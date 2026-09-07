package net.causw.app.main.shared.infra.mail.ses;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.causw.app.main.domain.admin.emailcampaign.config.EmailCampaignProperties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
@RequiredArgsConstructor
public class SesConfig {

	private final EmailCampaignProperties properties;

	@Bean
	public SesV2Client sesV2Client() {
		return SesV2Client.builder()
			.region(Region.of(properties.getRegion()))
			.credentialsProvider(DefaultCredentialsProvider.builder().build())
			.build();
	}
}
