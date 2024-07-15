package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.user.*;
import net.causw.application.user.UserService;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 사용자 고유 id 값으로 사용자 정보를 조회하는 API
     * @param userId
     * @return
     */
    @GetMapping(value = "/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 정보 조회 API (완료)",
            description = "userId에는 사용자 고유 id 값을 입력해주세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "해당 사용자를 찾을 수 없습니다", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "소모임장이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "해당 유저는 소모임 회원이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto findByUserId(@PathVariable String userId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findByUserId(userId, loginUserId);
    }

    /**
     * 현재 로그인한 사용자 정보를 조회하는 API
     * @return
     */
    @GetMapping(value = "/me")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그인한 사용자 정보 조회 API (완료)",
            description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "소모임장이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto findCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findCurrentUser(loginUserId);
    }

    @GetMapping(value = "/posts")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그인한 사용자의 게시글 조회 API(완료)")
    public UserPostsResponseDto findPosts(@RequestParam(defaultValue = "0") Integer pageNum) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findPosts(loginUserId, pageNum);
    }

    @GetMapping(value = "/comments")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그인한 사용자의 댓글 조회 API(완료)")
    public UserCommentsResponseDto findComments(@RequestParam(defaultValue = "0") Integer pageNum) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findComments(loginUserId, pageNum);
    }

    @GetMapping(value = "/name/{name}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "유저 관리 시 사용자 이름으로 검색 API(완료)")
    public List<UserResponseDto> findByName(@PathVariable String name) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findByName(loginUserId, name);
    }

    @GetMapping(value = "/privileged")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "특별한 권한을 가진 사용자 목록 확인 API(완료)", description = "학생회장, 부학생회장, 학생회, 학년대표, 동문회장 역할을 가지는 사용자를 반환합니다. \n 권한 역임을 할 수 있기 때문에 중복되는 사용자가 존재합니다.(ex. PRESIDENT_N_LEADER_CIRCLE)")
    public UserPrivilegedResponseDto findPrivilegedUsers() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findPrivilegedUsers(loginUserId);
    }

    @GetMapping(value = "/state/{state}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "유저 관리 시 사용자의 상태(ACTIVE, INACTIVE 등) 에 따라 검색하는 API(완료)", description = "유저를 관리할 때 사용자가 활성, 비활성 상태인지에 따라서 분류하여 검색할 수 있습니다. \n state 는 ACTIVE, INACTIVE, AWAIT, REJECT, DROP 으로 검색가능합니다.")
    public Page<UserResponseDto> findByState(
            @PathVariable String state,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") Integer pageNum) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findByState(loginUserId, state, name, pageNum);
    }

    /**
     * 회원가입 컨트롤러
     * @param userCreateDto
     * @return UserResponseDto
     */
    @PostMapping(value = "/sign-up")
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "회원가입 API (완료)", description = "회원가입 후에는 신청서를 작성해야 합니다.\n신청서 작성 후 승인이 이뤄지면 로그인이 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "4001", description = "중복된 이메일입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = "비밀번호 형식이 잘못되었습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = "입학년도를 다시 확인해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
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
    @Operation(summary = "로그인 API (완료)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSignInResponseDto.class))),
            @ApiResponse(responseCode = "4101", description = "잘못된 이메일 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4101", description = "비밀번호를 잘못 입력했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4011", description = "신청서를 작성하지 않았습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserSignInResponseDto signIn(@RequestBody UserSignInRequestDto userSignInRequestDto) {
        return this.userService.signIn(userSignInRequestDto);
    }

    /**
     * 이메일 중복 확인 컨트롤러
     * @param email
     * @return DuplicatedCheckResponseDto
     */
    @GetMapping(value = "/{email}/is-duplicated")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "이메일 중복 확인 API (완료)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4001", description = "탈퇴한 계정의 재가입은 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public DuplicatedCheckResponseDto isDuplicatedEmail(@PathVariable String email) {
        return this.userService.isDuplicatedEmail(email);
    }

    /**
     * 사용자 정보 업데이트 컨트롤러
     * @param userUpdateDto
     * @return UserResponseDto
     */
    @PutMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 정보 업데이트 API (완료)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 사용중인 이메일입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4003", description = "입학년도를 다시 확인해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto update(@RequestBody UserUpdateRequestDto userUpdateDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.update(loginUserId, userUpdateDto);
    }

    /**
     * 권한 업데이트 컨트롤러
     * @param granteeId
     * @param userUpdateRoleRequestDto
     * @return
     */
    @PutMapping(value = "/{granteeId}/role")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "역할 업데이트 API(완료)", description = "grantorId 에는 관리자의 고유 id값, granteeId 에는 권한이 업데이트될 사용자의 고유 id 값을 넣어주세요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "권한을 받을 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4106", description = "권한을 부여할 수 없습니다. - 부여하는 사용자 권한 : ADMIN, 부여할 권한 : PRESIDENT, 부여받는 사용자 권한 : COMMON", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "위임할 수 있는 권한이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4002", description = "소모임장을 위임할 소모임 입력이 필요합니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "사용자가 가입 신청한 소모임이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "동문회장이 존재하지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5001", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto updateRole(@PathVariable String granteeId, @RequestBody UserUpdateRoleRequestDto userUpdateRoleRequestDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.updateUserRole(loginUserId, granteeId, userUpdateRoleRequestDto);
    }

    /**
     * 비밀번호 찾기 API
     * @param email
     * @param name
     * @param studentId
     * @return
     */
    @PutMapping(value = "/password/find")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "비밀번호 찾기 API (완료)", description = "비밀번호 재설정 이메일 전송 API입니다.")
    public UserResponseDto findPassword(@RequestBody UserFindPasswordRequestDto userFindPasswordRequestDto) {
        return this.userService.findPassword(userFindPasswordRequestDto);
    }

    @PutMapping(value = "/password")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "비밀번호 업데이트 API (완료)")
    public UserResponseDto updatePassword(@RequestBody UserUpdatePasswordRequestDto userUpdatePasswordRequestDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.updatePassword(loginUserId, userUpdatePasswordRequestDto);
    }

    /**
     * 탈퇴 컨트롤러
     * @param
     * @return
     */
    @DeleteMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 탈퇴 API (완료)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto leave() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.leave(loginUserId);
    }

    @PutMapping(value = "{id}/drop")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 추방 및 사물함 반환 API (완료)")
    public UserResponseDto drop(@PathVariable String id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.dropUser(loginUserId, id);
    }
    @GetMapping(value = "/circles")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자가 속한 동아리 목록 불러오기 API(완료)" , description = "관리자, 학생회장인 경우 모든 동아리 불러오기")
    public List<CircleResponseDto> getCircleList() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.getCircleList(loginUserId);
    }

    @GetMapping(value = "/admissions/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "가입 대기 사용자 정보 확인 API (완료)")
    public UserAdmissionResponseDto findAdmissionById(@PathVariable String id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findAdmissionById(loginUserId, id);
    }


    @GetMapping(value = "/admissions")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "모든 가입 대기 사용자 목록 확인 API (완료)")
    public Page<UserAdmissionsResponseDto> findAllAdmissions(
            @RequestParam(defaultValue = "0") Integer pageNum,
            @RequestParam(required = false) String name
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.findAllAdmissions(loginUserId, name, pageNum);
    }

    @PostMapping(value = "/admissions/apply")
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "승인 신청서 작성 API (완료)", description = "가입 신청 api입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "회원가입된 사용자의 이메일이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 신청한 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "이미 등록된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserAdmissionResponseDto createAdmission(
            @ModelAttribute UserAdmissionCreateRequestDto userAdmissionCreateRequestDto
    ) {
        return this.userService.createAdmission(userAdmissionCreateRequestDto);
    }

    @PutMapping(value = "/admissions/{id}/accept")
    @Operation(summary = "신청 승인 API (완료)", description = "id 에는 승인 고유 id 값(admission id)을 넣어주세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "사용자의 가입 신청을 찾을 수 없습니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class)))
    })
    public UserAdmissionResponseDto acceptAdmission(@PathVariable String id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.accept(loginUserId, id);
    }

    /**
     * 신청 거절 API
     * @param id
     * @return
     */
    @PutMapping(value = "/admissions/{id}/reject")
    @Operation(summary = "신청 거절 API (완료)", description = "id 에는 승인 고유 id 값(admission id)을 넣어주세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "사용자의 가입 신청을 찾을 수 없습니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class)))
    })
    public UserAdmissionResponseDto rejectAdmission(@PathVariable String id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.reject(loginUserId, id);
    }

//    @PostMapping(value = "/favorite-boards/{boardId}")
//    @ResponseStatus(value = HttpStatus.CREATED)
//    @ApiOperation(value = "즐겨찾는 게시판 생성 API(완료)", notes = "즐겨찾는 게시판을 생성할 수 있습니다.")
//    public BoardResponseDto createFavoriteBoard(@PathVariable String boardId) {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String loginUserId = ((String) principal);
//        return this.userService.createFavoriteBoard(loginUserId, boardId);
//    }

    @PutMapping(value = "/restore/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 복구 API(완료)", description = "복구할 사용자의 id를 넣어주세요")
    public UserResponseDto restore(@PathVariable String id) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.userService.restore(loginUserId, id);
    }

    @PutMapping(value = "/token/update")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "토큰 재발급 API(완료)", description = "refreshToken을 넣어주세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSignInResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserSignInResponseDto updateToken(@RequestBody UserUpdateTokenRequestDto userUpdateTokenRequestDto) {
        return this.userService.updateToken(userUpdateTokenRequestDto.getRefreshToken());
    }

    @PostMapping(value = "/sign-out")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그아웃 API" , description = "로그아웃(refreshToken, AccessToken 무효화")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserSignInResponseDto.class)))
    })
    public UserSignOutResponseDto signOut(@RequestBody UserSignOutRequestDto userSignOutRequestDto){
        return userService.signOut(userSignOutRequestDto);
    }
}
