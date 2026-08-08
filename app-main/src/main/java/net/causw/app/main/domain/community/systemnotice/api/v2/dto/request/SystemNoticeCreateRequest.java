package net.causw.app.main.domain.community.systemnotice.api.v2.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SystemNoticeCreateRequest(@NotBlank String content) {
}
