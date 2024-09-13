package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.semester.CurrentSemesterResponseDto;
import net.causw.application.dto.userAcademicRecordApplication.*;
import net.causw.application.userAcademicRecord.UserAcademicRecordApplicationService;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.model.enums.AcademicStatus;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/academic-record")
@RequiredArgsConstructor
public class UserAcademicRecordApplicationController {

    private final UserAcademicRecordApplicationService userAcademicRecordApplicationService;

    @GetMapping("/semester/current")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "현재 학기 조회",
            description = "현재 학기를 조회합니다.")
    public CurrentSemesterResponseDto getCurrentSemesterYearAndType() {
        return userAcademicRecordApplicationService.getCurrentSemesterYearAndType();
    }

    /**
     * 전체 유저의 학적 정보 목록 조회
     * @param pageable
     * @return Page<UserAcademicRecordListResponseDto>
     */
    @GetMapping("/list/active-users")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "전체 유저의 학적 정보 목록 조회(관리자용)",
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
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "전체 학적 인증 승인 대기 목록 조회(관리자용)",
            description = "전체 학적 인증 승인 대기 목록 조회합니다.")
    public Page<UserAcademicRecordApplicationListResponseDto> getAllUserAwaitingAcademicRecordPage(
            @ParameterObject Pageable pageable
    ) {
        return userAcademicRecordApplicationService.getAllUserAwaitingAcademicRecordPage(pageable);
    }

    /**
     * 유저 학적 정보 상세 보기
     * @param userId
     * @return UserAcademicRecordInfoResponseDto
     */
    @GetMapping("/record/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 정보 상세 보기(관리자용)",
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
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 정보 노트 변경(관리자용)",
            description = "유저 학적 정보 노트를 변경합니다.")
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
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 승인 요청 상세 보기(관리자용)",
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
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 정보 상태 변경(관리자용)",
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
     * @param updateUserAcademicRecordApplicationStateRequestDto
     * @return UpdateUserAcademicRecordApplicationRequestDto
     */
    @PutMapping("/application/admin")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "유저 학적 인증 승인 상태 변경(승인/거부)(관리자용)",
            description = "유저 학적 인증 승인 상태를 변경합니다.")
    public Void updateUserAcademicRecordApplicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateUserAcademicRecordApplicationStateRequestDto updateUserAcademicRecordApplicationStateRequestDto
    ) {
        return userAcademicRecordApplicationService.updateUserAcademicRecordApplicationStatus(userDetails.getUser(), updateUserAcademicRecordApplicationStateRequestDto);
    }

    /**
     * 사용자 본인의 학적 증빙 상태 조회
     * @param userDetails
     */
    @GetMapping("current")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "사용자 본인의 학적 증빙 상태 조회",
            description = "사용자 본인의 학적 증빙 상태를 조회합니다.")
    public AcademicStatus getCurrentUserAcademicRecord(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userAcademicRecordApplicationService.getCurrentUserAcademicRecord(userDetails.getUser());
    }

    /**
     * 사용자 본인의 학적 증빙 제출 서류 조회(승인 대기/거절 중인 것만)
     * @param userDetails
     */
    @GetMapping("current/not-accepted")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "사용자 본인의 학적 증빙 제출 서류 조회(승인 대기/거절 중인 것만)",
            description = "사용자 본인의 대기/거절 중인 학적 증빙 제출 서류를 조회합니다.")
    public CurrentUserAcademicRecordApplicationResponseDto getCurrentUserAcademicRecordApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userAcademicRecordApplicationService.getCurrentUserAcademicRecordApplication(userDetails.getUser());
    }

    /**
     * 사용자 본인의 학적 증빙 서류 제출
     * @param userDetails
     * @param createUserAcademicRecordApplicationRequestDto
     * @param imageFileList
     * @return
     */
    @PostMapping(value = "/application/create")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "사용자 본인의 학적 증빙 서류 제출",
            description = "사용자 본인의 학적 증빙 서류를 제출합니다.")
    public Void createUserAcademicRecordApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "createUserAcademicRecordApplicationRequestDto") @Valid CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto,
            @RequestPart(value = "imageFileList", required = false) List<MultipartFile> imageFileList
    ) {
        return userAcademicRecordApplicationService.createUserAcademicRecordApplication(userDetails.getUser(), createUserAcademicRecordApplicationRequestDto, imageFileList);
    }

    /**
     * 사용자 본인의 학적 증빙 서류 수정
     * @param userDetails
     * @param createUserAcademicRecordApplicationRequestDto
     * @param imageFileList
     * @return
     */
    @PutMapping(value = "/application/update")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "사용자 본인의 학적 증빙 서류 수정",
            description = "사용자 본인의 학적 증빙 서류를 수정합니다.")
    public Void updateUserAcademicRecordApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "createUserAcademicRecordApplicationRequestDto") @Valid CreateUserAcademicRecordApplicationRequestDto createUserAcademicRecordApplicationRequestDto,
            @RequestPart(value = "imageFileList", required = false) List<MultipartFile> imageFileList
    ) {
        return userAcademicRecordApplicationService.updateUserAcademicRecordApplication(userDetails.getUser(), createUserAcademicRecordApplicationRequestDto, imageFileList);
    }

}
