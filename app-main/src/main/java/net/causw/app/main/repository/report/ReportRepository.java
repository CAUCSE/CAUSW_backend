package net.causw.app.main.repository.report;

import net.causw.app.main.domain.model.entity.report.Report;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.report.ReportType;
import net.causw.app.main.dto.report.ReportedPostNativeProjection;
import net.causw.app.main.dto.report.ReportedCommentNativeProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, String> {

    // 중복 신고 체크
    boolean existsByReporterAndReportTypeAndTargetId(User reporter, ReportType reportType, String targetId);

    // 게시글 신고 목록 조회 (통합된 메서드)
    @Query(
            value = """
            SELECT 
                r.id AS reportId,
                p.id AS postId,
                p.title AS postTitle,
                u.name AS writerName,
                r.report_reason AS reportReason,
                r.created_at AS reportCreatedAt,
                b.name AS boardName,
                b.id AS boardId
            FROM tb_report r 
            JOIN tb_post p ON r.target_id = p.id 
            JOIN tb_user u ON p.user_id = u.id 
            JOIN tb_board b ON p.board_id = b.id 
            WHERE r.report_type = :reportType 
            AND (:userId IS NULL OR u.id = :userId)
            ORDER BY r.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM tb_report r 
            JOIN tb_post p ON r.target_id = p.id 
            JOIN tb_user u ON p.user_id = u.id 
            WHERE r.report_type = :reportType
            AND (:userId IS NULL OR u.id = :userId)
            """,
            nativeQuery = true
    )
    Page<ReportedPostNativeProjection> findPostReportsWithDetails(
            @Param("reportType") String reportType,
            @Param("userId") String userId,
            Pageable pageable
    );

    // 신고된 사용자 목록 조회
    @Query("SELECT u FROM User u " +
            "WHERE u.reportCount > 0 " +
            "ORDER BY u.reportCount DESC")
    Page<User> findReportedUsersByReportCount(Pageable pageable);

    // 댓글, 대댓글 신고 목록 조회(메모리 문제를 막기 위해 Native Query와 UNION ALL로 DB 내에서 처리하도록 함)
    @Query(
            value = """
            SELECT 
                reportId, contentId, content, postTitle, postId, boardId, writerName, reportReason, reportCreatedAt 
            FROM ( 
                SELECT 
                    r.id AS reportId, 
                    c.id AS contentId, 
                    c.content AS content, 
                    p.title AS postTitle, 
                    p.id AS postId, 
                    p.board_id AS boardId,
                    u.name AS writerName, 
                    r.report_reason AS reportReason, 
                    r.created_at AS reportCreatedAt
                FROM tb_report r 
                JOIN tb_comment c ON r.target_id = c.id AND r.report_type = 'COMMENT' 
                JOIN tb_post p ON c.post_id = p.id 
                JOIN tb_user u ON c.user_id = u.id 
                WHERE (:userId IS NULL OR u.id = :userId)
                UNION ALL 
                SELECT 
                    r.id AS reportId, 
                    cc.id AS contentId, 
                    cc.content AS content, 
                    p.title AS postTitle, 
                    p.id AS postId, 
                    p.board_id AS boardId,
                    u.name AS writerName, 
                    r.report_reason AS reportReason, 
                    r.created_at AS reportCreatedAt
                FROM tb_report r 
                JOIN tb_child_comment cc ON r.target_id = cc.id AND r.report_type = 'CHILD_COMMENT' 
                JOIN tb_comment parent_c ON cc.parent_comment_id = parent_c.id 
                JOIN tb_post p ON parent_c.post_id = p.id 
                JOIN tb_user u ON cc.user_id = u.id 
                WHERE (:userId IS NULL OR u.id = :userId)
            ) AS combined_reports 
            ORDER BY reportCreatedAt DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM ( 
                SELECT r.id 
                FROM tb_report r 
                JOIN tb_comment c ON r.target_id = c.id AND r.report_type = 'COMMENT' 
                JOIN tb_user u ON c.user_id = u.id 
                WHERE (:userId IS NULL OR u.id = :userId)
                UNION ALL 
                SELECT r.id 
                FROM tb_report r 
                JOIN tb_child_comment cc ON r.target_id = cc.id AND r.report_type = 'CHILD_COMMENT' 
                JOIN tb_user u ON cc.user_id = u.id 
                WHERE (:userId IS NULL OR u.id = :userId)
            ) AS T
            """,
            nativeQuery = true
    )
    Page<ReportedCommentNativeProjection> findCombinedCommentReports(
            @Param("userId") String userId,
            Pageable pageable
    );
}