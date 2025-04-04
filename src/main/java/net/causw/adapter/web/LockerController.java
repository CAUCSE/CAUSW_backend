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

    @GetMapping(value = "/{lockerId}")
    @Operation(summary = "사물함 조회 Api", description = "사물함 id를 바탕으로 사물함 정보를 가져오는 Api 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public LockerResponseDto findById(
            @PathVariable("lockerId") String lockerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.findById(lockerId, userDetails.getUser());
    }

    @PostMapping(value = "")
    @Operation(summary = "사물함 생성 Api(관리자/회장 전용)", description = "사물함을 생성하는 Api입니다.")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    public LockerResponseDto create(
            @Valid @RequestBody LockerCreateRequestDto lockerCreateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.create(userDetails.getUser(), lockerCreateRequestDto);
    }

    @PutMapping(value = "/{lockerId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "사물함 상태 update Api", description = "사물함 상태를 변경하는 Api입니다.\n" +
            "허용 동작 목록: \"ENABLE(관리자/회장 전용)\", \"DISABLE(관리자/회장 전용)\", \"REGISTER\", \"RETURN\", \"EXTEND\"")
    public LockerResponseDto update(
            @PathVariable("lockerId") String lockerId,
            @Valid @RequestBody LockerUpdateRequestDto lockerUpdateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.update(
                userDetails.getUser(),
                lockerId,
                lockerUpdateRequestDto
        );
    }

    @PutMapping(value = "/{lockerId}/move")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "사물함 위치 이동 Api(관리자/회장 전용)", description = "사물함의 위치(locker location)를 이동(변경)시키는 Api입니다. ex) 1번 사물함에 있어서 1층 1번 -> 2층 1번, 층만 바뀜")
    public LockerResponseDto move(
            @PathVariable("lockerId") String lockerId,
            @Valid @RequestBody LockerMoveRequestDto lockerMoveRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.move(
                userDetails.getUser(),
                lockerId,
                lockerMoveRequestDto
        );
    }

    @DeleteMapping(value = "/{lockerId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "사물함 삭제 Api(관리자/회장 전용)", description = "사물함을 삭제하는 Api입니다.")
    public LockerResponseDto delete(
            @PathVariable("lockerId") String lockerId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.delete(userDetails.getUser(), lockerId);
    }

    @GetMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "사물함 층별 사용가능 여부 조회 Api", description = "사물함 층별 개수 정보와 사용 가능 개수를 제공하는 API입니다.")
    public LockerLocationsResponseDto findAllLocation(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return this.lockerService.findAllLocation(userDetails.getUser());
    }

    @GetMapping(value = "/locations/{locationId}")
    @Operation(summary = "사물함 특정 층별 사용가능 여부 조회 Api", description = "사물함 특정 층별 개수 정보와 사용 가능 개수를 제공하는 API입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    public LockersResponseDto findByLocation(
            @PathVariable("locationId") String locationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.findByLocation(locationId, userDetails.getUser());
    }

    @PostMapping(value = "/locations")
    @Operation(summary = "사물함 위치 생성 API(관리자/회장 전용)", description = "사물함 특정 층 생성 API 입니다.")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    public LockerLocationResponseDto createLocation(
            @Valid @RequestBody LockerLocationCreateRequestDto lockerLocationCreateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.createLocation(userDetails.getUser(), lockerLocationCreateRequestDto);
    }

    @PutMapping(value = "/locations/{locationId}")
    @Operation(summary = "사물함 위치 업데이트 API(관리자/회장 전용)", description = "사물함 특정 층 업데이트 API 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
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
    @Operation(summary = "사물함 위치 삭제 API(관리자/회장 전용)", description = "사물함 특정 층 삭제 API 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    public LockerLocationResponseDto deleteLocation(
            @PathVariable("locationId") String locationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.lockerService.deleteLocation(userDetails.getUser(), locationId);
    }

    @GetMapping(value = "/{lockerId}/log")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사물함 로그 조회 API(관리자/회장 전용)", description = "사물함 로그를 조회하는 API입니다.")
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    public List<LockerLogResponseDto> findLog(
            @PathVariable("lockerId") String lockerId
    ) {
        return this.lockerService.findLog(lockerId);
    }

    @PostMapping(value = "/expire")
    @Operation(summary = "사물함 만료 기한 설정 Api(관리자/회장 전용)", description = "사물함 만료 기한을 설정하는 API입니다.(학생회장만 가능)")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    public void setExpireDate(
            @Valid @RequestBody LockerExpiredAtRequestDto lockerExpiredAtRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        this.lockerService.setExpireAt(userDetails.getUser(), lockerExpiredAtRequestDto);
    }

    @PostMapping(value = "/createAll")
    @Operation(summary = "사물함 전체 생성 API(관리자/회장 전용)" , description = "현재 존재하는 모든 사물함을 생성하는 API입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    public void createAllLockers(@AuthenticationPrincipal CustomUserDetails userDetails){
        this.lockerService.createAllLockers(userDetails.getUser());
    }
}
