package net.causw.app.main.domain.community.systemnotice.api.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SystemNoticeCreateRequest(
	@NotBlank @Size(max = 255, message = "시스템 공지 제목은 255자를 초과할 수 없습니다.") String title,
	@NotBlank String content) {
}
