package net.causw.app.main.domain.user.academic.api.v2.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.user.academic.api.v2.dto.request.AcademicReturnApplicationListRequest;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicReturnApplicationDetailResponse;
import net.causw.app.main.domain.user.academic.api.v2.dto.response.AcademicReturnApplicationSummaryResponse;
import net.causw.app.main.domain.user.academic.api.v2.mapper.AcademicReturnApplicationMapper;
import net.causw.app.main.domain.user.academic.service.AcademicRecordAdminService;
import net.causw.app.main.domain.user.auth.userdetails.CustomUserDetails;
import net.causw.app.main.shared.dto.ApiResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/admin/academic-records")
@PreAuthorize("@security.hasRole(@Role.ADMIN)")
@Tag(name = "관리자 학적상태 변경신청 관리 api", description = "관리자 학적상태 변경신청 관리 API")
public class AcademicRecordAdminController {

    private final AcademicRecordAdminService academicRecordAdminService;
    private final AcademicReturnApplicationMapper applicationMapper;

    /**
     * 학적 변경 신청 목록 조회 (검색/필터링)
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<AcademicReturnApplicationSummaryResponse>> getApplications(
            @ParameterObject AcademicReturnApplicationListRequest request
    ) {
        return ApiResponse.success(
                academicRecordAdminService.getApplications(applicationMapper.toCondition(request))
                        .map(applicationMapper::toResponse)
        );
    }


    /**
     * 학적 변경 신청 상세 조회
     */
    @GetMapping("/{recordId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AcademicReturnApplicationDetailResponse> getApplicationDetail(
            @PathVariable Long recordId
    ) {
        return ApiResponse.success(
                academicRecordAdminService.getApplicationDetail(recordId)
        );
    }

    /**
     * 학적 변경 신청 승인
     */
    @PostMapping("/{recordId}/approve")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> approveApplication(
            @AuthenticationPrincipal CustomUserDetails adminDetails,
            @PathVariable Long recordId
    ) {
        academicRecordAdminService.approve(
                adminDetails.getUser(),
                recordId
        );
        return ApiResponse.success();
    }

    /**
     * 학적 변경 신청 반려
     */
    @PostMapping("/{recordId}/reject")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> rejectApplication(
            @AuthenticationPrincipal CustomUserDetails adminDetails,
            @PathVariable Long recordId,
            @RequestBody @Valid AcademicRecordRejectRequestDto requestDto
    ) {
        academicRecordAdminService.reject(
                adminDetails.getUser(),
                recordId,
                requestDto.reason()
        );
        return ApiResponse.success();
    }
}
