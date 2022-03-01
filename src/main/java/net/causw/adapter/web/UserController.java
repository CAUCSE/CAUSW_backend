package net.causw.adapter.web;

import net.causw.application.UserService;
import net.causw.application.dto.user.UserFindEmailRequestDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.DuplicatedCheckResponseDto;
import net.causw.application.dto.user.UserAdmissionsResponseDto;
import net.causw.application.dto.user.UserAdmissionCreateRequestDto;
import net.causw.application.dto.user.UserAdmissionResponseDto;
import net.causw.application.dto.user.UserCommentsResponseDto;
import net.causw.application.dto.user.UserCreateRequestDto;
import net.causw.application.dto.user.UserUpdatePasswordRequestDto;
import net.causw.application.dto.user.UserPostsResponseDto;
import net.causw.application.dto.user.UserPrivilegedResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.user.UserSignInRequestDto;
import net.causw.application.dto.user.UserUpdateRequestDto;
import net.causw.application.dto.user.UserUpdateRoleRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/email")
    @ResponseStatus(value = HttpStatus.OK)
    public String findEmail(
            @RequestBody UserFindEmailRequestDto userFindEmailRequestDto
    ) {
        return this.userService.findEmail(userFindEmailRequestDto);
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto findById(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.userService.findById(id, requestUserId);
    }

    @GetMapping(value = "/me")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto findCurrentUser(@AuthenticationPrincipal String currentUserId) {
        return this.userService.findById(currentUserId);
    }

    @GetMapping(value = "/posts")
    @ResponseStatus(value = HttpStatus.OK)
    public UserPostsResponseDto findPosts(
            @AuthenticationPrincipal String requestUserId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        return this.userService.findPosts(requestUserId, pageNum);
    }

    @GetMapping(value = "/comments")
    @ResponseStatus(value = HttpStatus.OK)
    public UserCommentsResponseDto findComments(
            @AuthenticationPrincipal String requestUserId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        return this.userService.findComments(requestUserId, pageNum);
    }

    @GetMapping(value = "/name/{name}")
    @ResponseStatus(value = HttpStatus.OK)
    public List<UserResponseDto> findByName(
            @AuthenticationPrincipal String currentUserId,
            @PathVariable String name
    ) {
        return this.userService.findByName(currentUserId, name);
    }

    @GetMapping(value = "/privileged")
    @ResponseStatus(value = HttpStatus.OK)
    public UserPrivilegedResponseDto findPrivilegedUsers(@AuthenticationPrincipal String currentUserId) {
        return this.userService.findPrivilegedUsers(currentUserId);
    }

    @GetMapping(value = "/state/{state}")
    @ResponseStatus(value = HttpStatus.OK)
    public Page<UserResponseDto> findByState(
            @AuthenticationPrincipal String currentUserId,
            @PathVariable String state,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        return this.userService.findByState(
                currentUserId,
                state,
                pageNum
        );
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
    public DuplicatedCheckResponseDto isDuplicatedEmail(@PathVariable String email) {
        return this.userService.isDuplicatedEmail(email);
    }

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

    @GetMapping(value = "/password")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto findPassword(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String studentId
    ) {
        return this.userService.findPassword(email, name, studentId);
    }

    @PutMapping(value = "/password")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto updatePassword(
            @AuthenticationPrincipal String id,
            @RequestBody UserUpdatePasswordRequestDto userUpdatePasswordRequestDto
    ) {
        return this.userService.updatePassword(id, userUpdatePasswordRequestDto);
    }

    @DeleteMapping
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto leave(@AuthenticationPrincipal String id) {
        return this.userService.leave(id);
    }

    @PutMapping(value = "{id}/drop")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto drop(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.userService.dropUser(requestUserId, id);
    }

    @GetMapping(value = "/circles")
    @ResponseStatus(value = HttpStatus.OK)
    public List<CircleResponseDto> getCircleList(@AuthenticationPrincipal String currentUserId) {
        return this.userService.getCircleList(currentUserId);
    }

    @GetMapping(value = "/admissions/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserAdmissionResponseDto findAdmissionById(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.userService.findAdmissionById(requestUserId, id);
    }

    @GetMapping(value = "/admissions")
    @ResponseStatus(value = HttpStatus.OK)
    public Page<UserAdmissionsResponseDto> findAllAdmissions(
            @AuthenticationPrincipal String requestUserId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        return this.userService.findAllAdmissions(
                requestUserId,
                pageNum
        );
    }

    @PostMapping(value = "/admissions/apply")
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserAdmissionResponseDto createAdmission(
            @ModelAttribute UserAdmissionCreateRequestDto userAdmissionCreateRequestDto
    ) {
        return this.userService.createAdmission(userAdmissionCreateRequestDto);
    }

    @PutMapping(value = "/admissions/{id}/accept")
    public UserAdmissionResponseDto acceptAdmission(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.userService.accept(
                requestUserId,
                id
        );
    }

    @PutMapping(value = "/admissions/{id}/reject")
    public UserAdmissionResponseDto rejectAdmission(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.userService.reject(
                requestUserId,
                id
        );
    }

    @PostMapping(value = "/favorite-boards/{boardId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    public BoardResponseDto createFavoriteBoard(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String boardId
    ) {
        return this.userService.createFavoriteBoard(
                requestUserId,
                boardId
        );
    }

    @PutMapping(value = "/restore/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public UserResponseDto restore(
            @AuthenticationPrincipal String requestUserId,
            @PathVariable String id
    ) {
        return this.userService.restore(
                requestUserId,
                id
        );
    }
}
