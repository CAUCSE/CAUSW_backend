package net.causw.app.main.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import net.causw.app.main.dto.comment.ChildCommentCreateRequestDto;
import net.causw.app.main.dto.comment.ChildCommentResponseDto;
import net.causw.app.main.dto.comment.ChildCommentUpdateRequestDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.comment.ChildCommentService;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/child-comments")
public class ChildCommentController {
    private final ChildCommentService childCommentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "대댓글 생성 API(완료)", description = "대댓글을 생성하는 api입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "답할 답글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "상위 댓글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시판입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 답글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public ChildCommentResponseDto createChildComment(
            @Valid @RequestBody ChildCommentCreateRequestDto childCommentCreateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        return this.childCommentService.createChildComment(userDetails.getUser(), childCommentCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "대댓글 수정 API", description = "특정 대댓글을 수정하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "수정할 답글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시판입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 답글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "Comment id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public ChildCommentResponseDto updateChildComment(
            @PathVariable("id") String id,
            @Valid @RequestBody ChildCommentUpdateRequestDto childCommentUpdateRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.childCommentService.updateChildComment(userDetails.getUser(), id, childCommentUpdateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "대댓글 삭제 API", description = "특정 대댓글을 삭제하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "삭제할 답글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시판입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 답글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "5000", description = "Comment id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public ChildCommentResponseDto deleteChildComment(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.childCommentService.deleteChildComment(userDetails.getUser(), id);
    }

    @PostMapping(value = "/{id}/like")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "대댓글 좋아요 저장 API(완료)", description = "특정 유저가 특정 대댓글에 좋아요를 누른 걸 저장하는 Api 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "댓글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 댓글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public void likeChildComment(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        this.childCommentService.likeChildComment(userDetails.getUser(), id);
    }

    @DeleteMapping(value ="/{id}/like" )
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "대댓글 좋아요 취소 API(완료)",
            description = "특정 유저가 특정 대댓글에 좋아요를 누른 걸 취소하는 Api 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "대댓글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "좋아요를 누르지 않은 대댓글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public void cancelLikePost(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        this.childCommentService.cancelLikeChildComment(userDetails.getUser(), id);
    }
}
