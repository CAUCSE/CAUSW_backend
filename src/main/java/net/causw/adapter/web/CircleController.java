package net.causw.adapter.web;

import com.sun.xml.bind.v2.runtime.reflect.opt.Const;
import io.swagger.annotations.*;
import net.causw.application.circle.CircleService;
import net.causw.application.dto.circle.CirclesResponseDto;
import net.causw.application.dto.circle.CircleCreateRequestDto;
import net.causw.application.dto.circle.CircleMemberResponseDto;
import net.causw.application.dto.circle.CircleResponseDto;
import net.causw.application.dto.circle.CircleUpdateRequestDto;
import net.causw.application.dto.circle.CircleBoardsResponseDto;
import net.causw.application.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import net.causw.domain.model.enums.CircleMemberStatus;
import net.causw.domain.validation.ConstraintValidator;
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
            paramType = "path"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleResponseDto.class),
            @ApiResponse(code = 4000, message = "소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
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
            @ApiResponse(code = 200, message = "OK", response = CircleResponseDto.class, responseContainer = "List"),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class)
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
            paramType = "path"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleBoardsResponseDto.class),
            @ApiResponse(code = 4000, message = "소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 소모임에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "소모임을 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4008, message = "가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4108, message = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", response = UnauthorizedException.class)
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
            paramType = "path"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = Long.class),
            @ApiResponse(code = 4000, message = "소모임을 찾을 수 없습니다.", response = BadRequestException.class)
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
                            paramType = "path"
                    ),
                    @ApiImplicitParam(name = "circleMemberStatus",
                            value = "동아리원 상태",
                            required = true,
                            dataType = "string",
                            paramType = "query"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class, responseContainer = "List"),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "소모임원을 찾을 수 없습니다.", response = BadRequestException.class)

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
            @ApiResponse(code = 201, message = "Created", response = CircleResponseDto.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "중복된 소모임 이름입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 0, message = "ConstraintValidator", response = ConstraintValidator.class),
            @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4106,
                    message = "권한을 부여할 수 없습니다. -부여하는 사용자 권한 : {grantorRole}, 부여할 권한 : {grantedRole}, 부여받는 사용자 권한 : {granteeRole}",
                    response = UnauthorizedException.class),
            @ApiResponse(code = 5000, message = "Leader id checked, but exception occured", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "Circle id immediately can be used, but exception occured", response = InternalServerException.class)

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
            notes = "circleId 에는 수정하고자 하는 동아리의 UUID 형식의 ID String 값을 입력해주세요.\n" +
                    "circleUpdateRequestDto 에는 수정하고자 하는 동아리의 정보를 입력해주세요.\n" +
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
            @ApiResponse(code = 200, message = "OK", response = CircleResponseDto.class),
            @ApiResponse(code = 4000, message = "수정할 소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "중복된 소모임 이름입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 0, message = "ConstraintValidator", response = ConstraintValidator.class),
            @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 5000, message = "This circle has not circle leader", response = InternalServerException.class),
            @ApiResponse(code = 5000, message = "Circle id checked, but exception occurred", response = InternalServerException.class)
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
     * 동아리 삭제 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleResponseDto
     */
    @DeleteMapping(value = "/{circleId}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "동아리 삭제 API",
            notes = "동아리 삭제 API 입니다.\n" +
                    "동아리 고유 ID 값(PK)을 입력해주세요.\n" +
                    "삭제 시 동아리 데이터가 아예 삭제되는 것이 아닌 isDeleted 가 true 로 바뀝니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "circleId",
                            value = "동아리 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class),
            @ApiResponse(code = 4000, message = "삭제할 소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 5000, message = "This circle has not circle leader", response = InternalServerException.class),
            @ApiResponse(code = 5000, message = "Leader id of this circle is null", response = InternalServerException.class),
            @ApiResponse(code = 5000, message = "Leader id checked, but exception occurred", response = InternalServerException.class),
            @ApiResponse(code = 5000, message = "Circle id checked, but exception occurred", response = InternalServerException.class)
    })
    public CircleResponseDto delete(
            @PathVariable(name = "circleId") String circleId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.delete(currentUserId, circleId);
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
            @ApiResponse(code = 201, message = "Created", response = CircleMemberResponseDto.class),
            @ApiResponse(code = 4000, message = "신청할 소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4009, message = "학번이 입력되지 않았습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 소모임에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "소모임을 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4008, message = "가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 5000, message = "Application id checked, but exception occurred", response = InternalServerException.class)
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
                            paramType = "path"
                    ),
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
                            paramType = "path"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "탈퇴할 소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "가입 신청한 소모임이 아닙니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 소모임에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "소모임을 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4008, message = "가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 5000, message = "This circle has not circle leader", response = InternalServerException.class),
            @ApiResponse(code = 5000, message = "Application id checked, but exception occurred", response = InternalServerException.class)
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
                            paramType = "path"
                    ),
                    @ApiImplicitParam(name = "circleId",
                            value = "동아리 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "추방할 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "소모임을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "추방시킬 사용자가 가입 신청한 소모임이 아닙니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 5000, message = "This circle has not circle leader", response = InternalServerException.class),
            @ApiResponse(code = 5000, message = "Application id checked, but exception occurred", response = InternalServerException.class)
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
                    "수락 시 동아리원 데이터의 상태(status)가 AWAIT 에서 MEMBER 로 변경됩니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "applicationId",
                            value = "동아리 가입 신청 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "소모임 가입 신청을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "가입 요청한 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 5000, message = "This circle has not circle leader", response = InternalServerException.class),
            @ApiResponse(code = 5000, message = "Application id checked, but exception occurred", response = InternalServerException.class)
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
                    "거절 시 동아리원으로의 데이터가 삭제되는 것이 아니라 상태(status)가 REJECT 로 변경됩니다.")
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(name = "applicationId",
                            value = "동아리 가입 신청 ID",
                            required = true,
                            dataType = "string",
                            paramType = "path"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CircleMemberResponseDto.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "소모임 가입 신청을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "가입 요청한 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = UnauthorizedException.class),
            @ApiResponse(code = 4004, message = "삭제된 {동아리명} 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "접근 권한이 없습니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 5000, message = "This circle has not circle leader", response = InternalServerException.class),
            @ApiResponse(code = 5000, message = "Application id checked, but exception occurred", response = InternalServerException.class)
    })
    public CircleMemberResponseDto rejectUser(
            @PathVariable(name = "applicationId") String applicationId
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserId = ((String) principal);

        return this.circleService.rejectUser(currentUserId, applicationId);
    }

}

