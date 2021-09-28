package net.causw.adapter.web;

import net.causw.application.UserAuthService;
import net.causw.application.UserService;
import net.causw.application.dto.CircleResponseDto;
import net.causw.application.dto.DuplicatedCheckDto;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserPasswordUpdateRequestDto;
import net.causw.application.dto.UserResponseDto;
import net.causw.application.dto.UserSignInRequestDto;
import net.causw.application.dto.UserUpdateRequestDto;
import net.causw.application.dto.UserUpdateRoleRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserAuthService userAuthService;

    public UserController(UserService userService, UserAuthService userAuthService) {
        this.userService = userService;
        this.userAuthService = userAuthService;
    }

    @GetMapping(value = "/me")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto findCurrentUser(@AuthenticationPrincipal String currentUserId) {
        return this.userService.findById(currentUserId);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto findByName(
            @AuthenticationPrincipal String currentUserId,
            @RequestParam String name
    ) {
        return this.userService.findByName(currentUserId, name);
    }

    @PostMapping(value = "/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserResponseDto signUp(@RequestBody UserCreateRequestDto userCreateDto) {
        return this.userService.signUp(userCreateDto);
    }

    @PostMapping(value = "/sign-in")
    @ResponseStatus(value = HttpStatus.OK)
    public String signIn(@RequestBody UserSignInRequestDto userSignInRequestDto) {
        return this.userService.signIn(userSignInRequestDto);
    }

    @GetMapping(value = "/{email}/is-duplicated")
    @ResponseStatus(value = HttpStatus.OK)
    public DuplicatedCheckDto isDuplicatedEmail(@PathVariable String email) {
        return this.userService.isDuplicatedEmail(email);
    }

    /* TODO : Refactoring & Implementation
    @GetMapping(value = "/auth/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserAuthDto findAuthById(@PathVariable String id) {
        return this.userAuthService.findById(id);
    }
    */

    @PutMapping
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto update(
            @AuthenticationPrincipal String id,
            @RequestBody UserUpdateRequestDto userUpdateDto
    ) {
        return this.userService.update(id, userUpdateDto);
    }

    @PutMapping(value = "/{granteeId}/role")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto updateRole(
            @AuthenticationPrincipal String grantorId,
            @PathVariable String granteeId,
            @RequestBody UserUpdateRoleRequestDto userUpdateRoleRequestDto
    ) {
        return this.userService.updateUserRole(grantorId, granteeId, userUpdateRoleRequestDto);
    }

    @PutMapping(value = "/password")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto updatePassword(
            @AuthenticationPrincipal String id,
            @RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto
    ) {
        return this.userService.updatePassword(id, userPasswordUpdateRequestDto);
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto leave(@AuthenticationPrincipal String id) {
        return this.userService.leave(id);
    }

    @GetMapping(value = "/circles")
    @ResponseStatus(value = HttpStatus.OK)
    public List<CircleResponseDto> getCircleList(@AuthenticationPrincipal String currentUserId) {
        return this.userService.getCircleList(currentUserId);
    }
}
