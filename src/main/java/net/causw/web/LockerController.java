package net.causw.web;

import net.causw.application.LockerService;
import net.causw.application.dto.LockerDetailDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/locker")
public class LockerController {
    private final LockerService lockerService;

    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerDetailDto findByLockerNumber(@PathVariable Long lockerNumber) {
        return this.lockerService.findByLockerNumber(lockerNumber);
    }
}
