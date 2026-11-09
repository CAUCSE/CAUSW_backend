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

	/**
	 * 설정된 리전과 기본 AWS credential chain을 사용하는 SES v2 client를 생성한다.
	 * @return 애플리케이션에서 공유할 SES v2 client
	 */
	@Bean
	public SesV2Client sesV2Client() {
		return SesV2Client.builder()
			.region(Region.of(properties.getRegion()))
			.credentialsProvider(DefaultCredentialsProvider.builder().build())
			.build();
	}
}
