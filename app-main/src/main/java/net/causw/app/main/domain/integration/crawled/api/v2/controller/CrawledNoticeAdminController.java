package net.causw.app.main.domain.integration.crawled.api.v2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.integration.crawled.service.PostCategoryBackfillService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/crawled-notices")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "Crawled Notice Admin v2", description = "관리자 크롤링 공지 관리 API")
public class CrawledNoticeAdminController {

	private final PostCategoryBackfillService postCategoryBackfillService;

	@PostMapping("/categories/backfill")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "크롤링 게시글 성격 일괄 분류", description = "성격이 미분류로 남은 기존 크롤링 게시글을 소급 분류하고 분류된 건수를 반환합니다.")
	public ApiResponse<Integer> backfillCategories() {
		return ApiResponse.success(postCategoryBackfillService.backfill());
	}
}
