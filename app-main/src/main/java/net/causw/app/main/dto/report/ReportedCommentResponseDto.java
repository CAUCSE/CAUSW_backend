package net.causw.app.main.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import net.causw.app.main.domain.model.enums.report.ReportReason;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportedCommentResponseDto {
    
    @Schema(description = "신고 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String reportId;
    
    @Schema(description = "댓글 ID", example = "550e8400-e29b-41d4-a716-446655440002")
    private final String commentId;
    
    @Schema(description = "댓글 내용", example = "신고된 댓글 내용")
    private final String commentContent;
    
    @Schema(description = "댓글이 달린 게시글 제목", example = "댓글이 달린 게시글 제목")
    private final String parentPostTitle;
    
    @Schema(description = "댓글이 달린 게시글 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private final String parentPostId;
    
    @Schema(description = "작성자 실명", example = "김철수")
    private final String writerName;
    
    @Schema(description = "신고 사유 설명", example = "욕설/비하")
    private final String reportReasonDescription;
    
    @Schema(description = "신고 생성 시간", example = "2024-03-15T10:30:00")
    private final LocalDateTime reportCreatedAt;
    
    @Schema(description = "게시글 URL", example = "/board/board-id/post-id")
    private final String url;
    
    public static ReportedCommentResponseDto from(ReportedCommentNativeProjection projection) {
        return ReportedCommentResponseDto.builder()
                .reportId(projection.getReportId())
                .commentId(projection.getContentId())
                .commentContent(projection.getContent())
                .parentPostTitle(projection.getPostTitle())
                .parentPostId(projection.getPostId())
                .writerName(projection.getWriterName())
                .reportReasonDescription(ReportReason.valueOf(projection.getReportReason()).getDescription())
                .reportCreatedAt(projection.getReportCreatedAt())
                .url("/board/" + projection.getBoardId() + "/" + projection.getPostId())
                .build();
    }

    public static ReportedCommentResponseDto of(
            String reportId,
            String commentId,
            String commentContent,
            String parentPostTitle,
            String parentPostId,
            String writerName,
            String reportReasonDescription,
            LocalDateTime reportCreatedAt,
            String url
    ) {
        return ReportedCommentResponseDto.builder()
                .reportId(reportId)
                .commentId(commentId)
                .commentContent(commentContent)
                .parentPostTitle(parentPostTitle)
                .parentPostId(parentPostId)
                .writerName(writerName)
                .reportReasonDescription(reportReasonDescription)
                .reportCreatedAt(reportCreatedAt)
                .url(url)
                .build();
    }
}