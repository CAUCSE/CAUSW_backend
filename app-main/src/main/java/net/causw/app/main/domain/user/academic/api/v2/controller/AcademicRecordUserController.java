package net.causw.app.main.domain.user.academic.api.v2.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import net.causw.app.main.domain.user.academic.api.v2.dto.request.EnrollmentApplicationRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.request.GraduationApplicationRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicStatusResponse;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.EnrollmentDetailsResponse;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.GraduationDetailsResponse;
import net.causw.app.main.domain.user.academic.service.AcademicRecordUserService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/users/me/academic-record")
@Tag(name = "유저 학적상태 변경 v2", description = "학적상태를 졸업 또는 재학으로 변경 신청합니다.")
public class AcademicRecordUserController {

	private final AcademicRecordUserService academicRecordUserService;

	@PostMapping("/graduation")
	@Operation(summary = "학적상태변경(재학 -> 졸업) 요청", description = "유저의 학적 상태를 재학에서 졸업으로 변경합니다. 관리자의 승인 없이 학적 상태가 변경됩니다.")
	public ApiResponse<AcademicStatusResponse<GraduationDetailsResponse>> updateStatusToGraduated(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid GraduationApplicationRequest req) {
		return ApiResponse.success(
			academicRecordUserService.updateStatusToGraduated(
				userDetails.getUser(),
				req));
	}

	@PostMapping(path = "/return", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "학적상태변경(졸업 -> 재학) 요청", description = "유저의 학적 상태를 졸업에서 재학으로 변경합니다. 관리자의 승인이 필요합니다.")
	public ApiResponse<AcademicStatusResponse<EnrollmentDetailsResponse>> updateStatusToEnrolled(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestPart(value = "enrollmentApplicationRequest") @Valid EnrollmentApplicationRequest req,
		@RequestPart(value = "imageFileList", required = false) List<MultipartFile> imageFileList) {
		return ApiResponse.success(
			academicRecordUserService.updateStatusToEnrolled(
				userDetails.getUser(),
				req,
				imageFileList));
	}
}
