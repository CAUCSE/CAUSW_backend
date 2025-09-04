package net.causw.app.main.service.report;

import net.causw.app.main.domain.model.entity.comment.ChildComment;
import net.causw.app.main.domain.model.entity.comment.Comment;
import net.causw.app.main.domain.model.entity.post.Post;
import net.causw.app.main.domain.model.entity.report.Report;
import net.causw.app.main.domain.model.entity.user.User;
import net.causw.app.main.domain.model.enums.report.ReportReason;
import net.causw.app.main.domain.model.enums.report.ReportType;
import net.causw.app.main.domain.model.enums.user.UserState;
import net.causw.app.main.dto.report.*;
import net.causw.app.main.dto.util.dtoMapper.ReportDtoMapper;
import net.causw.app.main.service.pageable.PageableFactory;
import net.causw.global.constant.StaticValue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import net.causw.app.main.repository.comment.ChildCommentRepository;
import net.causw.app.main.repository.comment.CommentRepository;
import net.causw.app.main.repository.post.PostRepository;
import net.causw.app.main.repository.report.ReportRepository;
import net.causw.app.main.repository.user.UserRepository;
import net.causw.app.main.util.ObjectFixtures;
import net.causw.global.constant.MessageUtil;
import net.causw.global.exception.BadRequestException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

	@InjectMocks
	private ReportService reportService;

	@Mock
	private ReportRepository reportRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private PostRepository postRepository;
	@Mock
	private CommentRepository commentRepository;
	@Mock
	private ChildCommentRepository childCommentRepository;
	@Mock
	private PageableFactory pageableFactory;

	private static final int pageNum = 0;
	private final User reporter = ObjectFixtures.getUser();
	private final User contentWriter = ObjectFixtures.getUser();
	private final Post post = ObjectFixtures.getPost(contentWriter, ObjectFixtures.getBoard());
	private final Comment comment = ObjectFixtures.getComment(contentWriter, post);
	private final ChildComment childComment = ObjectFixtures.getChildComment(contentWriter, comment);
	private ReportCreateRequestDto reportRequest;

	@BeforeEach
	void setUp() {
		// 신고자와 작성자 구분을 위해 이메일과 ID 수정
		reporter.setEmail("reporter@test.com");
		ReflectionTestUtils.setField(reporter, "id", "reporter-id");

		contentWriter.setEmail("writer@test.com");
		ReflectionTestUtils.setField(contentWriter, "id", "writer-id");
		contentWriter.setState(UserState.ACTIVE);

		reportRequest = new ReportCreateRequestDto();
		reportRequest.setReportType(ReportType.POST);
		reportRequest.setTargetId("target-id");
		reportRequest.setReportReason(ReportReason.SPAM_AD);
	}

	@Nested
	@DisplayName("신고 테스트")
	class CreateReportTest {

		@Test
		@DisplayName("게시글 신고 성공")
		void createPostReport_success() {
			// given
			given(reportRepository.existsByReporterAndReportTypeAndTargetId(
				reporter, ReportType.POST, "target-id")).willReturn(false);
			given(postRepository.findById("target-id")).willReturn(Optional.of(post));

			// when
			ReportCreateResponseDto response = reportService.createReport(reporter, reportRequest);

			// then
			assertThat(response.getMessage()).isEqualTo(MessageUtil.REPORT_SUCCESS);

			ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
			verify(reportRepository).save(reportCaptor.capture());

			Report savedReport = reportCaptor.getValue();
			assertThat(savedReport.getReporter()).isEqualTo(reporter);
			assertThat(savedReport.getReportType()).isEqualTo(ReportType.POST);
			assertThat(savedReport.getTargetId()).isEqualTo("target-id");
			assertThat(savedReport.getReportReason()).isEqualTo(ReportReason.SPAM_AD);

			// 신고 횟수 증가 확인
			assertThat(contentWriter.getReportCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("댓글 신고 성공")
		void createCommentReport_success() {
			// given
			reportRequest.setReportType(ReportType.COMMENT);

			given(reportRepository.existsByReporterAndReportTypeAndTargetId(
				reporter, ReportType.COMMENT, "target-id")).willReturn(false);
			given(commentRepository.findById("target-id")).willReturn(Optional.of(comment));

			// when
			ReportCreateResponseDto response = reportService.createReport(reporter, reportRequest);

			// then
			assertThat(response.getMessage()).isEqualTo(MessageUtil.REPORT_SUCCESS);
			assertThat(contentWriter.getReportCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("대댓글 신고 성공")
		void createChildCommentReport_success() {
			// given
			reportRequest.setReportType(ReportType.CHILD_COMMENT);

			given(reportRepository.existsByReporterAndReportTypeAndTargetId(
				reporter, ReportType.CHILD_COMMENT, "target-id")).willReturn(false);
			given(childCommentRepository.findById("target-id")).willReturn(Optional.of(childComment));

			// when
			ReportCreateResponseDto response = reportService.createReport(reporter, reportRequest);

			// then
			assertThat(response.getMessage()).isEqualTo(MessageUtil.REPORT_SUCCESS);
			assertThat(contentWriter.getReportCount()).isEqualTo(1);
		}

		@Test
		@DisplayName("중복 신고 시 예외 발생")
		void createReport_alreadyReported() {
			// given
			given(reportRepository.existsByReporterAndReportTypeAndTargetId(
				reporter, ReportType.POST, "target-id")).willReturn(true);

			// when & then
			assertThatThrownBy(() -> reportService.createReport(reporter, reportRequest))
				.isInstanceOf(BadRequestException.class)
				.hasMessage(MessageUtil.REPORT_ALREADY_REPORTED);
		}
	}

	@Nested
	@DisplayName("신고 목록 조회 테스트")
	class GetReportListTest {

		@Test
		@DisplayName("게시글 신고 목록 조회 성공")
		void getReportedPosts_success() {
			// given
			Pageable pageable = PageRequest.of(0, 20);
			given(pageableFactory.create(0, StaticValue.DEFAULT_REPORT_PAGE_SIZE)).willReturn(pageable);
			Page<ReportedPostNativeProjection> projectionPage = Page.empty();
			Page<ReportedPostResponseDto> expectedPage = projectionPage.map(
				ReportDtoMapper.INSTANCE::toReportedPostDto);

			given(reportRepository.findPostReportsWithDetails("POST", null, pageable))
				.willReturn(projectionPage);

			// when
			Page<ReportedPostResponseDto> result = reportService.getReportedPosts(pageNum);

			// then
			assertThat(result).isEqualTo(expectedPage);
			verify(reportRepository).findPostReportsWithDetails("POST", null, pageable);
		}

		@Test
		@DisplayName("댓글/대댓글 신고 목록 조회 성공")
		void getReportedComments_success() {
			// given
			Pageable pageable = PageRequest.of(0, 20);
			given(pageableFactory.create(0, StaticValue.DEFAULT_REPORT_PAGE_SIZE)).willReturn(pageable);
			Page<ReportedCommentNativeProjection> nativePage = Page.empty();

			given(reportRepository.findCombinedCommentReports(null, pageable))
				.willReturn(nativePage);

			// when
			Page<ReportedCommentResponseDto> result = reportService.getReportedComments(pageNum);

			// then
			assertThat(result).isNotNull();
			verify(reportRepository).findCombinedCommentReports(null, pageable);
		}

		@Test
		@DisplayName("신고된 사용자 목록 조회 성공")
		void getReportedUsers_success() {
			// given
			Pageable pageable = PageRequest.of(0, 20);
			given(pageableFactory.create(0, StaticValue.DEFAULT_REPORT_PAGE_SIZE)).willReturn(pageable);
			Page<User> userPage = Page.empty();

			given(reportRepository.findReportedUsersByReportCount(pageable))
				.willReturn(userPage);

			// when
			Page<ReportedUserResponseDto> result = reportService.getReportedUsers(pageNum);

			// then
			assertThat(result).isNotNull();
			verify(reportRepository).findReportedUsersByReportCount(pageable);
		}

		@Test
		@DisplayName("특정 사용자의 신고된 게시글 목록 조회 성공")
		void getReportedPostsByUser_success() {
			// given
			String userId = "test-user-id";
			Pageable pageable = PageRequest.of(0, 20);
			given(pageableFactory.create(0, StaticValue.DEFAULT_REPORT_PAGE_SIZE)).willReturn(pageable);
			Page<ReportedPostNativeProjection> projectionPage = Page.empty();
			Page<ReportedPostResponseDto> expectedPage = projectionPage.map(
				ReportDtoMapper.INSTANCE::toReportedPostDto);

			given(userRepository.existsById(userId)).willReturn(true);
			given(reportRepository.findPostReportsWithDetails("POST", userId, pageable))
				.willReturn(projectionPage);

			// when
			Page<ReportedPostResponseDto> result = reportService.getReportedPostsByUser(userId, pageNum);

			// then
			assertThat(result).isEqualTo(expectedPage);
			verify(userRepository).existsById(userId);
			verify(reportRepository).findPostReportsWithDetails("POST", userId, pageable);
		}

		@Test
		@DisplayName("특정 사용자의 신고된 댓글/대댓글 목록 조회 성공")
		void getReportedCommentsByUser_success() {
			// given
			String userId = "test-user-id";
			Pageable pageable = PageRequest.of(pageNum, 20);
			given(pageableFactory.create(pageNum, StaticValue.DEFAULT_REPORT_PAGE_SIZE)).willReturn(pageable);
			Page<ReportedCommentNativeProjection> nativePage = Page.empty();

			given(userRepository.existsById(userId)).willReturn(true);
			given(reportRepository.findCombinedCommentReports(userId, pageable))
				.willReturn(nativePage);

			// when
			Page<ReportedCommentResponseDto> result = reportService.getReportedCommentsByUser(userId, pageNum);

			// then
			assertThat(result).isNotNull();
			verify(userRepository).existsById(userId);
			verify(reportRepository).findCombinedCommentReports(userId, pageable);
		}
	}
}