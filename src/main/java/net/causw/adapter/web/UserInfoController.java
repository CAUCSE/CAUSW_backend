package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.user.UserUpdateRequestDto;
import net.causw.application.dto.userInfo.UserInfoResponseDto;
import net.causw.application.dto.userInfo.UserInfoUpdateRequestDto;
import net.causw.application.dto.userInfo.UsersInfoResponseDto;
import net.causw.application.uesrInfo.UserInfoService;
import net.causw.application.user.UserService;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/user-info")
@RequiredArgsConstructor
public class UserInfoController {
    private final UserService userService;
    private final UserInfoService userInfoService;

    /**
     * 사용자 고유 id 값으로 사용자 세부정보를 조회하는 API
     * @param userId
     * @return
     */
    @GetMapping(value = "/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "사용자 세부정보 조회 API",
            description = "UserId로 사용자 세부정보 (사용자 소개)를 조회합니다.")
    public UserInfoResponseDto getByUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("userId") String userId
    ) {
        return userInfoService.getUserInfo(userId);
    }

    @GetMapping(value = "")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "전체 사용자 목록 (기본 정렬: 최근 수정순)")
    public List<UsersInfoResponseDto> getUsersInfo(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return userInfoService.getUsersInfo(pageNum);
    }

    @GetMapping(value = "/me")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "자신의 세부정보 조회 API",
        description = "자신의 세부정보 (사용자 소개)를 조회합니다.")
    public UserInfoResponseDto getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("userId") String userId
    ) {
        return userInfoService.getUserInfo(userDetails.getUser().getId());
    }

    @PutMapping(value = "/me")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "자신의 정보를 수정합니다.")
    public UserInfoResponseDto updateMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestPart(value = "userInfoUpdateDto") @Valid UserInfoUpdateRequestDto userInfoUpdateDto,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return userInfoService.update(userDetails.getUser().getId(), userInfoUpdateDto, profileImage);
    }

    @GetMapping(value = "/search")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "특정 조건의 사용자 세부정보 (사용자 소개)를 조회합니다.")
    public UsersInfoResponseDto getUsersInfoBySearch(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return null;
    }



}

