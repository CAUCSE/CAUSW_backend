package net.causw.adapter.web;

import net.causw.application.LockerService;
import net.causw.application.dto.LockerLocationResponseDto;
import net.causw.application.dto.LockerLogDetailDto;
import net.causw.application.dto.LockerResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping(value = "/")
    @ResponseStatus(value = HttpStatus.OK)
    public List<LockerResponseDto> findAll() {
        return this.lockerService.findAll();
    }

    @GetMapping(value = "/location")
    @ResponseStatus(value = HttpStatus.OK)
    public List<LockerLocationResponseDto> findAllLocation() {
        return this.lockerService.findAllLocation();
    }

    @GetMapping(value="/{id}/log")
    @ResponseStatus(value = HttpStatus.OK)
    public List<LockerLogDetailDto> findLog(@PathVariable String id) {
        return this.lockerService.findLog(id);
    }
}
