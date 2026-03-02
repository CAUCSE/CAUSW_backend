package net.causw.app.main.domain.community.report.service.v2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.report.service.v2.dto.ReportedCommentSummaryResult;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedUserListCondition;
import net.causw.app.main.domain.community.report.service.v2.dto.ReportedUserSummaryResult;
import net.causw.app.main.domain.community.report.service.v2.implementation.CommentReportReader;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportAdminService {

	private final CommentReportReader commentReportReader;
	private final UserReader userReader;

	@Transactional(readOnly = true)
	public Page<ReportedUserSummaryResult> getReportedUserList(
		ReportedUserListCondition condition,
		Pageable pageable
	) {
		return userReader.findReportedUserList(
			condition.keyword(),
			condition.state(),
			condition.academicStatus(),
			pageable
		).map(ReportedUserSummaryResult::from);
	}

	@Transactional(readOnly = true)
	public Page<ReportedCommentSummaryResult> getReportedCommentListByUser(
		String userId,
		Pageable pageable
	) {
		// 존재하지 않는 사용자 조회 요청은 404로 처리
		userReader.findUserById(userId);

		return commentReportReader.findCombinedCommentReportsByUserId(userId, pageable)
			.map(ReportedCommentSummaryResult::from);
	}
}
