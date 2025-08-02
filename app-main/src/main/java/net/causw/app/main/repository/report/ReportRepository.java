package net.causw.app.main.repository.report;

import net.causw.app.main.domain.model.entity.report.Report;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.report.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {
    
    // 중복 신고 체크
    boolean existsByReporterAndReportTypeAndTargetId(User reporter, ReportType reportType, String targetId);
}