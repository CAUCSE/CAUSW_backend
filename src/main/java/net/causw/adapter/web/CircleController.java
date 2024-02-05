package net.causw.adapter.web;

import io.swagger.annotations.*;
import net.causw.application.circle.CircleService;
import net.causw.application.dto.circle.CirclesResponseDto;
import net.causw.application.dto.circle.CircleCreateRequestDto;
import net.causw.application.dto.circle.CircleMemberResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.circle.CircleUpdateRequestDto;
import net.causw.application.dto.circle.CircleBoardsResponseDto;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.application.dto.user.UserResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.model.enums.CircleMemberStatus;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     * 동아리 고유 id 값으로 동아리 정보를 조회하는 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleResponseDto
     */
    @GetMapping(value = "/{circleId}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 정보 조회 API / findById (완료)", notes = "circleId 에는 동아리 고유 id 값(PK)을 입력해주세요.")
    @ApiImplicitParam(name = "circleId",
            value = "동아리 ID",
            required = true,
            dataType = "string",
            paramType = "path",
            defaultValue = "")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleResponseDto.class)
    })
    public CircleResponseDto findById(@PathVariable(name = "circleId") String circleId) {
        return this.circleService.findById(circleId);
    }


    /**
     * 전체 동아리 정보를 조회하는 API
     * @return List<CircleResponseDto>
     */
    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "전체 동아리 정보 조회 API / findAll (완료)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleResponseDto.class, responseContainer = "List")
    })
    public List<CirclesResponseDto> findAll() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.findAll(currentUserId);
    }


    /**
     * 동아리 고유 id 값으로 동아리 소속 게시판을 조회하는 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleBoardsResponseDto
     */
    @GetMapping("/{circleId}/boards")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 소속 게시판 조회 API / findBoards (완료)", notes = "circleId 에는 동아리 고유 id 값(PK)을 입력해주세요.")
    @ApiImplicitParam(name = "circleId",
            value = "동아리 ID",
            required = true,
            dataType = "string",
            paramType = "path",
            defaultValue = "")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleBoardsResponseDto.class)
    })
    public CircleBoardsResponseDto findBoards(@PathVariable(name = "circleId") String circleId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.findBoards(currentUserId, circleId);
    }


    /**
     * 동아리 고유 id 값으로 동아리원 숫자를 조회하는 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return Long
     */
    @GetMapping(value = "/{circleId}/num-member")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리원 숫자 조회 API / getNumMember (완료)", notes = "circleId 에는 동아리 고유 id 값(PK)을 입력해주세요.")
    @ApiImplicitParam(name = "circleId",
            value = "동아리 ID",
            required = true,
            dataType = "string",
            paramType = "path",
            defaultValue = "")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Long.class)
    })
    public Long getNumMember(@PathVariable(name = "circleId") String circleId) {
        return this.circleService.getNumMember(circleId);
    }


    /**
     * 동아리 고유 id 값과 조회하고자하는 동아리원의 상태에 따라 동아리원 목록을 조회하는 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @param circleMemberStatus 동아리원 상태
     * @return Long
     */
    @GetMapping(value = "/{circleId}/users")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리원 상태별 조회 API / getUserList (완료)", notes = "circleId 에는 동아리 고유 id 값(PK), circleMemberStatus 엔 조회하고자 하는 동아리원의 상태를 입력해주세요.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "circleId",
                            value = "동아리 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = ""),
                    @ApiImplicitParam(name = "circleMemberStatus",
                            value = "동아리원 상태",
                            required = true,
                            dataType = "string",
                            paramType = "query",
                            defaultValue = "none",
                            allowableValues = ""
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class, responseContainer = "List")
    })
    public List<CircleMemberResponseDto> getUserList(
            @PathVariable String circleId,
            @RequestParam CircleMemberStatus circleMemberStatus
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.getUserList(
                currentUserId,
                circleId,
                circleMemberStatus
        );
    }


    /**
     * 동아리 생성 API
     * @param  circleCreateRequestDto 동아리 생성 정보
     * @return Long
     */
    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation(value = "동아리 생성 API / create (완료)",
            notes = "생성하고자 하는 동아리의 정보를 입력해주세요.\n동아리장의 권한은 일반 유저만 가능하며, 생성 요청은 관리자(admin), 학생회장(president)만 가능합니다.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = CircleResponseDto.class)
    })
    public CircleResponseDto create(
            @RequestBody @ApiParam(value = "동아리 생성 정보", required = true) CircleCreateRequestDto circleCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.create(currentUserId, circleCreateRequestDto);
    }


    /**
     * 동아리 수정 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @param  circleUpdateRequestDto 동아리 생성 정보
     * @return Long
     */
    @PutMapping(value = "/{circleId}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 수정 API / update (완료)",
            notes = "circleId에는 수정하고자 하는 동아리의 UUID 형식의 ID String 값을 입력해주세요.\n" +
                    "circleUpdateRequestDto에는 수정하고자 하는 동아리의 정보를 입력해주세요.\n" +
                    "동아리장의 권한은 일반 유저만 가능하며, 생성 요청은 관리자(admin), 학생회장(president)만 가능합니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "circleId",
                            value = "동아리 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = "none"),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleResponseDto.class)
    })
    public CircleResponseDto update(
            @PathVariable(name = "circleId") String circleId,
            @RequestBody CircleUpdateRequestDto circleUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.update(currentUserId, circleId, circleUpdateRequestDto);
    }

    /**
     * 사용자 동아리 지원 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @GetMapping(value = "/{circleId}/applications")
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation(value = "사용자 동아리 지원 API",
            notes = "사용자가 동아리에 지원하는 API 입니다.\n" +
                    "현재 로그인한 사용자 기준으로 자동으로 동아리 ID만 입력하면 해당 동아리에 지원됩니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "circleId",
                            value = "동아리 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = "none"),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = CircleMemberResponseDto.class)
    })
    public CircleMemberResponseDto userApply(
            @PathVariable(name = "circleId") String circleId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.userApply(currentUserId, circleId);
    }


    /**
     * 동아리 이름 중복 검사 API
     * @param circleName 동아리 이름
     * @return DuplicatedCheckResponseDto
     */
    @GetMapping(value = "/{circleName}/is-duplicated")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 이름 중복 검사 API",
            notes = "동아리 이름 중복 검사 API 입니다. 이름 기준으로 검사하면 String 형식으로 동아리 이름을 넣어주세요.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "circleName",
                            value = "동아리 이름",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = ""),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = DuplicatedCheckResponseDto.class)
    })
    public DuplicatedCheckResponseDto isDuplicatedName(@PathVariable(name = "circleName") String circleName) {
        return this.circleService.isDuplicatedName(circleName);
    }


    /**
     * 동아리 탈퇴 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @PutMapping(value = "/{circleId}/users/leave")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 탈퇴 API",
            notes = "동아리 탈퇴 API 입니다. 현재 로그인 된 유저 기준입니다.\n" +
                    "탈퇴 시 해당 유저의 동아리 가입 정보 자체가 사라지는 것이 아닌, 동아리 멤버 상태(status)가 LEAVE 로 변경됩니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "circleId",
                            value = "동아리 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = ""),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class)
    })
    public CircleMemberResponseDto leaveUser(
            @PathVariable(name = "circleId") String circleId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.leaveUser(currentUserId, circleId);
    }


    /**
     * 동아리원 제거 API
     * @param userId 유저 고유 ID 값(PK)
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @PutMapping(value = "/{circleId}/users/{userId}/drop")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리원 제거 API",
            notes = "동아리원을 제거하는 API 입니다. userId 에는 제거하려는 유저를, circleId 에는 타깃 동아리를 넣어주세요.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "userId",
                            value = "유저 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = ""),
                    @ApiImplicitParam(name = "circleId",
                            value = "동아리 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = ""
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class)
    })
    public CircleMemberResponseDto dropUser(
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "circleId") String circleId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.dropUser(
                currentUserId,
                userId,
                circleId
        );
    }


    /**
     * 동아리 가입 신청 수락 API
     * @param applicationId 동아리 가입 신청 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @PutMapping(value = "/applications/{applicationId}/accept")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 가입 신청 수락 API",
            notes = "동아리 가입 신청에 대해 수락하는 API 입니다.\n" +
                    "동아리 가입 신청 건수 고유의 ID 값(PK)을 입력해주세요.\n" +
                    "수락 시 동아리원 데이터의 상태(status)가 AWAIT에서 MEMBER로 변경됩니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "applicationId",
                            value = "동아리 가입 신청 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = ""),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class)
    })
    public CircleMemberResponseDto acceptUser(
            @PathVariable(name = "applicationId") String applicationId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.acceptUser(currentUserId, applicationId);
    }


    /**
     * 동아리 가입 신청 거절 API
     * @param applicationId 동아리 가입 신청 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @PutMapping(value = "/applications/{applicationId}/reject")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 가입 신청 거절 API",
            notes = "동아리 가입 신청에 대해 거절하는 API 입니다.\n" +
                    "동아리 가입 신청 건수 고유의 ID 값(PK)을 입력해주세요.\n" +
                    "거절 시 동아리원으로의 데이터가 삭제되는 것이 아니라 상태(status)가 REJECT로 변경됩니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "applicationId",
                            value = "동아리 가입 신청 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = ""),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class)
    })
    public CircleMemberResponseDto rejectUser(
            @PathVariable(name = "applicationId") String applicationId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.rejectUser(currentUserId, applicationId);
    }


    /**
     * 동아리 삭제 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleResponseDto
     */
    @DeleteMapping(value = "/{circleId}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 삭제 API",
            notes = "동아리 삭제 API 입니다.\n" +
                    "동아리 고유 ID 값(PK)을 입력해주세요.\n" +
                    "삭제 시 동아리 데이터가 아예 삭제되는 것이 아닌 isDeleted가 true로 바뀝니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "circleId",
                            value = "동아리 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path",
                            defaultValue = ""),
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class)
    })
    public CircleResponseDto delete(
            @PathVariable(name = "circleId") String circleId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.delete(currentUserId, circleId);
    }
}

