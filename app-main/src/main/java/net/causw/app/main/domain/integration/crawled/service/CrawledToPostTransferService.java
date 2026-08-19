package net.causw.app.main.domain.integration.crawled.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostReader;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.integration.crawled.entity.CrawledFileLink;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.entity.CrawledPostImage;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledPostImageWriter;
import net.causw.app.main.domain.notification.notification.event.OfficialPostEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
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
	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeWriter crawledNoticeWriter;
	private final PostReader postReader;
	private final PostWriter postWriter;
	private final UserReader userReader;
	private final BoardReader boardReader;
	private final CrawledPostImageWriter crawledPostImageWriter;

	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 전송 대기 중인 크롤링 공지를 대상 게시판의 Post로 생성하거나 갱신합니다.
	 */
	@Transactional
	public void transferToPosts() {
		User systemUser = getSystemUser();
		List<CrawledNotice> updatedNotices = getUpdatedNotices();
		Map<String, Board> boardCache = new HashMap<>();

		int savedCount = 0;
		for (CrawledNotice notice : updatedNotices) {
			Board board = boardCache.computeIfAbsent(notice.getTargetBoardId(), boardReader::getById);
			Post post = processUpdatedNotice(notice, board, systemUser);
			crawledNoticeWriter.markTransferred(notice, post);
			savedCount++;
		}
		log.info("[크롤링] 공지 Post 변환 완료. 변환 수={}", savedCount);
	}

	//관리자 조회
	private User getSystemUser() {
		return userReader.findByEmail(StaticValue.SYSTEM_CRAWLER_ACCOUNT)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));
	}

	/**
	 * Post 전송을 기다리는 최근 공지를 조회합니다.
	 *
	 * @return 전송 대기 공지 목록
	 */
	private List<CrawledNotice> getUpdatedNotices() {
		return crawledNoticeReader.findPendingNotices();
	}

	/**
	 * 크롤링 공지를 지정 게시판의 Post로 변환하거나 기존 Post에 반영합니다.
	 *
	 * @param notice 변환할 크롤링 공지
	 * @param board 저장 대상 게시판
	 * @param adminUser Post 작성자로 사용할 시스템 사용자
	 * @return 생성하거나 갱신한 Post
	 */
	private Post processUpdatedNotice(CrawledNotice notice, Board board, User adminUser) {
		String title = (notice.getTitle() == null || notice.getTitle().isBlank())
			? "제목 없음" : notice.getTitle();

		// Post 변환 시점에서 첨부파일 링크 추가
		String contentHtml = buildContentWithAttachmentsAndLink(notice, title);

		// 원본 HTML에서 이미지 URL 추출 (첨부파일 영역 추가 전 원본 기준)
		List<String> imageUrls = extractImageUrls(notice.getContent(), notice.getLink());

		// 제목으로 기존 게시글 조회
		Post existingPost = findExistingPost(notice, board, title);

		if (existingPost != null) {
			// 기존 Post 업데이트
			existingPost.update(title, contentHtml, existingPost.getForm(), existingPost.getPostAttachImageList());
			postWriter.save(existingPost);

			// 기존 크롤링 이미지 교체
			crawledPostImageWriter.deleteAllByPostId(existingPost.getId());
			savePostImages(existingPost, imageUrls);
			return existingPost;
		} else {
			// 새 Post 생성 (크롤링 게시판은 익명 게시판이 아니므로 isAnonymous=false)
			Post newPost = Post.of(
				title,
				contentHtml,
				adminUser,
				false,
				false,
				board,
				null,
				new ArrayList<>());
			newPost.setCrawled();
			postWriter.save(newPost);

			// 크롤링 이미지 저장
			savePostImages(newPost, imageUrls);

			// 새 게시글인 경우에만 알림 전송
			//			boardNotificationService.sendByBoardIsSubscribed(board, newPost);
			applicationEventPublisher.publishEvent(new OfficialPostEvent(board.getId(), newPost.getId(), title));
			return newPost;
		}
	}

	//크롤링 이미지 URL 목록을 CrawledPostImage 엔티티로 저장
	private void savePostImages(Post post, List<String> imageUrls) {
		if (imageUrls.isEmpty()) {
			return;
		}
		List<CrawledPostImage> images = new ArrayList<>();
		for (int i = 0; i < imageUrls.size(); i++) {
			images.add(CrawledPostImage.of(post, imageUrls.get(i), i));
		}
		crawledPostImageWriter.saveAll(images);
	}

	//HTML 본문에서 <img src="..."> URL 추출
	private List<String> extractImageUrls(String html, String baseUri) {
		if (html == null || html.isBlank()) {
			return List.of();
		}
		Document doc = Jsoup.parse(html, baseUri != null ? baseUri : "");
		Elements imgElements = doc.select("img[src]");
		return imgElements.stream()
			.map(img -> img.attr("abs:src").isBlank() ? img.attr("src") : img.attr("abs:src"))
			.filter(src -> !src.isBlank())
			.toList();
	}

	//HTML 본문에서 <img> 태그 및 의미없는 빈 <p> 태그 제거하여 반환
	private String cleanUpHtml(String html, String baseUri) {
		if (html == null || html.isBlank()) {
			return html;
		}
		Document doc = Jsoup.parse(html, baseUri != null ? baseUri : "");
		doc.select("img").remove();

		for (org.jsoup.nodes.Element p : doc.select("p")) {
			if (p.text().isBlank()) {
				p.remove();
			}
		}
		return doc.body().html();
	}

	//본문 내용에 첨부파일 링크를 추가하여 반환
	private String buildContentWithAttachmentsAndLink(CrawledNotice notice, String title) {
		StringBuilder contentBuilder = new StringBuilder();

		// 제목도 본문에 포함
		String safeTitle = Jsoup.clean(title, org.jsoup.safety.Safelist.none());
		contentBuilder.append("<p style='margin-bottom: 20px;'><strong>")
			.append(safeTitle)
			.append("</strong></p>");

		// 원본 HTML 내용 (이미지 태그 제거)
		String originalContent = (notice.getContent() == null || notice.getContent().isBlank())
			? "<p>내용 없음</p>" : cleanUpHtml(notice.getContent(), notice.getLink());
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

	/**
	 * 공지에 연결된 유효한 Post를 우선 조회하고, 연결이 없으면 제목으로 기존 Post를 찾습니다.
	 *
	 * @param notice 원본 크롤링 공지
	 * @param board 저장 대상 게시판
	 * @param title 조회할 Post 제목
	 * @return 기존 Post 또는 일치하는 Post가 없으면 {@code null}
	 */
	private Post findExistingPost(CrawledNotice notice, Board board, String title) {
		if (notice.getPost() != null
			&& !Boolean.TRUE.equals(notice.getPost().getIsDeleted())
			&& notice.getPost().getBoard() != null
			&& notice.getTargetBoardId().equals(notice.getPost().getBoard().getId())) {
			return notice.getPost();
		}

		// 기존 데이터가 최초로 새 식별 구조를 사용할 때만 제목으로 연결한다.
		List<Post> existingPosts = postReader.findAllByBoardAndNotDeleted(board);

		for (Post post : existingPosts) {
			if (post.getTitle() != null && post.getTitle().equals(title)) {
				return post;
			}
		}

		return null;
	}
}
