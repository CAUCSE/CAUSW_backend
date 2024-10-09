package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.userCouncilFee.*;
import net.causw.application.userCouncilFee.UserCouncilFeeService;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.model.util.MessageUtil;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-council-fee")
public class UserCouncilFeeController {

    private final UserCouncilFeeService userCouncilFeeService;

    @GetMapping("/export/excel")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학생회비 엑셀 다운로드",
        description = "학생회비 엑셀 파일을 다운로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "5000", description = MessageUtil.FAIL_TO_GENERATE_EXCEL_FILE, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void exportUserCouncilFeeToExcel(
            HttpServletResponse response
    ) {
        userCouncilFeeService.exportUserCouncilFeeToExcel(response);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학생회비 납부자 목록 조회",
        description = "학생회비 납부자 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "4000", description = MessageUtil.USER_NOT_FOUND, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public Page<UserCouncilFeeListResponseDto> getUserCouncilFeeList(
            @ParameterObject Pageable pageable
    ) {
        return userCouncilFeeService.getUserCouncilFeeList(pageable);
    }

    @GetMapping("/info/{userCouncilFeeId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학생회비 납부자 상세 조회",
        description = "학생회비 납부자 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserCouncilFeeResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = MessageUtil.USER_COUNCIL_FEE_NOT_FOUND, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public UserCouncilFeeResponseDto getUserCouncilFeeInfo(
            @PathVariable(value = "userCouncilFeeId") String userCouncilFeeId
    ) {
        return userCouncilFeeService.getUserCouncilFeeInfo(userCouncilFeeId);
    }

    @PostMapping("/create-user")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학생회비 납부자 등록(가입 유저 대상)",
        description = "동문네트워크 가입자 대상으로 학생회비 납부자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "학생회비 납부자 등록 완료", content = @Content),
            @ApiResponse(responseCode = "4000", description = MessageUtil.INVALID_USER_COUNCIL_FEE_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = MessageUtil.INVALID_USER_COUNCIL_FEE_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4013", description = MessageUtil.API_NOT_ACCESSIBLE, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void createUserCouncilFeeWithUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateUserCouncilFeeWithUserRequestDto createUserCouncilFeeWithUserRequestDto
    ) {
        userCouncilFeeService.creatUserCouncilFeeWithUser(userDetails.getUser(), createUserCouncilFeeWithUserRequestDto);
    }

    @PostMapping("/create-fake-user")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학생회비 납부자 등록(미가입자 대상)",
        description = "동문네트워크 미 가입자 대상으로 학생회비 납부자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "학생회비 납부자 등록 완료", content = @Content),
            @ApiResponse(responseCode = "4000", description = MessageUtil.INVALID_USER_COUNCIL_FEE_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = MessageUtil.INVALID_USER_COUNCIL_FEE_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4013", description = MessageUtil.API_NOT_ACCESSIBLE, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void createUserCouncilFeeWithFakeUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateUserCouncilFeeWithFakeUserRequestDto createUserCouncilFeeRequestDto
    ) {
        userCouncilFeeService.creatUserCouncilFeeWithFakeUser(userDetails.getUser(), createUserCouncilFeeRequestDto);
    }

    @PutMapping("/update-user")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학생회비 납부자 수정(가입 유저 대상)",
        description = "학생회비 납부자를 수정합니다.(가입 유저 대상)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "학생회비 납부자 수정 완료", content = @Content),
            @ApiResponse(responseCode = "4000", description = MessageUtil.INVALID_USER_COUNCIL_FEE_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = MessageUtil.INVALID_USER_COUNCIL_FEE_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4013", description = MessageUtil.API_NOT_ACCESSIBLE, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void updateUserCouncilFeeWithUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader @Pattern(
                    regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                    message = "id 값은 대시(-)를 포함하고, 32자리의 UUID 형식이어야 합니다."
            )
            @NotBlank(message = "대상 사용자 고유 id 값은 필수 입력 값입니다.") String userCouncilFeeId,
            @RequestBody @Valid CreateUserCouncilFeeWithUserRequestDto createUserCouncilFeeWithUserRequestDto
    ) {
        userCouncilFeeService.updateUserCouncilFeeWithUser(userDetails.getUser(), userCouncilFeeId, createUserCouncilFeeWithUserRequestDto);
    }

    @PutMapping("/update-fake-user")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학생회비 납부자 수정(미가입자 대상)",
        description = "학생회비 납부자를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "학생회비 납부자 수정 완료", content = @Content),
            @ApiResponse(responseCode = "4000", description = MessageUtil.INVALID_USER_COUNCIL_FEE_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = MessageUtil.INVALID_USER_COUNCIL_FEE_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = MessageUtil.INVALID_COUNCIL_FEE_FAKE_USER_INFO, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4013", description = MessageUtil.API_NOT_ACCESSIBLE, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void updateUserCouncilFeeWithFakeUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader @Pattern(
                    regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                    message = "id 값은 대시(-)를 포함하고, 32자리의 UUID 형식이어야 합니다."
            )
            @NotBlank(message = "대상 사용자 고유 id 값은 필수 입력 값입니다.") String userCouncilFeeId,
            @RequestBody @Valid CreateUserCouncilFeeWithFakeUserRequestDto createUserCouncilFeeRequestDto
    ) {
        userCouncilFeeService.updateUserCouncilFeeWithFakeUser(userDetails.getUser(), userCouncilFeeId, createUserCouncilFeeRequestDto);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학생회비 납부자 삭제",
        description = "학생회비 납부자를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "학생회비 납부자 삭제 완료", content = @Content),
            @ApiResponse(responseCode = "4000", description = MessageUtil.USER_COUNCIL_FEE_NOT_FOUND, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void deleteUserCouncilFee(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader String userCouncilFeeId
    ) {
       userCouncilFeeService.deleteUserCouncilFee(userDetails.getUser(), userCouncilFeeId);
    }

    @GetMapping("/getUserIdByStudentId")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "학번으로 사용자 id 조회",
        description = "학번으로 사용자 id를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 ID 조회 완료", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = MessageUtil.USER_NOT_FOUND, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public String getUserIdByStudentId(
            @RequestHeader String studentId
    ) {
        return userCouncilFeeService.getUserIdByStudentId(studentId);
    }

    @GetMapping("/isCurrentSemesterApplied")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "hasAnyRole('ADMIN','PERSIDENT', 'VICE_PRESIDENT')")
    @Operation(summary = "특정 사용자가 현재 학생회비 적용 학기인지 여부 조회",
        description = "특정 사용자가 현재 학생회비 적용 학기인지 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "4000", description = MessageUtil.USER_NOT_FOUND, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public Boolean isCurrentSemesterApplied(
            @RequestHeader String userId
    ) {
        return userCouncilFeeService.isCurrentSemesterApplied(userId);
    }

    @GetMapping("/isCurrentSemesterApplied/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "본인이 현재 학생회비 적용 학기인지 여부 조회",
        description = "본인이 현재 학생회비 적용 학기인지 여부를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "4000", description = MessageUtil.USER_NOT_FOUND, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public Boolean isCurrentSemesterAppliedBySelf(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userCouncilFeeService.isCurrentSemesterAppliedBySelf(userDetails.getUser());
    }

    @GetMapping("/isCurrentSemesterApplied/self/info")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "본인이 현재 학생회비 적용 학기인지 여부 상세 조회",
        description = "본인이 현재 학생회비 적용 학기인지 여부를 상세 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CurrentUserCouncilFeeResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = MessageUtil.USER_NOT_FOUND, content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = MessageUtil.INTERNAL_SERVER_ERROR, content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public CurrentUserCouncilFeeResponseDto isCurrentSemesterAppliedBySelfInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userCouncilFeeService.isCurrentSemesterAppliedBySelfInfo(userDetails.getUser());
    }

}
