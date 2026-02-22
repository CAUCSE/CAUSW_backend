package net.causw.app.main.domain.community.report.service.v2;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.v2.implementation.PostReader;
import net.causw.app.main.domain.community.report.entity.Report;
import net.causw.app.main.domain.community.report.enums.ReportType;
import net.causw.app.main.domain.community.report.service.v2.dto.PostReportCreateCommand;
import net.causw.app.main.domain.community.report.service.v2.dto.PostReportCreateResult;
import net.causw.app.main.domain.community.report.service.v2.implementation.PostReportReader;
import net.causw.app.main.domain.community.report.service.v2.implementation.PostReportWriter;
import net.causw.app.main.domain.community.report.service.v2.util.PostReportValidator;
import net.causw.app.main.domain.user.account.entity.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReportService {

	private final PostReader postReader;
	private final PostReportReader postReportReader;
	private final PostReportWriter postReportWriter;

	@Transactional
	public PostReportCreateResult createReport(PostReportCreateCommand command) {
		User reporter = command.reporter();
		Post post = postReader.findById(command.postId());

		boolean alreadyReported = postReportReader.existsByReporterAndPostId(reporter, post.getId());
		PostReportValidator.validateCreate(reporter, post, alreadyReported);

		Report report = Report.of(reporter, ReportType.POST, post.getId(), command.reportReason());
		Report saved = postReportWriter.save(report);
		post.getWriter().increaseReportCount();

		return PostReportCreateResult.from(saved);
	}
}
