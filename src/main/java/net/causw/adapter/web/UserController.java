package net.causw.adapter.web;

import net.causw.application.UserAuthService;
import net.causw.application.UserService;
import net.causw.application.dto.EmailDuplicatedCheckDto;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserResponseDto;
import net.causw.application.dto.UserSignInRequestDto;
import net.causw.application.dto.UserUpdateRequestDto;
import net.causw.application.dto.UserUpdateRoleRequestDto;
import net.causw.config.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserAuthService userAuthService;

    public UserController(UserService userService, UserAuthService userAuthService) {
        this.userService = userService;
        this.userAuthService = userAuthService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto findById(@PathVariable String id) {
        return this.userService.findById(id);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto findByName(@RequestParam String name) {
        return this.userService.findByName(name);
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
    public EmailDuplicatedCheckDto isDuplicatedEmail(@PathVariable String email) {
        return this.userService.isDuplicatedEmail(email);
    }

    /* TODO : Refactoring & Implementation
    @GetMapping(value = "/auth/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserAuthDto findAuthById(@PathVariable String id) {
        return this.userAuthService.findById(id);
    }
    */

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto update(@PathVariable String id, @RequestBody UserUpdateRequestDto userUpdateDto) {
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
}
