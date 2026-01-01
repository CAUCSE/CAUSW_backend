package net.causw.app.main.domain.integration.crawled.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.repository.BoardRepository;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.repository.PostRepository;
import net.causw.app.main.domain.integration.crawled.entity.CrawledFileLink;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.repository.CrawledNoticeRepository;
import net.causw.app.main.domain.notification.notification.service.BoardNotificationService;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.repository.user.UserRepository;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawledToPostTransferService {
	private final CrawledNoticeRepository crawledNoticeRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final BoardRepository boardRepository;
	private final BoardNotificationService boardNotificationService;

	//크롤링 된 공지를 게시글로 반환
	@Transactional
	public void transferToPosts() {
		try {
			Board board = getBoard();
			User adminUser = getAdminUser();
			List<CrawledNotice> updatedNotices = getUpdatedNotices();

			int savedCount = 0;
			for (CrawledNotice notice : updatedNotices) {
				if (processUpdatedNotice(notice, board, adminUser)) {
					notice.setIsUpdated(false);
					crawledNoticeRepository.save(notice);
					savedCount++;
				}
			}
		} catch (Exception e) {
			log.error("게시글 변환 중 오류 발생", e);
		}
	}

	//게시판 조회
	private Board getBoard() {
		return boardRepository.findByName(StaticValue.CrawlingBoard)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.BOARD_NOT_FOUND));
	}

	//관리자 조회
	private User getAdminUser() {
		return userRepository.findByStudentId(StaticValue.ADMIN_STUDENT_ID)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));
	}

	//업데이트된 공지 목록 조회
	private List<CrawledNotice> getUpdatedNotices() {
		return crawledNoticeRepository.findTop30ByIsUpdatedTrueOrderByLastModifiedDesc();
	}

	//업데이트된 공지 처리
	private boolean processUpdatedNotice(CrawledNotice notice, Board board, User adminUser) {
		String title = (notice.getTitle() == null || notice.getTitle().isBlank())
			? "제목 없음" : notice.getTitle();

		// Post 변환 시점에서 첨부파일 링크 추가
		String contentHtml = buildContentWithAttachmentsAndLink(notice);

		// 제목으로 기존 게시글 조회
		Post existingPost = findExistingPostByTitle(board, title);

		if (existingPost != null) {
			// 기존 Post 업데이트
			existingPost.update(title, contentHtml, existingPost.getForm(), existingPost.getPostAttachImageList());
			postRepository.save(existingPost);
		} else {
			// 새 Post 생성
			Post newPost = Post.of(
				title,
				contentHtml,
				adminUser,
				true,
				false,
				board,
				null,
				new ArrayList<>());
			postRepository.save(newPost);

			// 새 게시글인 경우에만 알림 전송
			boardNotificationService.sendByBoardIsSubscribed(board, newPost);
		}
		return true;
	}

	//본문 내용에 첨부파일 링크를 추가하여 반환
	private String buildContentWithAttachmentsAndLink(CrawledNotice notice) {
		StringBuilder contentBuilder = new StringBuilder();

		// 원본 HTML 내용
		String originalContent = (notice.getContent() == null || notice.getContent().isBlank())
			? "<p>내용 없음</p>" : notice.getContent();
		contentBuilder.append(originalContent);

		// 첨부파일이 있으면 링크 추가
		if (notice.getCrawledFileLinks() != null && !notice.getCrawledFileLinks().isEmpty()) {
			contentBuilder.append("<hr style='margin: 20px 0; border: 1px solid #eee;'>");
			contentBuilder.append(
				"<div style='margin-top: 20px; padding: 15px; background-color: #f8f9fa; border-radius: 5px;'>");
			contentBuilder.append("<h4 style='margin: 0 0 10px 0; color: #495057;'>📎 첨부파일</h4>");
			contentBuilder.append("<ul style='margin: 0; padding-left: 20px;'>");

			for (CrawledFileLink fileLink : notice.getCrawledFileLinks()) {
				contentBuilder.append("<li style='margin-bottom: 5px;'>");
				contentBuilder.append("<a href='").append(fileLink.getFileLink()).append("' ");
				contentBuilder.append("target='_blank' ");
				contentBuilder.append("style='color: #007bff; text-decoration: none;'>");
				contentBuilder.append("📄 ").append(fileLink.getFileName());
				contentBuilder.append("</a>");
				contentBuilder.append("</li>");
			}

			contentBuilder.append("</ul>");
			contentBuilder.append("</div>");
		}

		// 원본 링크 정보 추가 (사용자에게 표시)
		contentBuilder.append("<hr style='margin: 20px 0; border: 1px solid #eee;'>");
		contentBuilder.append(
			"<div style='margin-top: 15px; padding: 10px; background-color: #f1f3f4; border-radius: 5px; font-size: 14px; color: #666;'>");
		contentBuilder.append("🔗 <strong>원본 공지사항:</strong> ");
		contentBuilder.append("<a href='")
			.append(notice.getLink())
			.append("' target='_blank' style='color: #1a73e8; text-decoration: none;'>");
		contentBuilder.append(StaticValue.ORIGINAL_NOTICE_SITE_NAME);
		contentBuilder.append("</a>");
		contentBuilder.append("</div>");

		return contentBuilder.toString();
	}

	//제목으로 기존 게시글 조회
	private Post findExistingPostByTitle(Board board, String title) {
		List<Post> existingPosts = postRepository.findAllByBoardAndIsDeletedIsFalse(board);

		for (Post post : existingPosts) {
			if (post.getTitle() != null && post.getTitle().equals(title)) {
				return post;
			}
		}

		return null;
	}
}