package net.causw.app.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.dto.report.*;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.report.ReportService;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.UnauthorizedException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {
    
    private final ReportService reportService;
    
    @PostMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "콘텐츠 신고 API", description = "게시글(POST), 댓글(COMMENT), 대댓글(CHILD_COMMENT)을 신고할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "이미 신고한 콘텐츠입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class))),
            @ApiResponse(responseCode = "401", description = "권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public ReportCreateResponseDto createReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReportCreateRequestDto request
    ) {
        return reportService.createReport(userDetails.getUser(), request);
    }
    
    @GetMapping("/posts")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "게시글 신고 목록 조회 API", description = "관리자가 신고된 게시글 목록을 조회할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public Page<ReportedPostResponseDto> getReportedPosts(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
            ) {
        return reportService.getReportedPosts(pageNum);
    }
    
    @GetMapping("/comments")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "댓글/대댓글 신고 목록 조회 API", description = "관리자가 신고된 댓글과 대댓글 목록을 조회할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public Page<ReportedCommentResponseDto> getReportedComments(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return reportService.getReportedComments(pageNum);
    }
    
    @GetMapping("/users")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "신고된 사용자 목록 조회 API", description = "관리자가 신고된 사용자 목록을 조회할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public Page<ReportedUserResponseDto> getReportedUsers(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return reportService.getReportedUsers(pageNum);
    }
    
    @GetMapping("/users/{userId}/posts")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "특정 사용자의 신고된 게시글 목록 조회 API", description = "관리자가 특정 사용자가 작성한 신고된 게시글 목록을 조회할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public Page<ReportedPostResponseDto> getReportedPostsByUser(
            @Parameter(description = "사용자 ID", example = "user-uuid")
            @PathVariable String userId,
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return reportService.getReportedPostsByUser(userId, pageNum);
    }
    
    @GetMapping("/users/{userId}/comments")
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("@security.hasRoleGroup(@RoleGroup.EXECUTIVES)")
    @Operation(summary = "특정 사용자의 신고된 댓글/대댓글 목록 조회 API", description = "관리자가 특정 사용자가 작성한 신고된 댓글과 대댓글 목록을 조회할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "권한이 없습니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnauthorizedException.class)))
    })
    public Page<ReportedCommentResponseDto> getReportedCommentsByUser(
            @Parameter(description = "사용자 ID", example = "user-uuid")
            @PathVariable String userId,
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum
    ) {
        return reportService.getReportedCommentsByUser(userId, pageNum);
    }
}