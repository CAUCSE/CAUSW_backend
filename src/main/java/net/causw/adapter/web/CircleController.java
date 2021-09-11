package net.causw.adapter.web;

import net.causw.application.CircleService;
import net.causw.application.dto.CircleCreateRequestDto;
import net.causw.application.dto.CircleMemberDto;
import net.causw.application.dto.CircleResponseDto;
import net.causw.application.dto.DuplicatedCheckDto;
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


@RestController
@RequestMapping("/api/v1/circles")
public class CircleController {
    private final CircleService circleService;

    public CircleController(CircleService circleService) {
        this.circleService = circleService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleResponseDto findById(@PathVariable String id) {
        return this.circleService.findById(id);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CircleResponseDto create(@AuthenticationPrincipal String userId, @RequestBody CircleCreateRequestDto circleCreateRequestDto) {
        return this.circleService.create(userId, circleCreateRequestDto);
    }

    @GetMapping(value = "/{circleId}/applications")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CircleMemberDto userApply(@AuthenticationPrincipal String userId, @PathVariable String circleId) {
        return this.circleService.userApply(userId, circleId);
    }

    @GetMapping(value = "/{name}/is-duplicated")
    @ResponseStatus(value = HttpStatus.OK)
    public DuplicatedCheckDto isDuplicatedName(@PathVariable String name) {
        return this.circleService.isDuplicatedName(name);
    }

    @PutMapping(value = "/{circleId}/users/leave")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleMemberDto leaveUser(@AuthenticationPrincipal String userId, @PathVariable String circleId) {
        return this.circleService.leaveUser(userId, circleId);
    }

    @PutMapping(value = "/{circleId}/users/{userId}/drop")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleMemberDto dropUser(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String userId,
            @PathVariable String circleId
    ) {
        return this.circleService.dropUser(
                requestUserId,
                userId,
                circleId
        );
    }

    @PutMapping(value = "/applications/{applicationId}/accept")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleMemberDto acceptUser(@AuthenticationPrincipal String requestUserId, @PathVariable String applicationId) {
        return this.circleService.acceptUser(requestUserId, applicationId);
    }

    @PutMapping(value = "/applications/{applicationId}/reject")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleMemberDto rejectUser(@AuthenticationPrincipal String requestUserId, @PathVariable String applicationId) {
        return this.circleService.rejectUser(requestUserId, applicationId);
    }
}
