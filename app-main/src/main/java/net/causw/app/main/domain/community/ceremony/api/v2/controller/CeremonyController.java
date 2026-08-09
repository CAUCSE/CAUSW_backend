package net.causw.app.main.domain.community.ceremony.api.v2.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CeremonyCreateRequest;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyDetailResponse;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonySummaryResponse;
import net.causw.app.main.domain.community.ceremony.api.v2.mapper.CeremonyDtoMapper;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.service.CeremonyService;
import net.causw.app.main.domain.community.ceremony.service.dto.request.CeremonyCreateCommand;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonyDetailResult;
import net.causw.app.main.domain.community.ceremony.service.dto.response.CeremonySummaryResult;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import net.causw.app.main.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ceremony Public", description = "경조사 조회 및 생성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CeremonyController {
	private final CeremonyDtoMapper ceremonyDtoMapper;
	private final CeremonyService ceremonyService;

	@PostMapping(value = "/v2/ceremonies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(summary = "사용자 본인의 경조사 생성 (v2)", description = "이미지를 직접 첨부하여 경조사를 생성합니다.")
	public ApiResponse<CeremonyDetailResponse> createCeremonyV2(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestPart(value = "ceremonyCreateRequest") @Valid CeremonyCreateRequest request,
		@RequestPart(value = "imageFileList", required = false) List<MultipartFile> imageFileList) {
		CeremonyCreateCommand command = ceremonyDtoMapper.toCreateCommand(request);
		CeremonyDetailResult result = ceremonyService.createCeremonyV2(userDetails.getUser(), command, imageFileList);
		return ApiResponse.success(ceremonyDtoMapper.toDetailResponse(result));
	}

	@PostMapping("/v3/ceremonies")
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(summary = "사용자 본인의 경조사 생성 (v3)", description = "presigned URL로 업로드한 이미지 UUID를 전달하여 경조사를 생성합니다.")
	public ApiResponse<CeremonyDetailResponse> createCeremony(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid CeremonyCreateRequest request) {
		CeremonyCreateCommand command = ceremonyDtoMapper.toCreateCommand(request);
		CeremonyDetailResult result = ceremonyService.createCeremony(userDetails.getUser(), command);
		return ApiResponse.success(ceremonyDtoMapper.toDetailResponse(result));
	}

	@GetMapping("/v2/ceremonies/{ceremonyId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "경조사 상세 보기 API", description = "경조사 상세 정보를 조회합니다.</br>" +
		"my : 내 경조사 상세 보기</br>" +
		"general : 경조사 상세 보기</br>")
	public ApiResponse<CeremonyDetailResponse> getCeremonyDetail(
		@PathVariable("ceremonyId") String ceremonyId,
		@RequestParam(name = "context", required = false, defaultValue = "general") String contextParam,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		CeremonyContext context = CeremonyContext.fromString(contextParam);
		CeremonyDetailResult result = ceremonyService.getCeremony(ceremonyId, context, userDetails.getUser());
		return ApiResponse.success(ceremonyDtoMapper.toDetailResponse(result));
	}

	@GetMapping("/v2/ceremonies/ongoing")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "진행 중인 경조사 리스트 조회", description = "진행 중인 경조사 리스트를 조회합니다.")
	public ApiResponse<PageResponse<CeremonySummaryResponse>> getOngoingCeremonyPage(
		@RequestParam(name = "type", required = false, defaultValue = "all") String typeParam,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {
		Page<CeremonySummaryResult> result = ceremonyService.getOngoingCeremonyPage(typeParam, pageNum);
		Page<CeremonySummaryResponse> response = result.map(ceremonyDtoMapper::toSummaryResponse);
		return ApiResponse.success(PageResponse.from(response));
	}

	@GetMapping("/v2/ceremonies/upcoming")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "곧 다가올 경조사 리스트 조회", description = "곧 다가올 경조사 리스트를 조회합니다.")
	public ApiResponse<PageResponse<CeremonySummaryResponse>> getUpcomingCeremonyPage(
		@RequestParam(name = "type", required = false, defaultValue = "all") String typeParam,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {
		Page<CeremonySummaryResult> result = ceremonyService.getUpcomingCeremonyPage(typeParam, pageNum);
		Page<CeremonySummaryResponse> response = result.map(ceremonyDtoMapper::toSummaryResponse);
		return ApiResponse.success(PageResponse.from(response));
	}

	@GetMapping("/v2/ceremonies/past")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "지난 경조사 리스트 조회", description = "지난 경조사 리스트를 조회합니다.")
	public ApiResponse<PageResponse<CeremonySummaryResponse>> getPastCeremonyPage(
		@RequestParam(name = "type", required = false, defaultValue = "all") String typeParam,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {
		Page<CeremonySummaryResult> result = ceremonyService.getPastCeremonyPage(typeParam, pageNum);
		Page<CeremonySummaryResponse> response = result.map(ceremonyDtoMapper::toSummaryResponse);
		return ApiResponse.success(PageResponse.from(response));
	}

	@GetMapping("/v2/ceremonies/my")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "내 경조사 리스트 조회", description = "내 경조사 리스트를 조회합니다.")
	public ApiResponse<PageResponse<CeremonySummaryResponse>> getMyCeremonyPage(
		@RequestParam(name = "state") String stateParam,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		CeremonyState state = CeremonyState.fromString(stateParam);
		Page<CeremonySummaryResult> result = ceremonyService.getMyCeremonyPage(userDetails.getUserId(), state, pageNum);
		Page<CeremonySummaryResponse> response = result.map(ceremonyDtoMapper::toSummaryResponse);
		return ApiResponse.success(PageResponse.from(response));
	}
}