package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.board.BoardService;
import net.causw.application.dto.board.*;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boards")
public class BoardController {
    private final BoardService boardService;

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시판 검색 API(완료)", description = "전체 게시판을 불러오는 api로 관리자 권한을 가진 경우 삭제된 게시판도 확인할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public List<BoardResponseDto> findAllBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.boardService.findAllBoard(userDetails.getUser());
    }


    @GetMapping("/main")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시판 검색 API(완료)", description = "전체 게시판을 불러오는 api로 관리자 권한을 가진 경우 삭제된 게시판도 확인할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public List<BoardMainResponseDto> mainBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.boardService.mainBoard(userDetails.getUser());
    }


    @PostMapping("/check")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시판 이름 중복 체크 API(완료)", description = "게시판 이름 중복 체크 api로 중복된 이름이 존재할 경우 isPresent가 true, 없을 경우 false로 반환됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public BoardNameCheckResponseDto checkBoardName(
            @RequestBody @Valid BoardNameCheckRequestDto boardNameCheckRequestDto
    ) {
        return this.boardService.checkBoardName(boardNameCheckRequestDto);
    }

    @PostMapping(value = "/apply")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "일반 게시판 게시판 신청 API", description = "게시판을 신청하는 API입니다. 권한 정보가 필요 없습니다. 관리자/학생회장/부학생회장일 경우 별도의 신청 절차 없이 바로 일반 게시판이 생성됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public void applyBoard(
            @RequestBody @Valid NormalBoardApplyRequestDto normalBoardApplyRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        this.boardService.applyNormalBoard(userDetails.getUser(), normalBoardApplyRequestDto);
    }


    
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "관리자/학생회장/부학생회장 전용 공지 게시판 생성 API", description = "관리자/학생회장/부학생회장이 별도의 신청 없이 생성할 수 있는 게시판을 만드는 API입니다. 게시판의 이름을 전달 받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public BoardResponseDto createBoard(
            @Valid @RequestBody BoardCreateRequestDto boardCreateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.boardService.createBoard(userDetails.getUser(), boardCreateRequestDto);
    }

    @GetMapping(value = "/apply/list")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "게시판 생성 신청 조회(완료)", description = "게시판 생성 신청 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public List<NormalBoardAppliesResponseDto> findAllBoardApply() {
        return this.boardService.findAllBoardApply();
    }

    @GetMapping(value = "/apply/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "게시판 생성 신청 단일 조회(완료)", description = "단일 게시판 생성 신청 내역을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public NormalBoardApplyResponseDto findBoardApplyById(
            @PathVariable("id") String id
    ) {
        return this.boardService.findBoardApplyByApplyId(id);
    }

    @PutMapping(value = "/apply/{applyId}/accept")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "게시판 생성 신청 승인(완료)", description = "게시판 생성 신청을 승인하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "사용자의 게시판 생성 신청을 찾을 수 없습니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public NormalBoardApplyResponseDto acceptApply(
            @PathVariable("applyId") String applyId
    ) {
        return this.boardService.accept(applyId);
    }

    @PutMapping(value = "/apply/{applyId}/reject")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified() and " +
            "@securityService.isAdminOrPresidentOrVicePresident()")
    @Operation(summary = "게시판 생성 신청 거부(완료)", description = "게시판 생성 신청을 거부하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "사용자의 게시판 생성 신청을 찾을 수 없습니다.", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/json", schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public NormalBoardApplyResponseDto rejectApply(
            @PathVariable("applyId") String applyId
    ) {
        return this.boardService.reject(applyId);
    }


    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시판 업데이트 API(완료)", description = "id에는 board id 값을 넣어주세요")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "게시판을 수정할 수 있는 권한이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4000", description = "수정할 게시판을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "The board has circle without circle leader", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class))),
            @ApiResponse(responseCode = "5001", description = "Board id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public BoardResponseDto updateBoard(
            @PathVariable("id") String id,
            @Valid @RequestBody BoardUpdateRequestDto boardUpdateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.boardService.updateBoard(userDetails.getUser(), id, boardUpdateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시판 삭제 API(완료)", description = "id에는 board id 값을 넣어주세요")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "게시판을 삭제할 수 있는 권한이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4000", description = "삭제할 게시판을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "Board id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public BoardResponseDto deleteBoard(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return this.boardService.deleteBoard(userDetails.getUser(), id);
    }

    @PutMapping(value = "/{id}/restore")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시판 복구 API(완료)", description = "id에는 board id 값을 넣어주세요")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4107", description = "게시판을 복구할 수 있는 권한이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4000", description = "복구할 게시판을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "Board id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public BoardResponseDto restoreBoard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String id
    ) {
        return this.boardService.restoreBoard(userDetails.getUser(), id);
    }
}
