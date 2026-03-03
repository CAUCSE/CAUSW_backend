package net.causw.app.main.domain.user.terms.api.v2.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.terms.api.v2.dto.response.TermsResponseDto;
import net.causw.app.main.domain.user.terms.service.v2.TermsService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Terms V2", description = "이용약관 API V2")
@RestController
@RequestMapping("api/v2/terms")
@RequiredArgsConstructor
public class TermsController {

	private final TermsService termsService;

	@Operation(summary = "이용약관 조회 V2", description = "모든 종류의 최신 버전 이용약관 목록을 조회합니다.")
	@GetMapping
	public ApiResponse<List<TermsResponseDto>> getTerms() {
		return ApiResponse.success(termsService.getTerms());
	}
}
