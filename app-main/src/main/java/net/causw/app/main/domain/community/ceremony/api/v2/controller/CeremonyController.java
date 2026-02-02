package net.causw.app.main.domain.community.ceremony.api.v2.controller;

import java.util.List;

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

import net.causw.app.main.domain.community.ceremony.api.v2.dto.CeremonyDetailResponseDto;
import net.causw.app.main.domain.community.ceremony.api.v2.dto.CreateCeremonyRequestDto;
import net.causw.app.main.domain.community.ceremony.enums.CeremonyContext;
import net.causw.app.main.domain.community.ceremony.service.CeremonyService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
}
