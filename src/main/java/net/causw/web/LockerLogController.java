package net.causw.web;

import net.causw.application.LockerLogService;
import net.causw.application.dto.LockerLogDetailDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locker/log")
public class LockerLogController {
    private final LockerLogService lockerLogService;

    public LockerLogController(LockerLogService lockerLogService) {
        this.lockerLogService = lockerLogService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    LockerLogDetailDto findById(@PathVariable String id) {
        return this.lockerLogService.findById(id);
    }
}
