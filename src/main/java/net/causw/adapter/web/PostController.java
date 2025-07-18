package net.causw.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.application.dto.post.*;
import net.causw.application.post.PostService;
import net.causw.config.security.userdetails.CustomUserDetails;
import net.causw.domain.exceptions.BadRequestException;
import net.causw.domain.exceptions.InternalServerException;
import net.causw.domain.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    @GetMapping(value = "/{id}", produces = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 열람 API(완료)", description = "게시판에서 게시글을 선택했을 때 게시글을 열람할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public PostResponseDto findPostById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.postService.findPostById(userDetails.getUser(), id);
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 전체 조회 API(완료)",
            description = "전체 게시글을 불러오는 API로 페이지 별로 불러올 수 있습니다. 현재 한 페이지당 20개의 게시글이 조회 가능합니다. 1페이지는 value값이 0입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시판을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public BoardPostsResponseDto findAllPost(
            @RequestParam("boardId") String boardId, // 게시판 id
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum, // PageNation
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.postService.findAllPost(userDetails.getUser(), boardId, pageNum);
    }

    @GetMapping("/search")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 검색 API(완료)",
            description = "게시글을 검색하는 API로 제목의 연관검색어로 검색 가능합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    @ApiResponse(responseCode = "4004", description = "삭제된 게시판입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    public BoardPostsResponseDto searchPost(
            @RequestParam("boardId") String boardId,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.postService.searchPost(userDetails.getUser(), boardId, keyword, pageNum);
    }

    @GetMapping("/app/notice")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "앱 자체 공지사항 확인 API(프론트에 없음)", description = "현재 프론트단에 코드가 존재하지 않습니다")
    public BoardPostsResponseDto findAllAppNotice(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return this.postService.findAllAppNotice(userDetails.getUser(), pageNum);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 생성 API(완료)",
            description = "게시글을 생성하는 API로 각 게시판의 createrolelist에 따라서 작성할 수 있는 권한이 달라집니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시판을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "사용자가 해당 동아리의 동아리장이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public PostCreateResponseDto createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "postCreateRequestDto") @Valid PostCreateRequestDto postCreateRequestDto,
            @RequestPart(value = "attachImageList", required = false) List<MultipartFile> attachImageList
    ) {
        return this.postService.createPost(userDetails.getUser(), postCreateRequestDto, attachImageList);
    }

    @PostMapping(value = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "신청서 첨부 게시글 생성 API(완료)",
            description = "신청서와 함께 게시글을 생성하는 API로 각 게시판의 createrolelist에 따라서 작성할 수 있는 권한이 달라집니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시판을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "사용자가 해당 동아리의 동아리장이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public PostCreateResponseDto createPostWithForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "postCreateWithFormRequestDto") @Valid PostCreateWithFormRequestDto postCreateWithFormRequestDto,
            @RequestPart(value = "attachImageList", required = false) List<MultipartFile> attachImageList
    ) {
        return this.postService.createPostWithForm(userDetails.getUser(), postCreateWithFormRequestDto, attachImageList);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 삭제 API(완료)",
            description = "게시글을 삭제하는 API로 작성자 본인이나 해당 게시판이 속한 동아리의 동아리장, 관리자, 학생회장의 경우 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시판입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "사용자가 해당 동아리의 동아리장이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "Post id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void deletePost(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePost(userDetails.getUser(), id);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 업데이트 API(완료)",
            description = "게시글을 업데이트하는 API로 작성자 본인이나 해당 게시판이 속한 동아리의 동아리장, 관리자, 학생회장의 경우 업데이트 가능합니다. 기존에 신청서가 있었던 게시글의 경우 신청서가 사라집니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4002", description = "4개 이상의 파일을 첨부할 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시판입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "사용자가 해당 동아리의 동아리장이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "Post id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String id,
            @RequestPart(value = "postUpdateRequestDto") @Valid PostUpdateRequestDto postUpdateRequestDto,
            @RequestPart(value = "attachImageList", required = false) List<MultipartFile> attachImageList
    ) {

        postService.updatePost(
                userDetails.getUser(),
                id,
                postUpdateRequestDto,
                attachImageList
        );
    }

    @PutMapping(value = "/{id}/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "신청서 첨부 게시글 업데이트 API(완료)",
            description = "신청서와 함께 게시글을 업데이트하는 API로 작성자 본인이나 해당 게시판이 속한 동아리의 동아리장, 관리자, 학생회장의 경우 업데이트 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4002", description = "4개 이상의 파일을 첨부할 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시판입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "사용자가 해당 동아리의 동아리장이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "Post id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void updatePostWithForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String id,
            @RequestPart(value = "postUpdateWithFormRequestDto") @Valid PostUpdateWithFormRequestDto postUpdateWithFormRequestDto,
            @RequestPart(value = "attachImageList", required = false) List<MultipartFile> attachImageList
    ) {
        postService.updatePostWithForm(userDetails.getUser(), id, postUpdateWithFormRequestDto, attachImageList);
    }

    @PutMapping(value = "/{id}/restore")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 복구 API(완료)",
            description = "게시글을 복구하는 API로 작성자 본인이나 해당 게시판이 속한 동아리의 동아리장, 관리자, 학생회장의 경우 복구 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4002", description = "4개 이상의 파일을 첨부할 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시판입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제되지 않은 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4108", description = "로그인된 사용자가 동아리 멤버가 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 동아리입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "이미 동아리에 가입한 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4006", description = "동아리를 떠난 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4008", description = "동아리 가입 대기 중인 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리 가입 거절된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4102", description = "동아리에서 추방된 사용자입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "사용자가 해당 동아리의 동아리장이 아닙니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4107", description = "접근 권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "5000", description = "Post id checked, but exception occurred", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InternalServerException.class)))
    })
    public void restorePost(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.restorePost(
                userDetails.getUser(),
                id
        );
    }

    @PostMapping(value ="/{id}/like" )
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 좋아요 저장 API(완료)",
            description = "특정 유저가 특정 게시글에 좋아요를 누른 걸 저장하는 Api 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "좋아요를 이미 누른 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public void likePost(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        this.postService.likePost(userDetails.getUser(), id);
    }

    @DeleteMapping(value ="/{id}/like" )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 좋아요 취소 API(완료)",
        description = "특정 유저가 특정 게시글에 좋아요를 누른 걸 취소하는 Api 입니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
        @ApiResponse(responseCode = "4000", description = "좋아요를 누르지 않은 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
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
        this.postService.cancelLikePost(userDetails.getUser(), id);
    }

    @PostMapping(value ="/{id}/favorite" )
    @ResponseStatus(value = HttpStatus.CREATED)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 즐겨찾기 저장 API(완료)",
            description = "특정 유저가 특정 게시글에 즐겨찾기를 누른 걸 저장하는 Api 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4001", description = "즐겨찾기를 이미 누른 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public void favoritePost(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        this.postService.favoritePost(userDetails.getUser(), id);
    }

    @DeleteMapping(value ="/{id}/favorite" )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "게시글 즐겨찾기 취소 API(완료)",
            description = "특정 유저가 특정 게시글에 즐겨찾기를 누른 걸 취소하는 Api 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "4000", description = "로그인된 사용자를 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "게시글을 찾을 수 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "즐겨찾기가 되어 있지 않습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4000", description = "즐겨찾기가 이미 취소되어 있습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4004", description = "삭제된 게시글입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "4102", description = "추방된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4103", description = "비활성화된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4104", description = "대기 중인 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4109", description = "가입이 거절된 사용자 입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class))),
            @ApiResponse(responseCode = "4012", description = "접근 권한이 없습니다. 다시 로그인 해주세요. 문제 반복시 관리자에게 문의해주세요.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
    })
    public void cancelFavoritePost(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        this.postService.cancelFavoritePost(userDetails.getUser(), id);
    }


    @PostMapping("/subscribe/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "로그인한 사용자의 게시글 알람 설정 켜기"
            , description = "id에는 post id 값을 넣어주세요")
    public PostSubscribeResponseDto subscribePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String id
    ){
        return postService.setPostSubscribe(userDetails.getUser(), id, true);
    }

    @DeleteMapping("/subscribe/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@securityService.isActiveAndNotNoneUserAndAcademicRecordCertified()")
    @Operation(summary = "로그인한 사용자의 게시글 알람 설정 끄기"
            , description = "id에는 post id 값을 넣어주세요")
    public PostSubscribeResponseDto unsubscribePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") String id
    ){
        return postService.setPostSubscribe(userDetails.getUser(), id, false);
    }



}
