package net.causw.adapter.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.causw.application.board.BoardService;
import net.causw.application.dto.board.BoardCreateRequestDto;
import net.causw.application.dto.board.BoardResponseDto;
import net.causw.application.dto.board.BoardUpdateRequestDto;
import net.causw.domain.exceptions.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/api/v1/boards")
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시판 검색 API(완료)", notes = "전체 게시판을 불러오는 api로 관리자 권한을 가진 경우 삭제된 게시판도 확인할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class)
    })
    public List<BoardResponseDto> findAllBoard() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.boardService.findAllBoard(loginUserId);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation(value = "게시판 생성 API(완료)", notes = "circleId는 현재 존재하는 circleId를 적용해야 합니다(nullable)")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Created", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "게시판을 생성할 수 있는 권한이 아닙니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "동아리를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "The board has circle without circle leader", response = BadRequestException.class)
    })
    public BoardResponseDto createBoard(
            @RequestBody BoardCreateRequestDto boardCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.boardService.createBoard(loginUserId, boardCreateRequestDto);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시판 업데이트 API(완료)", notes = "id 에는 board id 값을 넣어주세요")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "게시판을 수정할 수 있는 권한이 아닙니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "수정할 게시판을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "The board has circle without circle leader", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "Board id checked, but exception occurred", response = BadRequestException.class)
    })
    public BoardResponseDto updateBoard(
            @PathVariable String id,
            @RequestBody BoardUpdateRequestDto boardUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.boardService.updateBoard(loginUserId, id, boardUpdateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시판 삭제 API(완료)", notes = "id 에는 board id 값을 넣어주세요")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "게시판을 삭할 수 있는 권한이 아닙니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "삭제할 게시판을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "The board has circle without circle leader", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "Board id checked, but exception occurred", response = BadRequestException.class)
    })
    public BoardResponseDto deleteBoard(

            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.boardService.deleteBoard(loginUserId, id);
    }

    @PutMapping(value = "/{id}/restore")
    @ResponseStatus(value =  HttpStatus.OK)
    @ApiOperation(value = "게시판 복구 API(완료)", notes = "id 에는 board id 값을 넣어주세요")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 4000, message = "로그인된 사용자를 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 4102, message = "추방된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4103, message = "비활성화된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4104, message = "대기 중인 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4109, message = "가입이 거절된 사용자 입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4012, message = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", response = BadRequestException.class),
            @ApiResponse(code = 4004, message = "삭제된 동아리입니다.", response = BadRequestException.class),
            @ApiResponse(code = 4107, message = "게시판을 복구할 수 있는 권한이 아닙니다.", response = BadRequestException.class),
            @ApiResponse(code = 4000, message = "복구할 게시판을 찾을 수 없습니다.", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "The board has circle without circle leader", response = BadRequestException.class),
            @ApiResponse(code = 5000, message = "Board id checked, but exception occurred", response = BadRequestException.class)
    })
    public BoardResponseDto restoreBoard(

            @PathVariable String id
    ){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.boardService.restoreBoard(loginUserId, id);
    }
}
