package net.causw.app.main.domain.community.report.service.v2.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.report.enums.ReportType;
import net.causw.app.main.domain.community.report.repository.ReportRepository;
import net.causw.app.main.domain.community.report.repository.projection.ReportedPostNativeProjection;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReportReader {

	private final ReportRepository reportRepository;

	public boolean existsByReporterAndPostId(User reporter, String postId) {
		return reportRepository.existsByReporterAndReportTypeAndTargetId(reporter, ReportType.POST, postId);
	}

	public Page<ReportedPostNativeProjection> findPostReportsByUserId(
		String userId,
		Pageable pageable) {
		return reportRepository.findPostReportsWithDetails(ReportType.POST.name(), userId, pageable);
	}
}
