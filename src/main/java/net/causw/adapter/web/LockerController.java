package net.causw.adapter.web;

import net.causw.application.LockerService;
import net.causw.application.dto.LockerAllLocationResponseDto;
import net.causw.application.dto.LockerAllResponseDto;
import net.causw.application.dto.LockerCreateRequestDto;
import net.causw.application.dto.LockerLocationCreateRequestDto;
import net.causw.application.dto.LockerLocationResponseDto;
import net.causw.application.dto.LockerLocationUpdateRequestDto;
import net.causw.application.dto.LockerLogDetailDto;
import net.causw.application.dto.LockerMoveRequestDto;
import net.causw.application.dto.LockerResponseDto;
import net.causw.application.dto.LockerUpdateRequestDto;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/lockers")
public class LockerController {
    private final LockerService lockerService;

    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerResponseDto findById(
            @AuthenticationPrincipal String userId,
            @PathVariable String id
    ) {
        return this.lockerService.findById(id, userId);
    }

    @PostMapping(value = "")
    @ResponseStatus(value = HttpStatus.CREATED)
    public LockerResponseDto create(
            @AuthenticationPrincipal String creatorId,
            @RequestBody LockerCreateRequestDto lockerCreateRequestDto
    ) {
        return this.lockerService.create(creatorId, lockerCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerResponseDto update(
            @AuthenticationPrincipal String updaterId,
            @PathVariable String id,
            @RequestBody LockerUpdateRequestDto lockerUpdateRequestDto
    ) {
        return this.lockerService.update(
                updaterId,
                id,
                lockerUpdateRequestDto
        );
    }

    @PutMapping(value = "/{id}/move")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerResponseDto move(
            @AuthenticationPrincipal String updaterId,
            @PathVariable String id,
            @RequestBody LockerMoveRequestDto lockerMoveRequestDto
    ) {
        return this.lockerService.move(
                updaterId,
                id,
                lockerMoveRequestDto
        );
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerResponseDto delete(
            @AuthenticationPrincipal String deleterId,
            @PathVariable String id
    ) {
        return this.lockerService.delete(deleterId, id);
    }

    @GetMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerAllLocationResponseDto findAllLocation(@AuthenticationPrincipal String userId) {
        return this.lockerService.findAllLocation(userId);
    }

    @GetMapping(value = "/locations/{locationId}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerAllResponseDto findByLocation(
            @AuthenticationPrincipal String userId,
            @PathVariable String locationId
    ) {
        return this.lockerService.findByLocation(locationId, userId);
    }

    @PostMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.CREATED)
    public LockerLocationResponseDto createLocation(
            @AuthenticationPrincipal String creatorId,
            @RequestBody LockerLocationCreateRequestDto lockerLocationCreateRequestDto
    ) {
        return this.lockerService.createLocation(creatorId, lockerLocationCreateRequestDto);
    }

    @PutMapping(value = "/locations/{locationId}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerLocationResponseDto updateLocation(
            @AuthenticationPrincipal String updaterId,
            @PathVariable String locationId,
            @RequestBody LockerLocationUpdateRequestDto lockerLocationUpdateRequestDto
    ) {
        return this.lockerService.updateLocation(
                updaterId,
                locationId,
                lockerLocationUpdateRequestDto
        );
    }

    @DeleteMapping(value = "/locations/{locationId}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerLocationResponseDto deleteLocation(
            @AuthenticationPrincipal String deleterId,
            @PathVariable String locationId
    ) {
        return this.lockerService.deleteLocation(deleterId, locationId);
    }

    @GetMapping(value = "/{id}/log")
    @ResponseStatus(value = HttpStatus.OK)
    public List<LockerLogDetailDto> findLog(@PathVariable String id) {
        return this.lockerService.findLog(id);
    }
}
