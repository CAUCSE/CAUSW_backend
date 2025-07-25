package net.causw.app.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.service.circle.CircleService;
import net.causw.app.main.dto.circle.CirclesResponseDto;
import net.causw.app.main.dto.circle.CircleCreateRequestDto;
import net.causw.app.main.dto.circle.CircleMemberResponseDto;
import net.causw.app.main.dto.circle.CircleResponseDto;
import net.causw.app.main.dto.circle.CircleUpdateRequestDto;
import net.causw.app.main.dto.circle.CircleBoardsResponseDto;
import net.causw.app.main.dto.duplicate.DuplicatedCheckResponseDto;
import net.causw.app.main.dto.form.request.FormReplyRequestDto;
import net.causw.app.main.dto.form.request.create.FormCreateRequestDto;
import net.causw.app.main.dto.form.response.FormResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.InternalServerException;
import net.causw.global.exception.UnauthorizedException;
import net.causw.app.main.domain.model.enums.circle.CircleMemberStatus;
import net.causw.app.main.domain.validation.ConstraintValidator;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/circles")
public class CircleController {
    private final CircleService circleService;

    /**
     * 동아리 고유 id 값으로 동아리 정보를 조회하는 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleResponseDto
     */
    @GetMapping(value = "/{circleId}")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "동아리 정보 조회 API / findById (완료)", description = "circleId에는 동아리 고유 id 값(PK)을 입력해주세요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CircleResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public CircleResponseDto findById(
            @PathVariable(name = "circleId") String circleId
    ) {
        return this.circleService.findById(circleId);
    }


    /**
     * 전체 동아리 정보를 조회하는 API
     * @return List<CircleResponseDto>
     */
    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "전체 동아리 정보 조회 API / findAll (완료)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CircleResponseDto.class, type = "array"))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public List<CirclesResponseDto> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.circleService.findAll(userDetails.getUser());
    }


    /**
     * 동아리 고유 id 값으로 동아리 소속 게시판을 조회하는 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleBoardsResponseDto
     */
    @GetMapping("/{circleId}/boards")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "동아리 소속 게시판 조회 API / findBoards (완료)", description = "circleId에는 동아리 고유 id 값(PK)을 입력해주세요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CircleBoardsResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 소모임에 가입한 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "소모임을 떠난 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "가입 대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "가입 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public CircleBoardsResponseDto findBoards(
            @PathVariable(name = "circleId") String circleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.circleService.findBoards(userDetails.getUser(), circleId);
    }


    /**
     * 동아리 고유 id 값으로 동아리원 숫자를 조회하는 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return Long
     */
    @GetMapping(value = "/{circleId}/num-member")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(summary = "동아리원 숫자 조회 API / getNumMember (완료)", description = "circleId에는 동아리 고유 id 값(PK)을 입력해주세요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public Long getNumMember(
            @PathVariable(name = "circleId") String circleId
    ) {
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
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(summary = "동아리원 상태별 조회 API / getUserList (완료)", description = "circleId에는 동아리 고유 id 값(PK), circleMemberStatus 엔 조회하고자 하는 동아리원의 상태를 입력해주세요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CircleMemberResponseDto.class), array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = CircleMemberResponseDto.class)))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "소모임원을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public List<CircleMemberResponseDto> getUserList(
            @PathVariable("circleId") String circleId,
            @RequestParam("circleMemberStatus") @NotNull(message = "동아리원 상태는 null이 아니어야 합니다.") CircleMemberStatus circleMemberStatus,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return this.circleService.getUserList(
                userDetails.getUser(),
                circleId,
                circleMemberStatus
        );
    }

    @GetMapping(value = "/{circleId}/memberList")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "모든 동아리원 조회 API(완료)", description = "circleId에는 동아리 고유 id 값(PK)을 입력해주세요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CircleMemberResponseDto.class), array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = CircleMemberResponseDto.class)))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "소모임원을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public List<CircleMemberResponseDto> findCircleMemberList(
            @PathVariable("circleId") String circleId
    ) {
        return this.circleService.getMemberList(
                circleId
        );
    }

    /**
     * 동아리 생성 API
     * @param  circleCreateRequestDto 동아리 생성 정보
     * @return Long
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(
            summary = "동아리 생성 API / create (완료)",
            description = "생성하고자 하는 동아리의 정보를 입력해주세요. 동아리장의 권한은 일반 유저만 가능하며, 생성 요청은 관리자(admin), 학생회장(president)만 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = void.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "중복된 소모임 이름입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "0", description = "ConstraintValidator", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConstraintValidator.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4106", description = "권한을 부여할 수 없습니다. -부여하는 사용자 권한 : {grantorRole}, 부여할 권한 : {grantedRole}, 부여받는 사용자 권한 : {granteeRole}", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "Leader id checked, but exception occured", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "Circle id immediately can be used, but exception occured", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void create(
            @RequestPart(value = "circleCreateRequestDto") @Valid CircleCreateRequestDto circleCreateRequestDto,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage

    ) {
        circleService.create(circleCreateRequestDto, mainImage);
    }


    /**
     * 동아리 수정 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @param  circleUpdateRequestDto 동아리 생성 정보
     * @return Long
     */
    @PutMapping(value = "/{circleId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(
            summary = "동아리 수정 API / update (완료)",
            description = "circleId 에는 수정하고자 하는 동아리의 UUID 형식의 ID String 값을 입력해주세요.\n" +
                    "circleUpdateRequestDto 에는 수정하고자 하는 동아리의 정보를 입력해주세요.\n" +
                    "동아리장의 권한은 일반 유저만 가능하며, 생성 요청은 관리자(admin), 학생회장(president)만 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CircleResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "수정할 소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "중복된 소모임 이름입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "0", description = "ConstraintValidator", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ConstraintValidator.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "This circle has not circle leader", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class))),
            @ApiResponse(responseCode = "5000", description = "Circle id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public CircleResponseDto update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "circleId") String circleId,
            @RequestPart(value = "circleUpdateRequestDto") @Valid CircleUpdateRequestDto circleUpdateRequestDto,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage

    ) {
        return this.circleService.update(userDetails.getUser(), circleId, circleUpdateRequestDto, mainImage);
    }


    /**
     * 동아리 삭제 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleResponseDto
     */
    @DeleteMapping(value = "/{circleId}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(
            summary = "동아리 삭제 API",
            description = "동아리 삭제 API 입니다.\n" +
                    "동아리 고유 ID 값(PK)을 입력해주세요.\n" +
                    "삭제 시 동아리 데이터가 아예 삭제되는 것이 아닌 isDeleted 가 true 로 바뀝니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CircleMemberResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "삭제할 소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "This circle has not circle leader", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class))),
            @ApiResponse(responseCode = "5000", description = "Leader id of this circle is null", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class))),
            @ApiResponse(responseCode = "5000", description = "Leader id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class))),
            @ApiResponse(responseCode = "5000", description = "Circle id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public CircleResponseDto delete(
            @PathVariable(name = "circleId") String circleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.circleService.delete(userDetails.getUser(), circleId);
    }




    @PostMapping(value = "/{circleId}/applications")
    @ResponseStatus(value = HttpStatus.CREATED)
    @Operation(
            summary = "사용자 동아리 지원 API",
            description = "사용자가 동아리에 지원하는 API 입니다.\n" +
                    "현재 로그인한 사용자 기준으로 동아리 ID와 해당 동아리 신청서(Form)의 답변을 입력하면 해당 동아리에 지원됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = void.class))),
            @ApiResponse(responseCode = "4000", description = "신청할 소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4009", description = "학번이 입력되지 않았습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 소모임에 가입한 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "소모임을 떠난 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "가입 대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "가입 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "Application id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void userApply(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "circleId") String circleId,
            @RequestBody @Valid FormReplyRequestDto formReplyRequestDto
    ) {
        circleService.userApply(userDetails.getUser(), circleId, formReplyRequestDto);
    }


    /**
     * 동아리 이름 중복 검사 API
     * @param circleName 동아리 이름
     * @return DuplicatedCheckResponseDto
     */
    @GetMapping(value = "/{circleName}/is-duplicated")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "동아리 이름 중복 검사 API",
            description = "동아리 이름 중복 검사 API 입니다. 이름 기준으로 검사하면 String 형식으로 동아리 이름을 넣어주세요.")
    @ApiResponse(
            responseCode = "200",
            description = "OK",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DuplicatedCheckResponseDto.class))
    )
    public DuplicatedCheckResponseDto isDuplicatedName(
            @PathVariable(name = "circleName") @NotBlank(message = "동아리명은 공백이 아니어야 합니다.") String circleName
    ) {
        return this.circleService.isDuplicatedName(circleName);
    }


    /**
     * 동아리 탈퇴 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @PutMapping(value = "/{circleId}/users/leave")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
            summary = "동아리 탈퇴 API",
            description = "현재 로그인 된 유저 기준으로 동아리에서 탈퇴합니다.\n" +
                    "탈퇴 시 해당 유저의 동아리 가입 정보가 LEAVE 상태로 변경됩니다."
    )
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CircleMemberResponseDto.class)))
    @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없거나 탈퇴할 소모임을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4001", description = "이미 소모임에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "4102", description = "추방된 사용자이거나 다른 권한 관련 오류가 발생했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "5000", description = "동아리에 대한 특정 예외가 발생했습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    public CircleMemberResponseDto leaveUser(
            @PathVariable(name = "circleId") String circleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.circleService.leaveUser(userDetails.getUser(), circleId);
    }


    /**
     * 동아리원 제거 API
     * @param userId 유저 고유 ID 값(PK)
     * @param circleId 동아리 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @PutMapping(value = "/{circleId}/users/{userId}/drop")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(summary = "동아리원 제거 API",
            description = "동아리원을 제거하는 API 입니다. userId 에는 제거하려는 유저를, circleId 에는 타깃 동아리를 넣어주세요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CircleMemberResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없거나 추방할 사용자를 찾을 수 없거나 소모임을 찾을 수 없거나 추방시킬 사용자가 가입 신청한 소모임이 아닙니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "추방할 사용자를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "추방시킬 사용자가 가입 신청한 소모임이 아닙니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "This circle has not circle leader or Application id checked, but exception occurred", content = @Content(schema = @Schema(implementation = InternalServerException.class))),
            @ApiResponse(responseCode = "5000", description = "Application id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public CircleMemberResponseDto dropUser(
            @PathVariable(name = "userId") String userId,
            @PathVariable(name = "circleId") String circleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.circleService.dropUser(
                userDetails.getUser(),
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
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(summary = "동아리 가입 신청 수락 API",
            description = "동아리 가입 신청에 대해 수락하는 API 입니다.\n" +
                    "동아리 가입 신청 건수 고유의 ID 값(PK)을 입력해주세요.\n" +
                    "수락 시 동아리원 데이터의 상태(status)가 AWAIT 에서 MEMBER 로 변경됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CircleMemberResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없거나 소모임 가입 신청을 찾을 수 없거나 가입 요청한 사용자를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "This circle has not circle leader or Application id checked, but exception occurred", content = @Content(schema = @Schema(implementation = InternalServerException.class)))
    })
    public CircleMemberResponseDto acceptUser(
            @PathVariable(name = "applicationId") String applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.circleService.acceptUser(userDetails.getUser(), applicationId);
    }


    /**
     * 동아리 가입 신청 거절 API
     * @param applicationId 동아리 가입 신청 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @PutMapping(value = "/applications/{applicationId}/reject")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(summary = "동아리 가입 신청 거절 API",
            description = "동아리 가입 신청에 대해 거절하는 API 입니다.\n" +
                    "동아리 가입 신청 건수 고유의 ID 값(PK)을 입력해주세요.\n" +
                    "거절 시 동아리원으로의 데이터가 삭제되는 것이 아니라 상태(status)가 REJECT 로 변경됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CircleMemberResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없거나 소모임 가입 신청을 찾을 수 없거나 가입 요청한 사용자를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "This circle has not circle leader or Application id checked, but exception occurred", content = @Content(schema = @Schema(implementation = InternalServerException.class)))
    })
    public CircleMemberResponseDto rejectUser(
            @PathVariable(name = "applicationId") String applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.circleService.rejectUser(userDetails.getUser(), applicationId);
    }


    /**
     * 동아리 가입 신청 거절 API
     * @param circleId 동아리 고유 ID 값(PK)
     * @param userId 복구 유저 고유 ID 값(PK)
     * @return CircleMemberResponseDto
     */
    @PutMapping(value = "/{circleId}/users/{userId}/restore")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(summary = "추방된 동아리원 복구 API",
            description = "추방된 동아리원을 복구 시키는 API 입니다. 복구 시 동아리원으로 바꿔줍니다.\n" +
                    "해당하는 동아리 고유의 ID 값(PK)과 복구하려는 유저 고유의 ID 값(PK)를 입력해주세요.\n" +
                    "복구 시 동아리원으로 상태(status)가 ACTIVE 로 변경됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CircleMemberResponseDto.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없거나 소모임 가입 신청을 찾을 수 없거나 가입 요청한 사용자를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "복구할 사용자를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "소모임을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 {동아리명} 입니다.", content = @Content(schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "This circle has not circle leader or Application id checked, but exception occurred", content = @Content(schema = @Schema(implementation = InternalServerException.class)))
    })
    public CircleMemberResponseDto restoreUser(
            @PathVariable(name = "circleId") String circleId,
            @PathVariable(name = "userId") String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.circleService.restoreUser(userDetails.getUser(), circleId, userId);
    }


    @GetMapping(value = "/{circleId}/users/excel")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(
            summary = "동아리원 엑셀 다운로드 API",
            description = "동아리원 정보를 엑셀로 다운로드 하는 API 입니다.\n" +
                    "동아리 고유 ID 값(PK)을 입력해주세요.\n" +
                    "엑셀 다운로드 시 해당 동아리의 동아리원 정보가 엑셀로 다운로드 됩니다."
    )
    public void exportExcel(
            @PathVariable(name = "circleId") String circleId,
            HttpServletResponse response
    ){
        circleService.exportCircleMembersToExcel(circleId, response);
    }

    @PostMapping(value = "/{circleId}/apply/application")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRole(@Role.LEADER_CIRCLE)")
    @Operation(
            summary = "동아리 가입 신청서 생성/수정 API",
            description = "동아리 가입 신청서를 수정하는 API 입니다.\n" +
                    "동아리 고유 ID 값(PK)을 입력해주세요.\n" +
                    "동아리 가입 신청서를 수정하면 해당 동아리에 가입 신청서가 생성/수정됩니다. (기존에 있던 신청서 모두 비활성화)"
    )
    public void createApplicationForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "circleId") String circleId,
            @RequestBody @Valid FormCreateRequestDto formCreateRequestDto
    ) {
        circleService.createApplicationForm(userDetails.getUser(), circleId, formCreateRequestDto);
    }

    @GetMapping(value = "/{circleId}/apply/application/is-exist")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
            summary = "동아리 가입 신청서 존재 여부 확인 API",
            description = "현재 모집 중인 동아리 가입 신청서가 존재하는지 확인하는 API 입니다.\n" +
                    "동아리 고유 ID 값(PK)을 입력해주세요.\n" +
                    "동아리 가입 신청서가 존재하면 true, 존재하지 않으면 false 를 반환합니다."
    )
    public Boolean isCircleApplicationFormExist(
            @PathVariable(name = "circleId") String circleId
    ) {
        return circleService.isCircleApplicationFormExist(circleId);
    }

    @GetMapping(value = "/{circleId}/apply/application")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
            summary = "동아리 가입 신청서 조회 API",
            description = "동아리 가입 신청서를 조회하는 API 입니다.\n" +
                    "동아리 고유 ID 값(PK)을 입력해주세요.\n" +
                    "동아리 가입 신청서를 조회하면 해당 동아리에 가입 신청서가 반환됩니다."
    )
    public FormResponseDto getCircleApplicationForm(
            @PathVariable(name = "circleId") String circleId
    ) {
        return circleService.getCircleApplicationForm(circleId);
    }

    @GetMapping(value = "/{circleId}/apply/application/all")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES_AND_CIRCLE_LEADER)")
    @Operation(
            summary = "모든 동아리 가입 신청서 페이징 조회 API",
            description = "모든 동아리 가입 신청서를 조회하는 API 입니다.\n" +
                    "동아리 고유 ID 값(PK)을 입력해주세요.\n" +
                    "모든 동아리 가입 신청서를 조회하면 해당 동아리에 마감되었거나 삭제된 가입 신청서를 포함해 모든 가입 신청서가가 반환됩니다. (동아리장 및 관리자/학생회장 포함)"
    )
    public Page<FormResponseDto> getAllCircleApplicationFormList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable(name = "circleId") String circleId,
            @ParameterObject Pageable pageable
    ) {
        return circleService.getAllCircleApplicationFormList(userDetails.getUser(), circleId, pageable);
    }

}

