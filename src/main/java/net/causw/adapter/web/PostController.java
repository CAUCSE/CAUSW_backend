package net.causw.adapter.web;

import io.swagger.annotations.ApiOperation;
import net.causw.application.post.PostService;
import net.causw.application.dto.post.BoardPostsResponseDto;
import net.causw.application.dto.post.PostCreateRequestDto;
import net.causw.application.dto.post.PostResponseDto;
import net.causw.application.dto.post.PostUpdateRequestDto;
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

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시글 열람 API(validator응답값 추가 예정 / 사용가능)", notes = "게시판에서 게시글을 선택했을 때 게시글을 열람할 수 있습니다.")
    public PostResponseDto findPostById(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.findPostById(loginUserId, id);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시글 전체 조회 API(validator응답값 추가 예정 / 사용가능)", notes = "전체 게시글을 불러오는 api로 페이지 별로 불러올 수 있습니다.\n현재 한 페이지당 20개의 게시글이 조회 가능합니다 \n 1페이지는 value값이 0입니다")
    public BoardPostsResponseDto findAllPost(
            @RequestParam String boardId,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.findAll(loginUserId, boardId, pageNum);
    }

    @GetMapping("/search")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시글 검색 API(validator응답값 추가 예정 / 사용가능)", notes = "게시글을 검색하는 api로 검색 option은 writer와 title 중 택1입니다.")
    public BoardPostsResponseDto searchPost(
            @RequestParam String boardId,
            @RequestParam(defaultValue = "title") String option,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.search(loginUserId, boardId, option, keyword, pageNum);
    }

    @GetMapping("/app/notice")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "앱 자체 공지사항 확인 API(프론트에 없음)", notes = "현재 프론트단에 코드가 존재하지 않습니다")
    public BoardPostsResponseDto findAllAppNotice(
            @RequestParam(defaultValue = "0") Integer pageNum
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.findAllAppNotice(loginUserId, pageNum);
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    @ApiOperation(value = "게시글 생성 API(validator응답값 추가 예정 / 사용가능)", notes = "게시글을 생성하는 api로 각 게시판의 createrolelist에 따라서 작성할 수 있는 권한이 달라집니다.")
    public PostResponseDto createPost(
            @RequestBody PostCreateRequestDto postCreateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.create(loginUserId, postCreateRequestDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시글 삭제 API(validator응답값 추가 예정 / 사용가능)", notes = "게시글을 삭제하는 api로 작성자 본인이나 해당 게시판이 속한 동아리의 동아리장, 관리자, 학생회장의 경우 삭제 가능합니다.")
    public PostResponseDto deletePost(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.delete(loginUserId, id);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시글 업데이트 API(validator응답값 추가 예정 / 사용가능)", notes = "게시글을 업뎅이트하는 api로 작성자 본인이나 해당 게시판이 속한 동아리의 동아리장, 관리자, 학생회장의 경우 업데이트 가능합니다.")
    public PostResponseDto updatePost(
            @PathVariable String id,
            @RequestBody PostUpdateRequestDto postUpdateRequestDto
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.update(
                loginUserId,
                id,
                postUpdateRequestDto
        );
    }

    @PutMapping(value = "/{id}/restore")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation(value = "게시글 복구 API(validator응답값 추가 예정 / 사용가능)", notes = "게시글을 복구하는 api로 작성자 본인이나 해당 게시판이 속한 동아리의 동아리장, 관리자, 학생회장의 경우 복구 가능합니다.")
    public PostResponseDto restorePost(
            @PathVariable String id
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loginUserId = ((String) principal);
        return this.postService.restore(
                loginUserId,
                id
        );
    }
}
