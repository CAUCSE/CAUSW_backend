package net.causw.web;

import net.causw.application.LockerService;
import net.causw.application.dto.LockerDetailDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locker")
public class LockerController {
    private final LockerService lockerService;

    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @GetMapping(value = "/{locker_number}")
    @ResponseStatus(value = HttpStatus.OK)
    public LockerDetailDto findByLockerNumber(@PathVariable Long lockerNumber) {
        return this.lockerService.findByLockerNumber(lockerNumber);
    }
}
