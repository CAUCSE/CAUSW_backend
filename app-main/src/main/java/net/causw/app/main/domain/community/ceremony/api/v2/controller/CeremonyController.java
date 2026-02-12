package net.causw.app.main.domain.community.ceremony.api.v2.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.community.ceremony.api.v2.dto.request.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonyDetailResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.response.CeremonySummaryResponseDto;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyState;
import net.causw.app.main.domain.community.ceremony.service.CeremonyService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ceremony API V2", description = "경조사 API V2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/ceremonies")
public class CeremonyController {
	private final CeremonyService ceremonyService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.CREATED)
	@Operation(summary = "사용자 본인의 경조사 생성", description = "사용자 본인의 경조사 생성합니다.")
	public ApiResponse<CeremonyDetailResponseDto> createCeremony(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestPart(value = "createCeremonyRequestDTO") @Valid CreateCeremonyRequestDto createCeremonyRequestDTO,
		@RequestPart(value = "imageFileList", required = false) List<MultipartFile> imageFileList) {
		return ApiResponse
			.success(ceremonyService.createCeremony(userDetails.getUser(), createCeremonyRequestDTO, imageFileList));
	}

	@GetMapping("/{ceremonyId}")
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "경조사 상세 보기 API", description = "경조사 상세 정보를 조회합니다.</br>" +
		"my : 내 경조사 상세 보기</br>" +
		"general : 경조사 상세 보기</br>")
	public ApiResponse<CeremonyDetailResponseDto> getCeremonyDetail(
		@PathVariable("ceremonyId") String ceremonyId,
		@RequestParam(name = "context") String contextParam,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		CeremonyContext context = CeremonyContext.fromString(contextParam);
		return ApiResponse.success(ceremonyService.getCeremony(ceremonyId, context, userDetails.getUser()));
	}

	@GetMapping("/ongoing")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "진행 중인 경조사 리스트 조회", description = "진행 중인 경조사 리스트를 조회합니다.")
	public ApiResponse<Page<CeremonySummaryResponseDto>> getOngoingCeremonyPage(
		@RequestParam(name = "type", required = false, defaultValue = "all") String typeParam,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {
		return ApiResponse.success(ceremonyService.getOngoingCeremonyPage(typeParam, pageNum));
	}

	@GetMapping("/upcoming")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "곧 다가올 경조사 리스트 조회", description = "곧 다가올 경조사 리스트를 조회합니다.")
	public ApiResponse<Page<CeremonySummaryResponseDto>> getUpcomingCeremonyPage(
		@RequestParam(name = "type", required = false, defaultValue = "all") String typeParam,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {
		return ApiResponse.success(ceremonyService.getUpcomingCeremonyPage(typeParam, pageNum));
	}

	@GetMapping("/past")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "지난 경조사 리스트 조회", description = "지난 경조사 리스트를 조회합니다.")
	public ApiResponse<Page<CeremonySummaryResponseDto>> getPastCeremonyPage(
		@RequestParam(name = "type", required = false, defaultValue = "all") String typeParam,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum) {
		return ApiResponse.success(ceremonyService.getPastCeremonyPage(typeParam, pageNum));
	}

	@GetMapping("/my")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "내 경조사 리스트 조회", description = "내 경조사 리스트를 조회합니다.")
	public ApiResponse<Page<CeremonySummaryResponseDto>> getMyCeremonyPage(
		@RequestParam(name = "state") String stateParam,
		@RequestParam(name = "pageNum", required = false, defaultValue = "0") Integer pageNum,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		CeremonyState state = CeremonyState.fromString(stateParam);
		return ApiResponse.success(ceremonyService.getMyCeremonyPage(userDetails.getUserId(), state, pageNum));
	}
}
