package net.causw.web;

import net.causw.application.UserService;
import net.causw.application.dto.UserDetailDto;
import net.causw.application.dto.UserSaveRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserDetailDto findById(@PathVariable String id) {
        return this.userService.findById(id);
    }

    @PostMapping(value="/create")
    @ResponseStatus(value = HttpStatus.OK)
    public String save(@RequestBody UserSaveRequestDto requestDto) { return this.userService.save(requestDto); }
}
