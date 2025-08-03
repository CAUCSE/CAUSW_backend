package net.causw.app.main.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import net.causw.app.main.domain.model.enums.report.ReportReason;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ReportedPostResponseDto {
    
    @Schema(description = "신고 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String reportId;
    
    @Schema(description = "게시글 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    private final String postId;
    
    @Schema(description = "게시글 제목", example = "신고된 게시글 제목")
    private final String postTitle;
    
    @Schema(description = "작성자 실명", example = "김철수")
    private final String writerName;
    
    @Schema(description = "신고 사유 설명", example = "낚시/놀람/도배")
    private final String reportReasonDescription;
    
    @Schema(description = "신고 생성 시간", example = "2024-03-15T10:30:00")
    private final LocalDateTime reportCreatedAt;
    
    @Schema(description = "게시판 이름", example = "자유게시판")
    private final String boardName;
    
    @Schema(description = "게시글 URL", example = "/posts/123")
    private final String url;
    
    // DTO Projection용 생성자 (board/board-id/post-id 링크 형태)
    public ReportedPostResponseDto(
            String reportId,
            String postId,
            String postTitle,
            String writerName,
            ReportReason reportReason,
            LocalDateTime reportCreatedAt,
            String boardName,
            String boardId
    ) {
        this.reportId = reportId;
        this.postId = postId;
        this.postTitle = postTitle;
        this.writerName = writerName;
        this.reportReasonDescription = reportReason.getDescription();
        this.reportCreatedAt = reportCreatedAt;
        this.boardName = boardName;
        this.url = "/board/" + boardId + "/" + postId;
    }

    public static ReportedPostResponseDto of(
            String reportId,
            String postId,
            String postTitle,
            String writerName,
            String reportReasonDescription,
            LocalDateTime reportCreatedAt,
            String boardName,
            String url
    ) {
        return ReportedPostResponseDto.builder()
                .reportId(reportId)
                .postId(postId)
                .postTitle(postTitle)
                .writerName(writerName)
                .reportReasonDescription(reportReasonDescription)
                .reportCreatedAt(reportCreatedAt)
                .boardName(boardName)
                .url(url)
                .build();
    }
}