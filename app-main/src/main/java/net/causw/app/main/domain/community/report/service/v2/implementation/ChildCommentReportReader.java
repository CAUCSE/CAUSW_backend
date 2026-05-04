package net.causw.app.main.domain.community.report.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.report.enums.ReportType;
import net.causw.app.main.domain.community.report.repository.ReportRepository;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildCommentReportReader {

	private final ReportRepository reportRepository;

	public boolean existsByReporterAndChildCommentId(User reporter, String childCommentId) {
		return reportRepository.existsByReporterAndReportTypeAndTargetId(
			reporter, ReportType.CHILD_COMMENT, childCommentId);
	}
}
