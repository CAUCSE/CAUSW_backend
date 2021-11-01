package net.causw.adapter.web;

import net.causw.application.LockerService;
import net.causw.application.dto.LockerCreateRequestDto;
import net.causw.application.dto.LockerLocationCreateRequestDto;
import net.causw.application.dto.LockerLocationResponseDto;
import net.causw.application.dto.LockerLocationUpdateRequestDto;
import net.causw.application.dto.LockerLogDetailDto;
import net.causw.application.dto.LockerResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public LockerResponseDto findById(@PathVariable String id) {
        return this.lockerService.findById(id);
    }

    @PostMapping(value = "/")
    @ResponseStatus(value = HttpStatus.CREATED)
    public LockerResponseDto create(
            @AuthenticationPrincipal String creatorId,
            @RequestBody LockerCreateRequestDto locker
    ) {
        return this.lockerService.create(creatorId, locker);
    }
    
    @GetMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.OK)
    public List<LockerLocationResponseDto> findAllLocation() {
        return this.lockerService.findAllLocation();
    }

    @GetMapping(value = "/locations/{locationId}")
    @ResponseStatus(value = HttpStatus.OK)
    public List<LockerResponseDto> findByLocation(@PathVariable String locationId) {
        return this.lockerService.findByLocation(locationId);
    }

    @PostMapping(value = "/locations")
    @ResponseStatus(value = HttpStatus.CREATED)
    public LockerLocationResponseDto createLocation(
            @AuthenticationPrincipal String creatorId,
            @RequestBody LockerLocationCreateRequestDto lockerLocation
    ) {
        return this.lockerService.createLocation(creatorId, lockerLocation);
    }

    @PutMapping(value = "/locations/{locationId}")
    public LockerLocationResponseDto updateLocation(
            @AuthenticationPrincipal String updaterId,
            @PathVariable String locationId,
            @RequestBody LockerLocationUpdateRequestDto lockerLocation
    ) {
        return this.lockerService.updateLocation(updaterId, locationId, lockerLocation);
    }


    @GetMapping(value="/{id}/log")
    @ResponseStatus(value = HttpStatus.OK)
    public List<LockerLogDetailDto> findLog(@PathVariable String id) {
        return this.lockerService.findLog(id);
    }
}
