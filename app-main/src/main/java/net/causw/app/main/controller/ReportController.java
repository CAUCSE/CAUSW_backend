package net.causw.app.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.causw.app.main.dto.report.ReportCreateRequestDto;
import net.causw.app.main.dto.report.ReportCreateResponseDto;
import net.causw.app.main.infrastructure.security.userdetails.CustomUserDetails;
import net.causw.app.main.service.report.ReportService;
import net.causw.global.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportService reportService;
    
    @PostMapping
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(summary = "콘텐츠 신고 API", description = "게시글, 댓글, 대댓글을 신고할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportCreateResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "이미 신고한 콘텐츠입니다.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BadRequestException.class)))
    })
    public ReportCreateResponseDto createReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ReportCreateRequestDto request
    ) {
        return reportService.createReport(userDetails.getUser().getId(), request);
    }
}