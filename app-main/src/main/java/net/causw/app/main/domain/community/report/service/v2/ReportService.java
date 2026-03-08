package net.causw.app.main.domain.community.report.service.v2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.comment.entity.ChildComment;
import net.causw.app.main.domain.community.comment.entity.Comment;
import net.causw.app.main.domain.community.comment.service.v2.implementation.ChildCommentReader;
import net.causw.app.main.domain.community.comment.service.v2.implementation.CommentReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.report.entity.Report;
import net.causw.app.main.domain.community.report.enums.ReportType;
import net.causw.app.main.domain.community.report.service.v2.dto.ChildCommentReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.ChildCommentReportCreateResult;
import net.causw.app.main.domain.community.report.service.v2.dto.CommentReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.CommentReportCreateResult;
import net.causw.app.main.domain.community.report.service.v2.dto.PostReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.PostReportCreateResult;
import net.causw.app.main.domain.community.report.service.v2.implementation.ChildCommentReportReader;
import net.causw.app.main.domain.community.report.service.v2.implementation.ChildCommentReportWriter;
import net.causw.app.main.domain.community.report.service.v2.implementation.CommentReportReader;
import net.causw.app.main.domain.community.report.service.v2.implementation.CommentReportWriter;
import net.causw.app.main.domain.community.report.service.v2.implementation.PostReportReader;
import net.causw.app.main.domain.community.report.service.v2.implementation.PostReportWriter;
import net.causw.app.main.domain.community.report.service.v2.util.ChildCommentReportValidator;
import net.causw.app.main.domain.community.report.service.v2.util.CommentReportValidator;
import net.causw.app.main.domain.community.report.service.v2.util.PostReportValidator;
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
	private final ChildCommentReader childCommentReader;
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
		Comment comment = commentReader.findByIdAndNotDeleted(command.commentId());

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
		ChildComment childComment = childCommentReader.findByIdAndNotDeleted(command.childCommentId());

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
