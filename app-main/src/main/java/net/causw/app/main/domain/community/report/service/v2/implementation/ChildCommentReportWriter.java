package net.causw.app.main.domain.community.report.service.v2.implementation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.report.entity.Report;
import net.causw.app.main.domain.community.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class ChildCommentReportWriter {

	private final ReportRepository reportRepository;

	public Report save(Report report) {
		return reportRepository.save(report);
	}
}
