package net.causw.adapter.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.causw.application.comment.CommentService;
import net.causw.application.dto.comment.CommentCreateRequestDto;
import net.causw.application.dto.comment.CommentResponseDto;
import net.causw.application.dto.comment.CommentUpdateRequestDto;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.UnauthorizedException;
import org.springframework.data.domain.Page;
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
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping(params = "postId")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "댓글 조회 API(완료)", notes = "해당 게시글의 전체 댓글을 불러오는 api입니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "게시글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시판입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "동아리에서 추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리 가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4008, message = "동아리 가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "동아리를 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 동아리에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4108, message = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class)
    })
    public Page<CommentResponseDto> findAllComments(
            @RequestParam String postId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.commentService.findAllComments(loginUserId, postId, pageNum);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "댓글 생성 API(완료)")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "게시글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시판입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시글입니다.", response = BadRequestException.class),
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
    public CommentResponseDto createComment(
            @RequestBody CommentCreateRequestDto commentCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.commentService.createComment(loginUserId, commentCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "댓글 수정 API(완료)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "게시글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "수정할 댓글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시판입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 댓글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리에서 추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리 가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4008, message = "동아리 가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "동아리를 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 동아리에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4108, message = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "Comment id checked, but exception occurred", response = BadRequestException.class),

    })
    public CommentResponseDto updateComment(
            @PathVariable String id,
            @RequestBody CommentUpdateRequestDto commentUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.commentService.updateComment(
                loginUserId,
                id,
                commentUpdateRequestDto
        );
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "댓글 삭제 API(완료)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "게시글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "삭제할 댓글을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시판입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 댓글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 게시글입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4108, message = "로그인된 사용자가 가입 신청한 소모임이 아닙니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리에서 추방된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4102, message = "동아리 가입 거절된 사용자 입니다.", response = UnauthorizedException.class),
            @ApiResponse(code = 4008, message = "동아리 가입 대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4006, message = "동아리를 떠난 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4001, message = "이미 동아리에 가입한 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "The board has circle without circle leader", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "Comment id checked, but exception occurred", response = BadRequestException.class),
    })
    public CommentResponseDto deleteComment(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.commentService.deleteComment(loginUserId, id);
    }
}
