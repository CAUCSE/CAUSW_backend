package net.causw.app.main.service.report;

import lombok.RequiredArgsConstructor;
import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.report.Report;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.report.ReportType;
import net.causw.app.main.dto.report.*;
import net.causw.app.main.repository.comment.ChildCommentRepository;
import net.causw.app.main.repository.comment.CommentRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.report.ReportRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;
import net.causw.global.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // 신고하기
    @Transactional
    public ReportCreateResponseDto createReport(String reporterId, ReportCreateRequestDto request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ROW_DOES_NOT_EXIST,
                        MessageUtil.USER_NOT_FOUND
                ));
        
        // 중복 신고 체크
        boolean alreadyReported = reportRepository.existsByReporterAndReportTypeAndTargetId(
                reporter, request.getReportType(), request.getTargetId());
        
        if (alreadyReported) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.REPORT_ALREADY_REPORTED
            );
        }
        
        // 신고 대상 콘텐츠, 작성자 조회
        User contentWriter = getContentWriter(request.getReportType(), request.getTargetId());
        
        // 자기 자신 신고 방지
        if (reporter.getId().equals(contentWriter.getId())) {
            throw new BadRequestException(
                    ErrorCode.CANNOT_PERFORMED,
                    MessageUtil.REPORT_CANNOT_SELF
            );
        }
        
        Report report = Report.of(
                reporter,
                request.getReportType(),
                request.getTargetId(),
                request.getReportReason()
        );
        reportRepository.save(report);
        
        // 신고 대상 사용자의 신고 횟수 증가
        contentWriter.increaseReportCount();
        
        return ReportCreateResponseDto.of(MessageUtil.REPORT_SUCCESS);
    }
    
    // 게시글 신고 목록 조회
    @Transactional(readOnly = true)
    public Page<ReportedPostResponseDto> getReportedPosts(Pageable pageable) {
        return reportRepository.findPostReportsWithDetails(ReportType.POST, pageable);
    }
    
    // 댓글/대댓글 신고 목록 조회
    @Transactional(readOnly = true)
    public Page<ReportedCommentResponseDto> getReportedComments(Pageable pageable) {
        return reportRepository.findCombinedCommentReports(pageable)
                               .map(ReportedCommentResponseDto::from);
    }
    
    // 신고된 사용자 목록 조회
    @Transactional(readOnly = true)
    public Page<ReportedUserResponseDto> getReportedUsers(Pageable pageable) {
        Page<User> users = reportRepository.findReportedUsersByReportCount(pageable);
        return users.map(this::convertToReportedUserDto);
    }
    
    // 특정 사용자의 신고된 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<ReportedPostResponseDto> getReportedPostsByUser(
            String userId,
            Pageable pageable
    ) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    ErrorCode.ROW_DOES_NOT_EXIST,
                    MessageUtil.USER_NOT_FOUND
            );
        }
        
        return reportRepository.findPostReportsByUserIdWithDetails(userId, pageable);
    }
    
    // 특정 사용자의 신고된 댓글, 대댓글 목록 조회
    @Transactional(readOnly = true)
    public Page<ReportedCommentResponseDto> getReportedCommentsByUser(
            String userId,
            Pageable pageable
    ) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    ErrorCode.ROW_DOES_NOT_EXIST,
                    MessageUtil.USER_NOT_FOUND
            );
        }
        
        return reportRepository.findCombinedCommentReportsByUserId(userId, pageable)
                               .map(ReportedCommentResponseDto::from);
    }

    // 신고 대상의 작성자 조회
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
    
    // User를 ReportedUserResponseDto로 변환
    private ReportedUserResponseDto convertToReportedUserDto(User user) {
        String profileImage = null;
        if (user.getUserProfileImage() != null) {
            profileImage = user.getUserProfileImage().getUuidFile().getFileUrl();
        }
        
        return ReportedUserResponseDto.of(
                user.getId(),
                user.getName(),
                user.getNickname(),
                profileImage,
                user.getReportCount()
        );
    }
}