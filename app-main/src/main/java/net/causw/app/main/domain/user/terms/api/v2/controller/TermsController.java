package net.causw.app.main.domain.user.terms.api.v2.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.domain.user.terms.api.v2.dto.request.TermsAgreementRequestDto;
import net.causw.app.main.domain.user.terms.api.v2.dto.response.TermsResponseDto;
import net.causw.app.main.domain.user.terms.api.v2.dto.response.UserTermsAgreementStatusResponseDto;
import net.causw.app.main.domain.user.terms.service.v2.TermsService;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
		return ApiResponse.success(
			termsService.getTerms().stream().map(TermsResponseDto::from).toList());
	}

	@Operation(summary = "나의 약관 동의 상태 조회 V2", description = "현재 로그인한 유저의 약관 동의/미동의 현황을 조회합니다.")
	@GetMapping("/agreement-status")
	public ApiResponse<UserTermsAgreementStatusResponseDto> getAgreementStatus(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ApiResponse.success(
			UserTermsAgreementStatusResponseDto.from(
				termsService.getAgreementStatus(userDetails.getUser())));
	}

	@Operation(summary = "약관 동의 처리 V2", description = "지정한 약관에 동의 처리합니다. 전체 약관이 모두 포함되어야 합니다.")
	@PostMapping("/agreements")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Void> agreeToTerms(
		@Valid @RequestBody TermsAgreementRequestDto request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		termsService.agreeToTerms(userDetails.getUser(), request.termsIds());
		return ApiResponse.success();
	}
}
