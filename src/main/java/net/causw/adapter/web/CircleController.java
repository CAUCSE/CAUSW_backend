package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Parameter;
import net.causw.application.circle.CircleService;
import net.causw.application.dto.circle.CirclesResponseDto;
import net.causw.application.dto.circle.CircleCreateRequestDto;
import net.causw.application.dto.circle.CircleMemberResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.circle.CircleUpdateRequestDto;
import net.causw.application.dto.circle.CircleBoardsResponseDto;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.domain.model.enums.CircleMemberStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


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

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public List<CirclesResponseDto> findAll() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((String) principal);

        return this.circleService.findAll(userId);
    }

    @GetMapping("/{id}/boards")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleBoardsResponseDto findBoards(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((String) principal);

        return this.circleService.findBoards(userId, id);
    }

    @GetMapping(value = "/{id}/num-member")
    @ResponseStatus(value = HttpStatus.OK)
    public Long getNumMember(@PathVariable String id) {
        return this.circleService.getNumMember(id);
    }

    @GetMapping(value = "/{id}/users")
    @ResponseStatus(value = HttpStatus.OK)
    public List<CircleMemberResponseDto> getUserList(
            @PathVariable String id,
            @RequestParam CircleMemberStatus status
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.getUserList(
                currentUserId,
                id,
                status
        );
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CircleResponseDto create(
            @RequestBody CircleCreateRequestDto circleCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((String) principal);

        return this.circleService.create(userId, circleCreateRequestDto);
    }

    @PutMapping(value = "/{circleId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleResponseDto update(
            @PathVariable String circleId,
            @RequestBody CircleUpdateRequestDto circleUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((String) principal);

        return this.circleService.update(userId, circleId, circleUpdateRequestDto);
    }

    @GetMapping(value = "/{circleId}/applications")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CircleMemberResponseDto userApply(
            @PathVariable String circleId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((String) principal);

        return this.circleService.userApply(userId, circleId);
    }

    @GetMapping(value = "/{name}/is-duplicated")
    @ResponseStatus(value = HttpStatus.OK)
    public DuplicatedCheckResponseDto isDuplicatedName(@PathVariable String name) {
        return this.circleService.isDuplicatedName(name);
    }

    @PutMapping(value = "/{circleId}/users/leave")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleMemberResponseDto leaveUser(
            @PathVariable String circleId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId = ((String) principal);

        return this.circleService.leaveUser(userId, circleId);
    }

    @PutMapping(value = "/{circleId}/users/{userId}/drop")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleMemberResponseDto dropUser(
            @PathVariable String userId,
            @PathVariable String circleId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String requestUserId = ((String) principal);

        return this.circleService.dropUser(
                requestUserId,
                userId,
                circleId
        );
    }

    @PutMapping(value = "/applications/{applicationId}/accept")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleMemberResponseDto acceptUser(
            @PathVariable String applicationId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String requestUserId = ((String) principal);

        return this.circleService.acceptUser(requestUserId, applicationId);
    }

    @PutMapping(value = "/applications/{applicationId}/reject")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleMemberResponseDto rejectUser(
            @PathVariable String applicationId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String requestUserId = ((String) principal);

        return this.circleService.rejectUser(requestUserId, applicationId);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public CircleResponseDto delete(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String requestUserId = ((String) principal);

        return this.circleService.delete(requestUserId, id);
    }
}

