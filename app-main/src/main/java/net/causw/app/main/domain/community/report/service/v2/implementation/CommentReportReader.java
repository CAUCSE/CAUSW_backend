package net.causw.app.main.domain.community.report.service.v2.implementation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.report.repository.projection.ReportedCommentNativeProjection;
import net.causw.app.main.domain.community.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReportReader {

	private final ReportRepository reportRepository;

	public Page<ReportedCommentNativeProjection> findCombinedCommentReportsByUserId(
		String userId,
		Pageable pageable
	) {
		return reportRepository.findCombinedCommentReports(userId, pageable);
	}
}
