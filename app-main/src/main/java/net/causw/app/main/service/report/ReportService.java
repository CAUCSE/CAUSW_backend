package net.causw.app.main.service.report;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.report.Report;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.report.ReportType;
import net.causw.app.main.dto.report.ReportCreateRequestDto;
import net.causw.app.main.dto.report.ReportCreateResponseDto;
import net.causw.app.main.repository.comment.ChildCommentRepository;
import net.causw.app.main.repository.comment.CommentRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.report.ReportRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChildCommentRepository childCommentRepository;
    
    @Transactional
    public ReportCreateResponseDto createReport(String reporterId, ReportCreateRequestDto request) {
        // 1. 신고자 조회
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));
        
        // 2. 중복 신고 체크
        boolean alreadyReported = reportRepository.existsByReporterAndReportTypeAndTargetId(
                reporter, request.getReportType(), request.getTargetId());
        
        if (alreadyReported) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.REPORT_ALREADY_REPORTED
            );
        }
        
        // 3. 신고 대상 콘텐츠, 작성자 조회
        User contentWriter = getContentWriter(request.getReportType(), request.getTargetId());
        
        // 4. 신고 저장
        Report report = Report.of(
                reporter,
                request.getReportType(),
                request.getTargetId(),
                request.getReportReason()
        );
        reportRepository.save(report);
        
        // 5. 콘텐츠 작성자의 신고 카운트 증가 및 자동 정지 처리
        contentWriter.increaseReportCount();
        userRepository.save(contentWriter);
        
        return ReportCreateResponseDto.of(MessageUtil.REPORT_SUCCESS);
    }
    
    private User getContentWriter(ReportType reportType, String targetId) {
        return switch (reportType) {
            case POST -> {
                Post post = postRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.POST_NOT_FOUND
                        ));
                yield post.getWriter();
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.COMMENT_NOT_FOUND
                        ));
                yield comment.getWriter();
            }
            case CHILD_COMMENT -> {
                ChildComment childComment = childCommentRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundException(
                                ErrorCode.ROW_DOES_NOT_EXIST,
                                MessageUtil.CHILD_COMMENT_NOT_FOUND
                        ));
                yield childComment.getWriter();
            }
        };
    }
}