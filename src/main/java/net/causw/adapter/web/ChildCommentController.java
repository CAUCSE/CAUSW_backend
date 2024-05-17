package net.causw.adapter.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import net.causw.application.comment.ChildCommentService;
import net.causw.application.dto.comment.ChildCommentsResponseDto;
import net.causw.application.dto.comment.ChildCommentCreateRequestDto;
import net.causw.application.dto.comment.ChildCommentResponseDto;
import net.causw.application.dto.comment.ChildCommentUpdateRequestDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/child-comments")
public class ChildCommentController {
    private final ChildCommentService childCommentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "대댓글 생성 API(완료)")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "게시글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "답할 답글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "상위 댓글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시판입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 답글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리에서 추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리 가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4008, message = "동아리 가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "동아리를 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 동아리에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4108, message = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
    })
    public ChildCommentResponseDto createChildComment(
            @RequestBody ChildCommentCreateRequestDto childCommentCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.childCommentService.createChildComment(loginUserId, childCommentCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "대댓글 수정 API")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "게시글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "수정할 답글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시판입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 답글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리에서 추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리 가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4008, message = "동아리 가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "동아리를 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 동아리에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4108, message = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "Comment id checked, but exception occurred", response = BadRequestException.class),
    })
    public ChildCommentResponseDto updateChildComment(
            @PathVariable String id,
            @RequestBody ChildCommentUpdateRequestDto childCommentUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.childCommentService.updateChildComment(loginUserId, id, childCommentUpdateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "대댓글 수정 API")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "게시글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "삭제할 답글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시판입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 답글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리에서 추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리 가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4008, message = "동아리 가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "동아리를 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 동아리에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4108, message = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "Comment id checked, but exception occurred", response = BadRequestException.class),
    })
    public ChildCommentResponseDto deleteChildComment(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.childCommentService.deleteChildComment(loginUserId, id);
    }
}
