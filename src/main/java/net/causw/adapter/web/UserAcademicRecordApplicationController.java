package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.userAcademicRecordApplication.*;
import net.causw.application.userAcademicRecord.UserAcademicRecordApplicationService;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/academic-record")
@RequiredArgsConstructor
public class UserAcademicRecordApplicationController {

    private final UserAcademicRecordApplicationService userAcademicRecordApplicationService;

    /**
     * 전체 유저의 학적 정보 목록 조회
     * @param pageable
     * @return Page<UserAcademicRecordListResponseDto>
     */
    @GetMapping("/list/active-users")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and hasAnyRole('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "전체 유저의 학적 정보 목록 조회",
            description = "전체 유저의 학적 정보 목록을 조회합니다.")
    public Page<UserAcademicRecordListResponseDto> getAllUserAcademicRecordPage(
            @ParameterObject Pageable pageable
    ) {
        return userAcademicRecordApplicationService.getAllUserAcademicRecordPage(pageable);
    }

    /**
     * 전체 학적 인증 승인 대기 목록 조회
     * @param pageable
     * @return Page<UserAcademicRecordListResponseDto>
     */
    @GetMapping("/list/await")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and hasAnyRole('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "전체 학적 인증 승인 대기 목록 조회",
            description = "전체 학적 인증 승인 대기 목록 조회합니다.")
    public Page<UserAcademicRecordListResponseDto> getAllUserAwaitingAcademicRecordPage(
            @ParameterObject Pageable pageable
    ) {
        return userAcademicRecordApplicationService.getAllUserAwaitingAcademicRecordPage(pageable);
    }

    /**
     * 재학 인증 일괄 요청
     * @return Void
     */
    @PutMapping("/request-all")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and hasAnyRole('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "재학 인증 일괄 요청",
            description = "전체 유저의 재학 인증을 요청합니다.")
    public Void requestAllUserAcademicRecordApplication() {
        return null;
    }

    /**
     * 유저 학적 정보 상세 보기
     * @param userId
     * @return UserAcademicRecordInfoResponseDto
     */
    @GetMapping("/record/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and hasAnyRole('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 정보 상세 보기",
            description = "유저 학적 정보 상세를 조회합니다.")
    public UserAcademicRecordInfoResponseDto getUserAcademicRecordInfo(
            @PathVariable("userId") String userId
    ) {
        return userAcademicRecordApplicationService.getUserAcademicRecordInfo(userId);
    }

    /**
     * 유저 학적 정보 노트 변경
     * @param userId
     * @param note
     * @return Void
     */
    @PutMapping("/record/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and hasAnyRole('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT')")
    public Void updateUserAcademicRecordNote(
            @PathVariable("userId") String userId,
            @RequestBody String note
    ) {
        return userAcademicRecordApplicationService.updateUserAcademicRecordNote(userId, note);
    }

    /**
     * 유저 학적 승인 요청 상세 보기
     * @param userId
     * @param applicationId
     * @return UserAcademicRecordApplicationInfoResponseDto
     */
    @GetMapping("/application/{userId}/{applicationId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and hasAnyRole('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 승인 요청 상세 보기",
            description = "유저 학적 승인 요청 상세를 조회합니다.")
    public UserAcademicRecordApplicationInfoResponseDto getUserAcademicRecordApplicationInfo(
            @PathVariable("userId") String userId,
            @PathVariable("applicationId") String applicationId
    ) {
        return userAcademicRecordApplicationService.getUserAcademicRecordApplicationInfo(userId, applicationId);
    }

    /**
     * 유저 학적 정보 상태 변경
     * @param userDetails
     * @param updateUserAcademicStatusRequestDto
     * @return Void
     */
    @PutMapping("/update")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and hasAnyRole('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 정보 상태 변경",
            description = "유저 학적 정보 상태를 변경합니다.")
    public Void updateUserAcademicStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateUserAcademicStatusRequestDto updateUserAcademicStatusRequestDto
    ) {
        return userAcademicRecordApplicationService.updateUserAcademicStatus(userDetails.getUser(), updateUserAcademicStatusRequestDto);
    }

    /**
     * 유저 학적 인증 승인 상태 변경(승인/거부)
     * @param userDetails
     * @param updateUserAcademicRecordApplicationRequestDto
     * @return UpdateUserAcademicRecordApplicationRequestDto
     */
    @PutMapping("/application/admin")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and hasAnyRole('ADMIN', 'PRESIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 인증 승인 상태 변경(승인/거부)",
            description = "유저 학적 인증 승인 상태를 변경합니다.")
    public Void updateUserAcademicRecordApplicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateUserAcademicRecordApplicationRequestDto updateUserAcademicRecordApplicationRequestDto
    ) {
        return userAcademicRecordApplicationService.updateUserAcademicRecordApplicationStatus(userDetails.getUser(), updateUserAcademicRecordApplicationRequestDto);
    }

    /**
     *
     * @return
     */
    @PostMapping("/application/create")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "사용자 본인의 학적 증빙 서류 제출",
            description = "사용자 본인의 학적 증비 서류를 제출합니다.")
    public Void createUserAcademicRecordApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto
    ) {

    }

    /**
     *
     * @return
     */
    @PutMapping("/application/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "사용자 본인의 학적 증빙 서류 수정",
            description = "사용자 본인의 학적 증빙 서류를 수정합니다.")
    public Void updateUserAcademicRecordApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto
    ) {

    }

}
