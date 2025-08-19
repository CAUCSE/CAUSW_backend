package net.causw.app.main.controller;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyContext;
import net.causw.app.main.dto.notification.CeremonyListNotificationDto;
import net.causw.app.main.service.ceremony.CeremonyService;
import net.causw.app.main.dto.ceremony.*;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.domain.model.enums.ceremony.CeremonyState;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ceremony")
public class CeremonyController {
    private final CeremonyService ceremonyService;

    /**
     * 사용자 본인의 경조사 생성
     * @param userDetails
     * @param createCeremonyRequestDTO
     * @param imageFileList
     * @return
     */

    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "사용자 본인의 경조사 생성",
            description = "사용자 본인의 경조사 생성합니다.")
    public CeremonyResponseDto createCeremony(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "createCeremonyRequestDTO") @Valid CreateCeremonyRequestDto createCeremonyRequestDTO,
            @RequestPart(value = "imageFileList", required = false) List<MultipartFile> imageFileList
    ) {
        return ceremonyService.createCeremony(userDetails.getUser(), createCeremonyRequestDTO, imageFileList);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 본인의 경조사 신청 내역 조회",
            description = "사용자 본인의 경조사 신청 내역을 조회합니다.")
    public Page<CeremonyListNotificationDto> getCeremonies(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "ceremonyState", defaultValue = "ACCEPT") CeremonyState state,
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return ceremonyService.getUserCeremonyResponses(userDetails.getUser(), state, pageNum);
    }


    @GetMapping("/list/await")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_LEADER_ALUMNI)")
    @Operation(summary = "전체 경조사 승인 대기 목록 조회(관리자용)",
            description = "전체 경조사 승인 대기 목록을 조회합니다.")
    public Page<CeremonyListNotificationDto> getAllUserAwaitingCeremonyPage(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return ceremonyService.getAllUserAwaitingCeremonyPage(pageNum);
    }

    @GetMapping("/{ceremonyId}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "유저 경조사 정보 상세 보기",
            description = "유저 경조사 정보를 조회합니다. 접근한 페이지에 따라 Request Param을 다르게 해주세요.</br>" +
                    "general : 전체 알림 페이지에서 접근</br>" +
                    "my : 내 경조사 목록에서 접근</br>" +
                    "admin : 관리자용 경조사 관리 페이지에서 접근</br>")
    public CeremonyResponseDto getUserCeremonyInfo(
            @PathVariable("ceremonyId") String ceremonyId,
            @RequestParam(name = "context") String contextParam,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CeremonyContext context = CeremonyContext.fromString(contextParam);
        return ceremonyService.getCeremony(ceremonyId, context, userDetails.getUser());
    }

    @PutMapping("/state")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_LEADER_ALUMNI)")
    @Operation(summary = "유저 경조사 승인 상태 변경(승인/거부)(관리자용)",
            description = "유저 경조사 승인 상태를 변경합니다.")
    public CeremonyResponseDto updateUserCeremonyStatus(
            @RequestBody @Valid UpdateCeremonyStateRequestDto updateCeremonyStateRequestDto
    ) {
        return ceremonyService.updateUserCeremonyStatus(updateCeremonyStateRequestDto);
    }

    @PutMapping("/state/close/{ceremonyId}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "유저 경조사 신청 취소(사용자용)",
            description = "유저가 본인의 경조사 승인 상태를 close로 변경합니다.")
    public CeremonyResponseDto closeUserCeremonyStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "ceremonyId") String ceremonyId
    ) {
        return ceremonyService.closeUserCeremonyStatus(userDetails.getUser(), ceremonyId);
    }

    @PostMapping("/notification-setting")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "유저 경조사 알람 설정 생성",
            description = "유저 경조사 알람 설정을 생성합니다. 학번은 2자리로 입력해주세요. (ex. 19)")
    public CeremonyNotificationSettingResponseDto createCeremonyNotificationSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateCeremonyNotificationSettingDto ceremonyNotificationSettingDTO
    ){
        return ceremonyService.createCeremonyNotificationSettings(userDetails.getUser(), ceremonyNotificationSettingDTO);
    }


    @GetMapping("/notification-setting")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "유저 경조사 알람 설정 조회", description = "유저의 경조사 알람 설정을 조회합니다.")
    public CeremonyNotificationSettingResponseDto getCeremonyNotificationSetting(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ceremonyService.getCeremonyNotificationSetting(userDetails.getUser());
    }

    @PutMapping("/notification-setting")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "유저 경조사 알람 설정 수정", description = "유저의 경조사 알람 설정을 수정합니다. 학번은 2자리로 입력해주세요. (ex. 19)")
    public CeremonyNotificationSettingResponseDto updateCeremonyNotificationSetting(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateCeremonyNotificationSettingDto createCeremonyNotificationSettingDTO
    ) {
        return ceremonyService.updateUserSettings(userDetails.getUser(), createCeremonyNotificationSettingDTO);
    }


}
