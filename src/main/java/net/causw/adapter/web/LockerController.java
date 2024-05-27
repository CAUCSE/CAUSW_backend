package net.causw.adapter.web;

import io.swagger.annotations.ApiOperation;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @ApiOperation(value = "사물함 조회 Api(완료)", notes = "사물함 id를 바탕으로 사물함 정보를 가져오는 Api 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerResponseDto findById(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.findById(id, loginUserId);
    }

    @PostMapping(value = "")
    @ApiOperation(value = "사물함 생성 Api(완료)", notes = "사물함을 생성하는 Api입니다.")
    @ResponseStatus(value = HttpStatus.CREATED)
    public LockerResponseDto create(
            @RequestBody LockerCreateRequestDto lockerCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.create(loginUserId, lockerCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "사물함 상태 update Api", notes = "사물함 상태를 변경하는 Api입니다.")
    public LockerResponseDto update(
            @PathVariable String id,
            @RequestBody LockerUpdateRequestDto lockerUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.update(
                loginUserId,
                id,
                lockerUpdateRequestDto
        );
    }

    @PutMapping(value = "/{id}/move")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "사물함 위치 이동 Api(완료)", notes = "사물함의 위치를 이동시키는 Api입니다.")
    public LockerResponseDto move(
            @PathVariable String id,
            @RequestBody LockerMoveRequestDto lockerMoveRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.move(
                loginUserId,
                id,
                lockerMoveRequestDto
        );
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "사물함 삭제 Api(완료)", notes = "사물함을 삭제하는 Api입니다.")
    public LockerResponseDto delete(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.delete(loginUserId, id);
    }

    @GetMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "사물함 층별 사용가능 여부 조회 Api(완료)", notes = "사물함 층별 개수 정보와 사용 가능 개수를 제공하는 API입니다.")
    public LockerLocationsResponseDto findAllLocation() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.findAllLocation(loginUserId);
    }

    @GetMapping(value = "/locations/{locationId}")
    @ApiOperation(value = "사물함 특정 층별 사용가능 여부 조회 Api(완료)", notes = "사물함 특정 층별 개수 정보와 사용 가능 개수를 제공하는 API입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    public LockersResponseDto findByLocation(
            @PathVariable String locationId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.findByLocation(locationId, loginUserId);
    }

    @PostMapping(value = "/locations")
    @ApiOperation(value = "사물함 위치 생성 API(완료)", notes = "사물함 특정 층 생성 API 입니다.")
    @ResponseStatus(value = HttpStatus.CREATED)
    public LockerLocationResponseDto createLocation(
            @RequestBody LockerLocationCreateRequestDto lockerLocationCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.createLocation(loginUserId, lockerLocationCreateRequestDto);
    }

    @PutMapping(value = "/locations/{locationId}")
    @ApiOperation(value = "사물함 위치 업데이트 API(완료)", notes = "사물함 특정 층 업데이트 API 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerLocationResponseDto updateLocation(
            @PathVariable String locationId,
            @RequestBody LockerLocationUpdateRequestDto lockerLocationUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.updateLocation(
                loginUserId,
                locationId,
                lockerLocationUpdateRequestDto
        );
    }

    @DeleteMapping(value = "/locations/{locationId}")
    @ApiOperation(value = "사물함 위치 삭제 API(완료)", notes = "사물함 특정 층 삭제 API 입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerLocationResponseDto deleteLocation(
            @PathVariable String locationId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.deleteLocation(loginUserId, locationId);
    }

    @GetMapping(value = "/{id}/log")
    @ResponseStatus(value = HttpStatus.OK)
    public List<LockerLogResponseDto> findLog(@PathVariable String id) {
        return this.lockerService.findLog(id);
    }

    @PostMapping(value = "/expire")
    @ApiOperation(value = "사물함 만료 기한 설정 Api(완료)", notes = "사물함 만료 기한을 설정하는 API입니다.(학생회장만 가능)")
    @ResponseStatus(value = HttpStatus.OK)
    public void setExpireDate(
            @RequestBody LockerExpiredAtRequestDto lockerExpiredAtRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        this.lockerService.setExpireAt(loginUserId, lockerExpiredAtRequestDto);
    }

    @PostMapping(value = "/createAll")
    @ApiOperation(value = "사물함 전체 생성 API(관리자)" , notes = "현재 존재하는 모든 사물함을 생성하는 API입니다.")
    @ResponseStatus(value = HttpStatus.OK)
    public void createAllLockers(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        this.lockerService.createAllLockers(loginUserId);
    }
}
