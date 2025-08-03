package net.causw.app.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.dto.user.*;
import net.causw.app.main.service.user.UserRoleService;
import net.causw.app.main.service.user.UserService;
import net.causw.app.main.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.app.main.dto.circle.CircleResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.UnauthorizedException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRoleService userRoleService;

    /**
     * 사용자 고유 id 값으로 사용자 정보를 조회하는 API
     * @param userId
     * @return
     */
    @GetMapping(value = "/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
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
    public UserResponseDto findByUserId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("userId") String userId
    ) {
        return this.userService.findByUserId(userId, userDetails.getUser());
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
    public UserResponseDto findCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findCurrentUser(userDetails.getUser());
    }

    //FIXME: findMyWrittenPost로 대체(동일 기능), 리팩토링 통합 완료 후 삭제 예정
    @GetMapping(value = "/posts")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "(구)로그인한 사용자의 게시글 조회 API(삭제 예정 -> posts/written으로 변경)")
    public UserPostsResponseDto findPosts(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findPosts(userDetails.getUser(), pageNum);
    }

    @GetMapping(value = "/posts/written")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그인한 사용자가 작성한 게시글 기록 조회 API(완료)",
            description = "로그인한 사용자가 작성한 게시글의 목록을 조회하는 Api 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public UserPostsResponseDto findMyWrittenPosts(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findPosts(userDetails.getUser(), pageNum);
    }

    @GetMapping(value = "/posts/favorite")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그인한 사용자가 누른 즐겨찾기 게시글 기록 조회 API(완료)",
            description = "로그인한 사용자가 즐겨찾기한 게시글의 목록을 조회하는 Api 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public UserPostsResponseDto findMyFavoritePosts(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findFavoritePosts(userDetails.getUser(), pageNum);
    }

    @GetMapping(value = "/posts/like")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그인한 사용자가 좋아요 누른 게시글 기록 조회 API(완료)",
        description = "로그인한 사용자가 좋아요 누른 게시글의 목록을 조회하는 Api 입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserPostsResponseDto.class))),
        @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
        @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public UserPostsResponseDto findMyLikePosts(
        @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findLikePosts(userDetails.getUser(), pageNum);
    }

    @GetMapping(value = "/comments/written")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그인한 사용자가 작성한 댓글들의 게시물 기록 조회 API(완료)",
            description = "로그인한 사용자가 작성한 댓글들의 게시물 기록 조회하는 Api 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public UserPostsResponseDto findMyCommentedPosts(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findCommentedPosts(userDetails.getUser(), pageNum);
    }

    @GetMapping(value = "/comments")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그인한 사용자의 댓글 조회 API(완료)")
    public UserCommentsResponseDto findComments(
            @RequestParam(name = "pageNum",defaultValue = "0") Integer pageNum,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findComments(userDetails.getUser(), pageNum);
    }

    @GetMapping(value = "/name/{name}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.OPERATIONS_TEAM)")
    @Operation(summary = "유저 관리 시 사용자 이름으로 검색 API(완료)")
    public List<UserResponseDto> findByName(
            @PathVariable("name") String name,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findByName(userDetails.getUser(), name);
    }

    @GetMapping(value = "/privileged")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "특별한 권한을 가진 사용자 목록 확인 API(완료)", description = "학생회장, 부학생회장, 학생회, 학년대표, 동문회장 역할을 가지는 사용자를 반환합니다. \n 권한 역임을 할 수 있기 때문에 중복되는 사용자가 존재합니다.(ex. PRESIDENT_N_LEADER_CIRCLE)")
    public UserPrivilegedResponseDto findPrivilegedUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findPrivilegedUsers(userDetails.getUser());
    }

    @GetMapping(value = "/state/{state}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "유저 관리 시 사용자의 상태(ACTIVE, INACTIVE 등) 에 따라 검색하는 API(완료)", description = "유저를 관리할 때 사용자가 활성, 비활성 상태인지에 따라서 분류하여 검색할 수 있습니다. \n state 는 ACTIVE, INACTIVE, AWAIT, REJECT, DROP 으로 검색가능합니다.")
    public Page<UserResponseDto> findByState(
            @PathVariable("state") String state,
            @RequestParam(name = "user name", required = false) String name,
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return this.userService.findByState(userDetails.getUser(), state, name, pageNum);
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
            @ApiResponse(responseCode = "4003", description = "입학년도를 다시 확인해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 존재하는 닉네임입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto signUp(
            @Valid @RequestBody UserCreateRequestDto userCreateDto
    ) {
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
    public DuplicatedCheckResponseDto isDuplicatedEmail(@PathVariable("email") String email) {
        return this.userService.isDuplicatedEmail(email);
    }

    /**
     * 닉네임 중복 확인 컨트롤러
     * @param nickname
     * @return DuplicatedCheckResponseDto
     */
    @GetMapping(value = "/{nickname}/is-duplicated-nickname")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "닉네임 중복 확인 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4001", description = "탈퇴한 계정의 재가입은 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public DuplicatedCheckResponseDto isDuplicatedNickname(@PathVariable("nickname") String nickname) {
        return this.userService.isDuplicatedNickname(nickname);
    }

    /**
     * 학번 중복 확인 컨트롤러
     * @param studentId
     * @return DuplicatedCheckResponseDto
     */
    @GetMapping(value = "/{studentId}/is-duplicated-student-id")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "닉네임 중복 확인 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4001", description = "탈퇴한 계정의 재가입은 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public DuplicatedCheckResponseDto isDuplicatedStudentId(@PathVariable("studentId") String studentId) {
        return this.userService.isDuplicatedStudentId(studentId);
    }

    /**
     * 사용자 정보 업데이트 컨트롤러
     * @param userUpdateDto
     * @return UserResponseDto
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 존재하는 닉네임입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "userUpdateDto") @Valid UserUpdateRequestDto userUpdateDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {

        return this.userService.update(userDetails.getUser(), userUpdateDto, profileImage);
    }

    @PutMapping(value = "/{delegateeId}/delegate-role")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
            summary = "자신의 권한 위임",
            description = """
        로그인된 사용자가 자신의 권한 중 하나를 특정 사용자에게 위임합니다.
        - 위임자는 해당 권한이 회수 됩니다.
        - 고유 권한(ex. 학생회장 등)을 위임할 경우, 기존 모든 사용자로부터 해당 권한이 제거됩니다.
        - 학생회장 권한 위임 시, 학생회 전체 권한(부회장, 학생회 등)이 초기화됩니다.
        """
    )
    public UserResponseDto delegateRole(
            @PathVariable("delegateeId") String delegateeId,
            @Valid @RequestBody UserUpdateRoleRequestDto userUpdateRoleRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userRoleService.delegateRole(userDetails.getUser(), delegateeId, userUpdateRoleRequestDto);
    }

    @PutMapping(value = "/{granteeId}/grant-role")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
            summary = "타인에게 권한 부여",
            description = """
        로그인된 사용자가 타인에게 권한을 부여합니다.
        - 부여일 경우 delegatorId는 생략해야 합니다.
        - delegatorId가 존재하면, 위임의 형태로 간주되어 delegator의 권한이 회수됩니다.
        - 고유 권한(ex. 학생회장 등) 부여 시 기존 모든 사용자로부터 해당 권한이 제거됩니다.
        - 학생회장 권한 부여 시, 학생회 전체 권한(부회장, 학생회 등)이 초기화됩니다.
        """
    )
    public UserResponseDto grantRole(
            @RequestParam(value = "delegatorId", required = false) String delegatorId,
            @PathVariable("granteeId") String granteeId,
            @Valid @RequestBody UserUpdateRoleRequestDto userUpdateRoleRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userRoleService.grantRole(userDetails.getUser(), delegatorId, granteeId, userUpdateRoleRequestDto);
    }


    @PutMapping(value = "/password/find")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "비밀번호 찾기 API (완료)", description = "비밀번호 재설정 이메일 전송 API입니다.")
    public void findPassword(@Valid @RequestBody UserFindPasswordRequestDto userFindPasswordRequestDto) {
        this.userService.findPassword(userFindPasswordRequestDto);
    }

    @PutMapping(value = "/password")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "비밀번호 업데이트 API (완료)")
    public UserResponseDto updatePassword(
            @Valid @RequestBody UserUpdatePasswordRequestDto userUpdatePasswordRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.updatePassword(userDetails.getUser(), userUpdatePasswordRequestDto);
    }

    /**
     * 탈퇴 컨트롤러
     * @param
     * @return
     */
    @DeleteMapping
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.CAN_LEAVE)")
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
    public UserResponseDto leave(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return this.userService.leave(userDetails.getUser());
    }

    /**
     * 유저 삭제 컨트롤러
     * @param
     * @return
     */
    @DeleteMapping(value = "{id}/delete")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "사용자 삭제 API (완료)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "User id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto delete(@PathVariable("id") String id, @AuthenticationPrincipal CustomUserDetails userDetails) {

        return this.userService.eraseUserData(userDetails.getUser(), id);
    }

    @PutMapping(value = "{id}/drop")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "사용자 추방 및 사물함 반환 API (완료)")
    public UserResponseDto drop(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody String dropReason
    ) {
        return this.userService.dropUser(userDetails.getUser(), id, dropReason);
    }
    @GetMapping(value = "/circles")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자가 속한 동아리 목록 불러오기 API(완료)" , description = "관리자, 학생회장인 경우 모든 동아리 불러오기")
    public List<CircleResponseDto> getCircleList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.getCircleList(userDetails.getUser());
    }

    @GetMapping(value = "/admissions/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "가입 대기 사용자 정보 확인 API (완료)")
    public UserAdmissionResponseDto findAdmissionById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findAdmissionById(userDetails.getUser(), id);
    }


    @GetMapping(value = "/admissions")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "모든 가입 대기 사용자 목록 확인 API (완료)")
    public Page<UserAdmissionsResponseDto> findAllAdmissions(
            @RequestParam(name = "pageNum",defaultValue = "0") Integer pageNum,
            @RequestParam(name = "name", required = false) String name,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.findAllAdmissions(userDetails.getUser(), name, pageNum);
    }

    @PostMapping(value = "/admissions/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "userAdmissionCreateRequestDto") @Valid UserAdmissionCreateRequestDto userAdmissionCreateRequestDto,
            @RequestPart(value = "userAdmissionAttachImageList") List<MultipartFile> userAdmissionAttachImageList
    ) {
        return this.userService.createAdmission(userDetails.getUser(), userAdmissionCreateRequestDto, userAdmissionAttachImageList);
    }

    @GetMapping(value = "/admissions/self")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "현재 사용자의 가입 신청 정보 확인 API")
    public UserAdmissionResponseDto getCurrentUserAdmission(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.getCurrentUserAdmission(userDetails.getUser());
    }

    @PutMapping(value = "/admissions/{id}/accept")
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
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
    public UserAdmissionResponseDto acceptAdmission(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.accept(userDetails.getUser(), id);
    }

    /**
     * 신청 거절 API
     * @param id
     * @return
     */
    @PutMapping(value = "/admissions/{id}/reject")
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
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
    public UserAdmissionResponseDto rejectAdmission(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody String rejectReason
    ) {

        return this.userService.reject(userDetails.getUser(), id, rejectReason);
    }

    @PutMapping(value = "/restore/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "사용자 복구 API(완료)", description = "복구할 사용자의 id를 넣어주세요")
    public UserResponseDto restore(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.userService.restore(userDetails.getUser(), id);
    }

    @PutMapping(value = "/token/update")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "토큰 재발급 API(완료)", description = "refreshToken을 넣어주세요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSignInResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserSignInResponseDto updateToken(
            @Valid @RequestBody UserUpdateTokenRequestDto userUpdateTokenRequestDto
    ) {
        return this.userService.updateToken(userUpdateTokenRequestDto.getRefreshToken());
    }

    @PostMapping(value = "/sign-out")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "로그아웃 API" , description = "로그아웃(refreshToken, AccessToken 무효화")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserSignInResponseDto.class)))
    })
    public UserSignOutResponseDto signOut(
            @Valid @RequestBody UserSignOutRequestDto userSignOutRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        return userService.signOut(userDetails.getUser(), userSignOutRequestDto);
    }
    /**
     * @param userFindIdRequestDto
     * @return userFindIdRequestDto
     * */
    @PostMapping(value = "/user-id/find")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "아이디 찾기 API" , description = "아이디를 찾기 위해 학번, 이름, 전화번호 필요")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserFindIdResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "해당 사용자를 찾을 수 없습니다", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserFindIdResponseDto findUserId(
            @Valid @RequestBody UserFindIdRequestDto userFindIdRequestDto
    ) {
        return userService.findUserId(userFindIdRequestDto);
    }

    @GetMapping(value = "/studentId/{studentId}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "학번으로 회원 조회 API", description = "학번을 입력하면 학번이 일치하는 회원 조회, 회원은 활동 중이고 재학 상태여야 함")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "해당 사용자를 찾을 수 없습니다", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public List<UserResponseDto> findByStudentId(
            @PathVariable("studentId") String studentId
    ) {
        return userService.findByStudentId(studentId);
    }

    @GetMapping(value = "/export")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "사용자 정보 엑셀 다운로드 API(완료)", description = "사용자 정보를 엑셀로 다운로드")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", schema = @Schema(implementation = Workbook.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public void exportUserList(HttpServletResponse response) {
        userService.exportUserListToExcel(response);
    }

    @PutMapping(value = "/update/isV2")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 isV2 칼럼 업데이트(v1->v2 DB 마이그레이션 전용)",
            description = "사용자 isV2 칼럼 업데이트(v1->v2 DB 마이그레이션 전용) API입니다. isV2를 true로 업데이트 합니다. 학부 인증과 학적 상태 인증이 모두 끝난 유저만 업데이트가 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserResponseDto updateUserIsV2(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userService.updateUserIsV2(userDetails.getUser());
    }


    @PostMapping(value = "/fcm")
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(summary = "사용자 fcmToken 등록",
            description = "로그인한 사용자의 fcmToken을 등록합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserFcmTokenResponseDto createFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "fcmToken") String fcmToken
    ) {
        return userService.createFcmToken(userDetails.getUser(), fcmToken);

    }

    @GetMapping(value = "/fcm")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "사용자 fcmToken 조회",
            description = "로그인한 사용자의 fcmToken을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public UserFcmTokenResponseDto getFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userService.getUserFcmToken(userDetails.getUser());
    }


}

