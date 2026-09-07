package net.causw.app.main.domain.admin.emailcampaign.service.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.causw.app.main.shared.exception.BaseRunTimeV2Exception;
import net.causw.app.main.shared.exception.errorcode.EmailCampaignErrorCode;

class EmailHtmlSanitizerTest {

	private final EmailHtmlSanitizer sanitizer = new EmailHtmlSanitizer();

	@Test
	@DisplayName("허용된 HTTPS 링크와 이메일용 스타일은 보존한다")
	void preserveAllowedMarkup() {
		// when
		String result = sanitizer.sanitize(
			"<p style=\"color: red; text-align: center\"><a href=\"https://example.com\">행사</a></p>");

		// then
		assertThat(result).contains("color: red", "text-align: center", "https://example.com", "행사");
	}

	@Test
	@DisplayName("스크립트 이벤트 속성과 위험 URL을 제거한다")
	void removeDangerousMarkup() {
		// when
		String result = sanitizer.sanitize(
			"<script>alert(1)</script><p onclick=\"alert(2)\"><a href=\"javascript:alert(3)\">링크</a>본문</p>");

		// then
		assertThat(result).doesNotContain("script", "onclick", "javascript:", "alert").contains("링크", "본문");
	}

	@Test
	@DisplayName("정제 후 내용이 비면 잘못된 HTML로 거부한다")
	void rejectEmptySanitizedHtml() {
		// when & then
		assertThatThrownBy(() -> sanitizer.sanitize("<script>alert(1)</script>"))
			.isInstanceOf(BaseRunTimeV2Exception.class)
			.extracting(exception -> ((BaseRunTimeV2Exception)exception).getErrorCode())
			.isEqualTo(EmailCampaignErrorCode.EMAIL_CAMPAIGN_INVALID_HTML);
	}
}
