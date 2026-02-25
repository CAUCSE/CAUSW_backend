package net.causw.app.main.domain.community.report.service.v2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.v2.implementation.CommentReader;
import net.causw.app.main.domain.community.report.entity.Report;
import net.causw.app.main.domain.community.report.enums.ReportType;
import net.causw.app.main.domain.community.report.service.v2.dto.CommentReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.CommentReportCreateResult;
import net.causw.app.main.domain.community.report.service.v2.implementation.CommentReportReader;
import net.causw.app.main.domain.community.report.service.v2.implementation.CommentReportWriter;
import net.causw.app.main.domain.community.report.service.v2.util.CommentReportValidator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReportService {

	private final CommentReader commentReader;
	private final CommentReportReader commentReportReader;
	private final CommentReportWriter commentReportWriter;

	@Transactional
	public CommentReportCreateResult createReport(CommentReportCreateCommand command) {
		User reporter = command.reporter();
		Comment comment = commentReader.findByIdAndNotDeleted(command.commentId());

		boolean alreadyReported = commentReportReader.existsByReporterAndCommentId(reporter, comment.getId());
		CommentReportValidator.validateCreate(reporter, comment, alreadyReported);

		Report report = Report.of(reporter, ReportType.COMMENT, comment.getId(), command.reportReason());
		Report saved = commentReportWriter.save(report);

		return CommentReportCreateResult.builder()
			.reportId(saved.getId())
			.commentId(comment.getId())
			.reportReason(saved.getReportReason())
			.createdAt(saved.getCreatedAt())
			.build();
	}
}
