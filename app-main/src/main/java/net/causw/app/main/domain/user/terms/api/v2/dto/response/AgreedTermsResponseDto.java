package net.causw.app.main.domain.user.terms.api.v2.dto.response;

import java.time.LocalDateTime;

import net.causw.app.main.domain.user.terms.entity.TermsType;
import net.causw.app.main.domain.user.terms.service.v2.dto.AgreedTermsInfo;

import io.swagger.v3.oas.annotations.media.Schema;

public record AgreedTermsResponseDto(
	@Schema(description = "약관 종류") TermsType type,
	@Schema(description = "동의한 버전") int version,
	@Schema(description = "동의 일시") LocalDateTime agreedAt) {

	public static AgreedTermsResponseDto from(AgreedTermsInfo info) {
		return new AgreedTermsResponseDto(info.type(), info.version(), info.agreedAt());
	}
}
