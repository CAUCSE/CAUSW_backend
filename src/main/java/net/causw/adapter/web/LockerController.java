package net.causw.adapter.web;

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
@RequestMapping("/api/v1/lockers")
public class LockerController {
    private final LockerService lockerService;

    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerResponseDto findById(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.findById(id, loginUserId);
    }

    @PostMapping(value = "")
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
    public LockerResponseDto delete(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.delete(loginUserId, id);
    }

    @GetMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerLocationsResponseDto findAllLocation() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.findAllLocation(loginUserId);
    }

    @GetMapping(value = "/locations/{locationId}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockersResponseDto findByLocation(
            @PathVariable String locationId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.findByLocation(locationId, loginUserId);
    }

    @PostMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.CREATED)
    public LockerLocationResponseDto createLocation(
            @RequestBody LockerLocationCreateRequestDto lockerLocationCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.lockerService.createLocation(loginUserId, lockerLocationCreateRequestDto);
    }

    @PutMapping(value = "/locations/{locationId}")
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
    @ResponseStatus(value = HttpStatus.OK)
    public void setExpireDate(
            @RequestBody LockerExpiredAtRequestDto lockerExpiredAtRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        this.lockerService.setExpireAt(loginUserId, lockerExpiredAtRequestDto);
    }
}
