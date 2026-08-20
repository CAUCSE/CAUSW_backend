package net.causw.app.main.domain.integration.crawled.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.integration.crawled.SiteConfigFixture;
import net.causw.app.main.domain.integration.crawled.entity.SiteConfig;
import net.causw.app.main.domain.integration.crawled.repository.SiteConfigRepository;
import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("SiteConfigReader 테스트")
class SiteConfigReaderTest {
	@InjectMocks
	private SiteConfigReader reader;

	@Mock
	private SiteConfigRepository repository;

	@Test
	@DisplayName("사이트 ID로 활성화된 DB 설정을 조회한다")
	void getEnabledBySiteId_shouldReturnConfig() {
		// given
		SiteConfig config = SiteConfigFixture.create();
		given(repository.findBySiteIdAndIsEnabledTrue("site")).willReturn(Optional.of(config));

		// when
		SiteConfig result = reader.getEnabledBySiteId("site");

		// then
		assertThat(result).isSameAs(config);
	}

	@Test
	@DisplayName("활성화된 설정이 없으면 예외를 발생시킨다")
	void getEnabledBySiteId_shouldThrow_whenConfigDoesNotExist() {
		// given
		given(repository.findBySiteIdAndIsEnabledTrue("site")).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> reader.getEnabledBySiteId("site"))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting(error -> ((BaseRunTimeV2Exception)error).getErrorCode())
			.isEqualTo(IntegrationErrorCode.CRAWL_SITE_CONFIG_NOT_FOUND);
	}

	@Test
	@DisplayName("활성화된 사이트 설정을 사이트 ID 순서로 조회한다")
	void findAllEnabled_shouldReturnConfigs() {
		// given
		List<SiteConfig> configs = List.of(
			SiteConfigFixture.create("first-site"),
			SiteConfigFixture.create("second-site"));
		given(repository.findAllByIsEnabledTrueOrderBySiteIdAsc()).willReturn(configs);

		// when
		List<SiteConfig> result = reader.findAllEnabled();

		// then
		assertThat(result).containsExactlyElementsOf(configs);
	}
}
