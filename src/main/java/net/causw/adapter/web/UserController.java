package net.causw.adapter.web;

import io.swagger.annotations.*;
import net.causw.application.user.UserService;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.user.UserAdmissionCreateRequestDto;
import net.causw.application.dto.user.UserAdmissionResponseDto;
import net.causw.application.dto.user.UserAdmissionsResponseDto;
import net.causw.application.dto.user.UserCommentsResponseDto;
import net.causw.application.dto.user.UserCreateRequestDto;
import net.causw.application.dto.user.UserFindEmailRequestDto;
import net.causw.application.dto.user.UserPostsResponseDto;
import net.causw.application.dto.user.UserPrivilegedResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.application.dto.user.UserSignInRequestDto;
import net.causw.application.dto.user.UserUpdatePasswordRequestDto;
import net.causw.application.dto.user.UserUpdateRequestDto;
import net.causw.application.dto.user.UserUpdateRoleRequestDto;
import net.causw.domain.exceptions.*;
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
@Api(tags = "User 컨트롤러")
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

    /**
     * 회원가입 컨트롤러
     * @param userCreateDto
     * @return UserResponseDto
     */
    @PostMapping(value = "/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation(value = "회원가입 API (완료)", notes = "회원가입 후에는 신청서를 작성해야 합니다.\n신청서 작성 후 승인이 이뤄지면 로그인이 가능합니다.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = UserResponseDto.class),
            @ApiResponse(code = 4001, message = "중복된 이메일입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4003, message = "비밀번호 형식이 잘못되었습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4003, message = "입학년도를 다시 확인해주세요.", response = BadRequestException.class)
    })
    public UserResponseDto signUp(@RequestBody UserCreateRequestDto userCreateDto) {
        return this.userService.signUp(userCreateDto);
    }

    /**
     * 로그인 컨트롤러
     * @param userSignInRequestDto
     * @return token 값
     */
    @PostMapping(value = "/sign-in")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "로그인 API (완료)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4101, message = "잘못된 이메일 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4101, message = "비밀번호를 잘못 입력했습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4011, message = "신청서를 작성하지 않았습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class)
    })
    public String signIn(@RequestBody UserSignInRequestDto userSignInRequestDto) {
        return this.userService.signIn(userSignInRequestDto);
    }

    /**
     * 이메일 중복 확인 컨트롤러
     * @param email
     * @return DuplicatedCheckResponseDto
     */
    @GetMapping(value = "/{email}/is-duplicated")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "이메일 중복 확인 API (완료)")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 4001, message = "탈퇴한 계정의 재가입은 관리자에게 문의해주세요.", response = BadRequestException.class)
    })
    public DuplicatedCheckResponseDto isDuplicatedEmail(@PathVariable String email) {
        return this.userService.isDuplicatedEmail(email);
    }

    /**
     * 사용자 정보 업데이트 컨트롤러
     * @param id
     * @param userUpdateDto
     * @return UserResponseDto
     */
    @PutMapping
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "사용자 정보 업데이트 API (완료)", notes = "id 에는 user 의 고유 id 값을 넣어주세요.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 사용중인 이메일입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4003, message = "입학년도를 다시 확인해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "User id checked, but exception occurred", response = BadRequestException.class)
    })
    public UserResponseDto update(
            @AuthenticationPrincipal String id,
            @RequestBody UserUpdateRequestDto userUpdateDto
    ) {
        return this.userService.update(id, userUpdateDto);
    }

    /**
     * 권한 업데이트 컨트롤러
     * @param grantorId
     * @param granteeId
     * @param userUpdateRoleRequestDto
     * @return
     */
    @PutMapping(value = "/{granteeId}/role")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "역할 업데이트 API", notes = "grantorId 에는 관리자의 고유 id값, granteeId 에는 권한이 업데이트될 사용자의 고유 id 값을 넣어주세요")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "권한을 받을 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4106, message = "권한을 부여할 수 없습니다. - 부여하는 사용자 권한 : ADMIN, 부여할 권한 : PRESIDENT, 부여받는 사용자 권한 : COMMON", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "위임할 수 있는 권한이 아닙니다.", response = BadRequestException.class),
            @ApiResponse(code = 4002, message = "소모임장을 위임할 소모임 입력이 필요합니다.", response = BadRequestException.class),
            @ApiResponse(code = 4108, message = "사용자가 가입 신청한 소모임이 아닙니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "동문회장이 존재하지 않습니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "User id checked, but exception occurred", response = BadRequestException.class)
    })
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

    /**
     * 탈퇴 컨트롤러
     * @param id
     * @return
     */
    @DeleteMapping
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "사용자 탈퇴 API", notes = "id 에는 user 의 고유 id 값을 넣어주세요.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "User id checked, but exception occurred", response = BadRequestException.class)
    })
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
    @ApiOperation(value = "승인 신청서 작성 API (미완료 / 사용 가능)", notes = "attachImage는 우선 무시하고 진행해주세요.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
        @ApiResponse(code = 4001, message = "이미 신청한 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4107, message = "이미 등록된 사용자 입니다.", response = BadRequestException.class)
    })
    public UserAdmissionResponseDto createAdmission(
            @ModelAttribute UserAdmissionCreateRequestDto userAdmissionCreateRequestDto
    ) {
        return this.userService.createAdmission(userAdmissionCreateRequestDto);
    }

    @PutMapping(value = "/admissions/{id}/accept")
    @ApiOperation(value = "신청 승인 API (미완료 / 사용 가능)", notes = "id 에는 승인 고유 id 값을 넣어주세요.\nrequestUserId는 token 값입니다. 추후 수정 및 삭제 예정입니다.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
        @ApiResponse(code = 4000, message = "사용자의 가입 신청을 찾을 수 없습니다.", response = BadRequestException.class),
        @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
        @ApiResponse(code = 5000, message = "User id checked, but exception occurred", response = BadRequestException.class)
    })
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
    @ApiOperation(value = "신청 거절 API (미완료 / 사용 가능)", notes = "id 에는 승인 고유 id 값을 넣어주세요.\nrequestUserId는 token 값입니다. 추후 수정 및 삭제 예정입니다.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "OK", response = String.class),
        @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
        @ApiResponse(code = 4000, message = "사용자의 가입 신청을 찾을 수 없습니다.", response = BadRequestException.class),
        @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
        @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
        @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = BadRequestException.class),
        @ApiResponse(code = 5000, message = "User id checked, but exception occurred", response = BadRequestException.class)
    })
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
