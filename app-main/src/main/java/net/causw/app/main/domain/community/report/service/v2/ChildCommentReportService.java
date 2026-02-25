package net.causw.app.main.domain.community.report.service.v2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.service.v2.implementation.ChildCommentReader;
import net.causw.app.main.domain.community.report.entity.Report;
import net.causw.app.main.domain.community.report.enums.ReportType;
import net.causw.app.main.domain.community.report.service.v2.dto.ChildCommentReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.ChildCommentReportCreateResult;
import net.causw.app.main.domain.community.report.service.v2.implementation.ChildCommentReportReader;
import net.causw.app.main.domain.community.report.service.v2.implementation.ChildCommentReportWriter;
import net.causw.app.main.domain.community.report.service.v2.util.ChildCommentReportValidator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChildCommentReportService {

	private final ChildCommentReader childCommentReader;
	private final ChildCommentReportReader childCommentReportReader;
	private final ChildCommentReportWriter childCommentReportWriter;

	@Transactional
	public ChildCommentReportCreateResult createReport(ChildCommentReportCreateCommand command) {
		User reporter = command.reporter();
		ChildComment childComment = childCommentReader.findByIdAndNotDeleted(command.childCommentId());

		boolean alreadyReported = childCommentReportReader.existsByReporterAndChildCommentId(
			reporter, childComment.getId());
		ChildCommentReportValidator.validateCreate(reporter, childComment, alreadyReported);

		Report report = Report.of(
			reporter, ReportType.CHILD_COMMENT, childComment.getId(), command.reportReason());
		Report saved = childCommentReportWriter.save(report);

		return ChildCommentReportCreateResult.builder()
			.reportId(saved.getId())
			.childCommentId(childComment.getId())
			.reportReason(saved.getReportReason())
			.createdAt(saved.getCreatedAt())
			.build();
	}
}
