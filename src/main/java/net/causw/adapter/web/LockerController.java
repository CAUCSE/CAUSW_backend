package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.locker.LockerService;
import net.causw.application.dto.locker.LockerExpiredAtRequestDto;
import net.causw.application.dto.locker.LockerLocationsResponseDto;
import net.causw.application.dto.locker.LockersResponseDto;
import net.causw.application.dto.locker.LockerCreateRequestDto;
import net.causw.application.dto.locker.LockerLocationCreateRequestDto;
import net.causw.application.dto.locker.LockerLocationResponseDto;
import net.causw.application.dto.locker.LockerLocationUpdateRequestDto;
import net.causw.application.dto.locker.LockerLogResponseDto;
import net.causw.application.dto.locker.LockerMoveRequestDto;
import net.causw.application.dto.locker.LockerResponseDto;
import net.causw.application.dto.locker.LockerUpdateRequestDto;
import net.causw.config.security.userdetails.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lockers")
public class LockerController {
    private final LockerService lockerService;

    @GetMapping(value = "/{id}")
    @Operation(summary = "사물함 조회 Api(완료)", description = "사물함 id를 바탕으로 사물함 정보를 가져오는 Api 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and @securityService.isAcademicRecordCertified()")
    public LockerResponseDto findById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.findById(id, userDetails.getUser());
    }

    @PostMapping(value = "")
    @Operation(summary = "사물함 생성 Api(완료)", description = "사물함을 생성하는 Api입니다.")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    public LockerResponseDto create(
            @Valid @RequestBody LockerCreateRequestDto lockerCreateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.create(userDetails.getUser(), lockerCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and @securityService.isAcademicRecordCertified()")
    @Operation(summary = "사물함 상태 update Api", description = "사물함 상태를 변경하는 Api입니다.")
    public LockerResponseDto update(
            @PathVariable("id") String id,
            @Valid @RequestBody LockerUpdateRequestDto lockerUpdateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.update(
                userDetails.getUser(),
                id,
                lockerUpdateRequestDto
        );
    }

    @PutMapping(value = "/{id}/move")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    @Operation(summary = "사물함 위치 이동 Api(완료)", description = "사물함의 위치를 이동시키는 Api입니다.")
    public LockerResponseDto move(
            @PathVariable("id") String id,
            @Valid @RequestBody LockerMoveRequestDto lockerMoveRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.move(
                userDetails.getUser(),
                id,
                lockerMoveRequestDto
        );
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    @Operation(summary = "사물함 삭제 Api(완료)", description = "사물함을 삭제하는 Api입니다.")
    public LockerResponseDto delete(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.delete(userDetails.getUser(), id);
    }

    @GetMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    @Operation(summary = "사물함 층별 사용가능 여부 조회 Api(완료)", description = "사물함 층별 개수 정보와 사용 가능 개수를 제공하는 API입니다.")
    public LockerLocationsResponseDto findAllLocation(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return this.lockerService.findAllLocation(userDetails.getUser());
    }

    @GetMapping(value = "/locations/{locationId}")
    @Operation(summary = "사물함 특정 층별 사용가능 여부 조회 Api(완료)", description = "사물함 특정 층별 개수 정보와 사용 가능 개수를 제공하는 API입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and @securityService.isAcademicRecordCertified()")
    public LockersResponseDto findByLocation(
            @PathVariable("locationId") String locationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.findByLocation(locationId, userDetails.getUser());
    }

    @PostMapping(value = "/locations")
    @Operation(summary = "사물함 위치 생성 API(완료)", description = "사물함 특정 층 생성 API 입니다.")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    public LockerLocationResponseDto createLocation(
            @Valid @RequestBody LockerLocationCreateRequestDto lockerLocationCreateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.createLocation(userDetails.getUser(), lockerLocationCreateRequestDto);
    }

    @PutMapping(value = "/locations/{locationId}")
    @Operation(summary = "사물함 위치 업데이트 API(완료)", description = "사물함 특정 층 업데이트 API 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    public LockerLocationResponseDto updateLocation(
            @PathVariable("locationId") String locationId,
            @Valid @RequestBody LockerLocationUpdateRequestDto lockerLocationUpdateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.updateLocation(
                userDetails.getUser(),
                locationId,
                lockerLocationUpdateRequestDto
        );
    }

    @DeleteMapping(value = "/locations/{locationId}")
    @Operation(summary = "사물함 위치 삭제 API(완료)", description = "사물함 특정 층 삭제 API 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    public LockerLocationResponseDto deleteLocation(
            @PathVariable("locationId") String locationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.deleteLocation(userDetails.getUser(), locationId);
    }

    @GetMapping(value = "/{id}/log")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    public List<LockerLogResponseDto> findLog(@PathVariable String id) {
        return this.lockerService.findLog(id);
    }

    @PostMapping(value = "/expire")
    @Operation(summary = "사물함 만료 기한 설정 Api(완료)", description = "사물함 만료 기한을 설정하는 API입니다.(학생회장만 가능)")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    public void setExpireDate(
            @Valid @RequestBody LockerExpiredAtRequestDto lockerExpiredAtRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        this.lockerService.setExpireAt(userDetails.getUser(), lockerExpiredAtRequestDto);
    }

    @PostMapping(value = "/createAll")
    @Operation(summary = "사물함 전체 생성 API(관리자)" , description = "현재 존재하는 모든 사물함을 생성하는 API입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUser() and " +
            "@securityService.isAdminOrPresidentOrVicePresident() and " +
            "@securityService.isAcademicRecordCertified()")
    public void createAllLockers(@AuthenticationPrincipal CustomUserDetails userDetails){
        this.lockerService.createAllLockers(userDetails.getUser());
    }
}
