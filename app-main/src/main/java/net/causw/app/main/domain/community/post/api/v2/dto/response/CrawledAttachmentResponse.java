package net.causw.app.main.domain.community.post.api.v2.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CrawledAttachmentResponse(
	@Schema(description = "크롤링 원본 첨부파일명", example = "수강신청 안내.pdf") String name,
	@Schema(description = "크롤링 원본 첨부파일 URL", example = "https://example.com/files/course-registration.pdf") String url) {
}
