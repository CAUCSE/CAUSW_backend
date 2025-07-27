package net.causw.app.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.dto.userInfo.UserInfoResponseDto;
import net.causw.app.main.dto.userInfo.UserInfoUpdateRequestDto;
import net.causw.app.main.dto.userInfo.UserInfoSummaryResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.userInfo.UserInfoService;
import net.causw.global.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users-info")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserInfoService userInfoService;

    /**
     * 사용자 고유 id 값으로 사용자 세부정보를 조회하는 API
     * @param userId
     * @return
     */
    @GetMapping(value = "/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 세부정보 조회 API")
    public UserInfoResponseDto getByUserId(
            @PathVariable("userId") String userId
    ) {
        return userInfoService.getByUserId(userId);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "전체 사용자 세부정보 조회 API", description = "최근 수정된 순서대로 정렬")
    public Page<UserInfoSummaryResponseDto> getAll(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return userInfoService.getAllOrderByUpdatedAtDesc(pageNum);
    }

    @GetMapping(value = "/me")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "자신의 세부정보 조회 API")
    public UserInfoResponseDto getCurrentUser(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userInfoService.getByUserId(userDetails.getUser().getId());
    }

    @PutMapping(value = "/me")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "자신의 세부정보 갱신 API")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "4000", description = "이메일 형식에 맞지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4001", description = "전화번호 형식에 맞지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4002", description = "한 줄 소개가 80자를 초과합니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4003", description = "사진의 용량이 10MB를 초과합니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4100", description = "종료일은 시작일 이후여야 합니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4101", description = "날짜 형식에 맞지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4201", description = "github 주소 형식에 맞지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4202", description = "linkedIn 주소 형식에 맞지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4203", description = "velog 주소 형식에 맞지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4204", description = "instagram 주소 형식에 맞지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })

    public UserInfoResponseDto updateCurrentUser(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestPart(value = "userInfoUpdateDto") @Valid UserInfoUpdateRequestDto userInfoUpdateDto,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return userInfoService.update(userDetails.getUser(), userInfoUpdateDto, profileImage);
    }

    @GetMapping(value = "/search")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 세부정보 조건 검색 API")
    public Page<UserInfoSummaryResponseDto> search(
        @RequestParam(name = "name", defaultValue = "") String name,
        @RequestParam(name = "job", defaultValue = "") String job,
        @RequestParam(name = "career", defaultValue = "") String career,
        @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return userInfoService.search(name, job, career, pageNum);
    }
}