package net.causw.app.main.domain.model.entity.report;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.report.ReportReason;
import net.causw.app.main.domain.model.enums.report.ReportType;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
    name = "tb_report",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_user_content_report",
            columnNames = {"reporter_id", "report_type", "target_id"}
        )
    }
)
public class Report extends BaseEntity {
    
    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;
    
    @Column(name = "target_id", nullable = false)
    private String targetId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_reason", nullable = false)
    private ReportReason reportReason;
    
    public static Report of(
            User reporter,
            ReportType reportType,
            String targetId,
            ReportReason reportReason
    ) {
        return Report.builder()
                .reporter(reporter)
                .reportType(reportType)
                .targetId(targetId)
                .reportReason(reportReason)
                .build();
    }
}