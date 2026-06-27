package net.causw.app.main.domain.community.report.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.implementation.CommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.report.entity.Report;
import net.causw.app.main.domain.community.report.enums.ReportType;
import net.causw.app.main.domain.community.report.service.dto.ChildCommentReportCreateCommand;
import net.causw.app.main.domain.community.report.service.dto.ChildCommentReportCreateResult;
import net.causw.app.main.domain.community.report.service.dto.CommentReportCreateCommand;
import net.causw.app.main.domain.community.report.service.dto.CommentReportCreateResult;
import net.causw.app.main.domain.community.report.service.dto.PostReportCreateCommand;
import net.causw.app.main.domain.community.report.service.dto.PostReportCreateResult;
import net.causw.app.main.domain.community.report.service.implementation.ChildCommentReportReader;
import net.causw.app.main.domain.community.report.service.implementation.ChildCommentReportWriter;
import net.causw.app.main.domain.community.report.service.implementation.CommentReportReader;
import net.causw.app.main.domain.community.report.service.implementation.CommentReportWriter;
import net.causw.app.main.domain.community.report.service.implementation.PostReportReader;
import net.causw.app.main.domain.community.report.service.implementation.PostReportWriter;
import net.causw.app.main.domain.community.report.service.util.ChildCommentReportValidator;
import net.causw.app.main.domain.community.report.service.util.CommentReportValidator;
import net.causw.app.main.domain.community.report.service.util.PostReportValidator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service("reportServiceV2")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

	private final PostReader postReader;
	private final PostReportReader postReportReader;
	private final PostReportWriter postReportWriter;
	private final CommentReader commentReader;
	private final CommentReportReader commentReportReader;
	private final CommentReportWriter commentReportWriter;
	private final ChildCommentReportReader childCommentReportReader;
	private final ChildCommentReportWriter childCommentReportWriter;

	@Transactional
	public PostReportCreateResult createPostReport(PostReportCreateCommand command) {
		User reporter = command.reporter();
		Post post = postReader.findById(command.postId());

		boolean alreadyReported = postReportReader.existsByReporterAndPostId(reporter, post.getId());
		PostReportValidator.validateCreate(reporter, post, alreadyReported);

		Report report = Report.of(reporter, ReportType.POST, post.getId(), command.reportReason());
		Report saved = postReportWriter.save(report);
		post.getWriter().increaseReportCount();

		return PostReportCreateResult.from(saved);
	}

	@Transactional
	public CommentReportCreateResult createCommentReport(CommentReportCreateCommand command) {
		User reporter = command.reporter();
		Comment comment = commentReader.getComment(command.commentId());

		boolean alreadyReported = commentReportReader.existsByReporterAndCommentId(reporter, comment.getId());
		CommentReportValidator.validateCreate(reporter, comment, alreadyReported);

		Report report = Report.of(reporter, ReportType.COMMENT, comment.getId(), command.reportReason());
		Report saved = commentReportWriter.save(report);
		comment.getWriter().increaseReportCount();

		return CommentReportCreateResult.builder()
			.reportId(saved.getId())
			.commentId(comment.getId())
			.reportReason(saved.getReportReason())
			.createdAt(saved.getCreatedAt())
			.build();
	}

	@Transactional
	public ChildCommentReportCreateResult createChildCommentReport(ChildCommentReportCreateCommand command) {
		User reporter = command.reporter();
		Comment childComment = commentReader.getComment(command.childCommentId());

		boolean alreadyReported = childCommentReportReader.existsByReporterAndChildCommentId(
			reporter, childComment.getId());
		ChildCommentReportValidator.validateCreate(reporter, childComment, alreadyReported);

		Report report = Report.of(
			reporter, ReportType.CHILD_COMMENT, childComment.getId(), command.reportReason());
		Report saved = childCommentReportWriter.save(report);
		childComment.getWriter().increaseReportCount();

		return ChildCommentReportCreateResult.builder()
			.reportId(saved.getId())
			.childCommentId(childComment.getId())
			.reportReason(saved.getReportReason())
			.createdAt(saved.getCreatedAt())
			.build();
	}
}
