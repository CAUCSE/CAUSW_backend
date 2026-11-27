package net.causw.app.main.domain.notification.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CeremonyNotificationSettingTest {

	@Nested
	@DisplayName("기본값")
	class DefaultValueTest {

		@Test
		@DisplayName("Builder로 값을 지정하지 않고 생성하면 전체 학번 알림 수신 기본값을 유지한다")
		void givenNoFieldValues_whenBuild_thenKeepDefaultValues() {
			CeremonyNotificationSetting setting = CeremonyNotificationSetting.builder().build();

			assertThat(setting.getSubscribedAdmissionYears()).isNotNull().isEmpty();
			assertThat(setting.isNotificationActive()).isTrue();
			assertThat(setting.isSetAll()).isTrue();
		}

		@Test
		@DisplayName("기본 생성자로 생성하면 전체 학번 알림 수신 기본값을 유지한다")
		void givenNoArgs_whenConstruct_thenKeepDefaultValues() {
			CeremonyNotificationSetting setting = new CeremonyNotificationSetting();

			assertThat(setting.getSubscribedAdmissionYears()).isNotNull().isEmpty();
			assertThat(setting.isNotificationActive()).isTrue();
			assertThat(setting.isSetAll()).isTrue();
		}
	}
}
