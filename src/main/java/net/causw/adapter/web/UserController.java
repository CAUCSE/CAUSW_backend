package net.causw.adapter.web;

import net.causw.application.UserAuthService;
import net.causw.application.UserService;
import net.causw.application.dto.EmailDuplicatedCheckDto;
import net.causw.application.dto.UserAuthDto;
import net.causw.application.dto.UserCreateRequestDto;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserSignInRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public UserDetailDto findById(@PathVariable String id) {
        return this.userService.findById(id);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public UserDetailDto findByName(@RequestParam String name) {
        return this.userService.findByName(name);
    }

    @PostMapping(value = "/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserDetailDto signUp(@RequestBody UserCreateRequestDto userCreateDto) {
        return this.userService.signUp(userCreateDto);
    }

    @PostMapping(value = "/sign-in")
    @ResponseStatus(value = HttpStatus.OK)
    public UserDetailDto signIn(@RequestBody UserSignInRequestDto userSignInRequestDto) {
        return this.userService.signIn(userSignInRequestDto);
    }

    @GetMapping(value = "/{email}/is-duplicated")
    @ResponseStatus(value = HttpStatus.OK)
    public EmailDuplicatedCheckDto isDuplicatedEmail(@PathVariable String email) {
        return this.userService.isDuplicatedEmail(email);
    }

    @GetMapping(value = "/auth/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserAuthDto findAuthById(@PathVariable String id) {
        return this.userAuthService.findById(id);
    }
}
