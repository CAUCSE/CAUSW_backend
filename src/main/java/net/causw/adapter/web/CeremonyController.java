package net.causw.adapter.web;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.ceremony.CeremonyService;
import net.causw.application.dto.ceremony.CeremonyResponseDTO;
import net.causw.application.dto.ceremony.CreateCeremonyRequestDTO;
import net.causw.application.dto.ceremony.UpdateCeremonyStateRequestDto;
import net.causw.application.dto.userAcademicRecordApplication.CurrentUserAcademicRecordApplicationResponseDto;
import net.causw.config.security.userdetails.CustomUserDetails;
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
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "사용자 본인의 경조사 생성",
            description = "사용자 본인의 경조사 생성합니다.")
    public CeremonyResponseDTO createUserAcademicRecordApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "createCeremonyRequestDTO") @Valid CreateCeremonyRequestDTO createCeremonyRequestDTO,
            @RequestPart(value = "imageFileList", required = false) List<MultipartFile> imageFileList
    ) {
        return ceremonyService.createCeremony(userDetails.getUser(), createCeremonyRequestDTO, imageFileList);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "사용자 본인의 경조사 신청 내역 조회",
            description = "사용자 본인의 경조사 신청 내역을 조회합니다.")
    public List<CeremonyResponseDTO> getUserCeremonyResponsesDTO(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ceremonyService.getUserCeremonyResponsesDTO(userDetails.getUser());
    }


    @GetMapping("/list/await")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "전체 경조사 승인 대기 목록 조회(관리자용)",
            description = "전체 경조사 승인 대기 목록을 조회합니다.")
    public Page<CeremonyResponseDTO> getAllUserAwaitingCeremonyPage(
            @ParameterObject Pageable pageable
    ) {
        return ceremonyService.getAllUserAwaitingCeremonyPage(pageable);
    }

    @GetMapping("/{ceremonyId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser()")
    @Operation(summary = "유저 경조사 정보 상세 보기",
            description = "유저 경조사 정보를 조회합니다.")
    public CeremonyResponseDTO getUserCeremonyInfo(
            @PathVariable("ceremonyId") String ceremonyId
    ) {
        return ceremonyService.getCeremony(ceremonyId);
    }

    @PutMapping("/state")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "유저 경조사 승인 상태 변경(승인/거부)(관리자용)",
            description = "유저 경조사 승인 상태를 변경합니다.")
    public CeremonyResponseDTO updateUserCeremonyStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateCeremonyStateRequestDto updateCeremonyStateRequestDto
    ) {
        return ceremonyService.updateUserCeremonyStatus(userDetails.getUser(), updateCeremonyStateRequestDto);
    }

}
