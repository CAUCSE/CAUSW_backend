package net.causw.app.main.dto.report;

import java.time.LocalDateTime;

import net.causw.app.main.domain.model.enums.user.UserState;

/**
 * Native Query 결과를 받기 위한 Projection 인터페이스
 * 게시글 신고를 조회할 때 사용
 */
public interface ReportedPostNativeProjection {
    String getReportId();
    String getPostId();
    String getPostTitle();
    String getWriterName();
    UserState getWriterState();
    String getReportReason();
    LocalDateTime getReportCreatedAt();
    String getBoardName();
    String getBoardId();
}