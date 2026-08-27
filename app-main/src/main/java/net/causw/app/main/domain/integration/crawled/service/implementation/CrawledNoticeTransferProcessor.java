package net.causw.app.main.domain.integration.crawled.service.implementation;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.community.post.util.PostCategoryClassifier;
import net.causw.app.main.domain.integration.crawled.entity.CrawledFileLink;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.entity.CrawledPostImage;
import net.causw.app.main.domain.notification.notification.event.OfficialPostEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.global.constant.MessageUtil;
import net.causw.global.constant.StaticValue;
import net.causw.global.exception.BadRequestException;
import net.causw.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CrawledNoticeTransferProcessor {
	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeWriter crawledNoticeWriter;
	private final PostWriter postWriter;
	private final UserReader userReader;
	private final BoardReader boardReader;
	private final CrawledPostImageWriter crawledPostImageWriter;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final PostCategoryClassifier postCategoryClassifier;

	/**
	 * 하나의 크롤링 공지를 Post로 생성하거나 갱신하고 전송 완료 상태로 변경합니다.
	 *
	 * <p>각 공지의 처리 결과를 독립적으로 확정하거나 롤백하기 위해 새 트랜잭션에서 실행합니다.</p>
	 *
	 * @param noticeId 전송할 크롤링 공지 식별자
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void transfer(String noticeId) {
		CrawledNotice notice = crawledNoticeReader.findById(noticeId);
		User systemUser = getSystemUser();
		Board board = boardReader.getById(notice.getTargetBoardId());
		Post post = processUpdatedNotice(notice, board, systemUser);
		crawledNoticeWriter.markTransferred(notice, post);
	}

	private User getSystemUser() {
		return userReader.findByEmail(StaticValue.SYSTEM_CRAWLER_ACCOUNT)
			.orElseThrow(() -> new BadRequestException(
				ErrorCode.ROW_DOES_NOT_EXIST, MessageUtil.USER_NOT_FOUND));
	}

	private Post processUpdatedNotice(CrawledNotice notice, Board board, User adminUser) {
		String title = (notice.getTitle() == null || notice.getTitle().isBlank())
			? "제목 없음" : notice.getTitle();
		String contentHtml = buildContentWithAttachmentsAndLink(notice, title);
		List<String> imageUrls = extractImageUrls(notice.getContent(), notice.getLink());
		Post existingPost = findExistingPost(notice);

		if (existingPost != null) {
			existingPost.update(title, contentHtml, existingPost.getIsAnonymous(),
				existingPost.getPostAttachImageList());
			postWriter.save(existingPost);
			crawledPostImageWriter.deleteAllByPostId(existingPost.getId());
			savePostImages(existingPost, imageUrls);
			return existingPost;
		}

		Post newPost = Post.of(title, contentHtml, adminUser, false, false, board, null, new ArrayList<>());
		newPost.setCrawled();
		// 성격 자동 분류 (갱신 시에는 관리자 수동 지정 보존을 위해 미적용)
		newPost.updateCategory(postCategoryClassifier.classify(notice.getTitle()));
		postWriter.save(newPost);
		savePostImages(newPost, imageUrls);
		applicationEventPublisher.publishEvent(new OfficialPostEvent(board.getId(), newPost.getId(), title));
		return newPost;
	}

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

	private String buildContentWithAttachmentsAndLink(CrawledNotice notice, String title) {
		Document document = Jsoup.parseBodyFragment("");
		Element body = document.body();
		body.appendElement("p")
			.attr("style", "margin-bottom: 20px;")
			.appendElement("strong")
			.text(title);

		String originalContent = (notice.getContent() == null || notice.getContent().isBlank())
			? "<p>내용 없음</p>" : cleanUpHtml(notice.getContent(), notice.getLink());
		body.append(originalContent);

		if (notice.getCrawledFileLinks() != null && !notice.getCrawledFileLinks().isEmpty()) {
			body.appendElement("hr").attr("style", "margin: 20px 0; border: 1px solid #eee;");
			Element attachmentSection = body.appendElement("div")
				.attr("style", "margin-top: 20px; padding: 15px; background-color: #f8f9fa; border-radius: 5px;");
			attachmentSection.appendElement("h4")
				.attr("style", "margin: 0 0 10px 0; color: #495057;")
				.text("📎 첨부파일");
			Element attachmentList = attachmentSection.appendElement("ul")
				.attr("style", "margin: 0; padding-left: 20px;");

			for (CrawledFileLink fileLink : notice.getCrawledFileLinks()) {
				Element fileLinkElement = buildExternalLink(document, fileLink.getFileLink(),
					"📄 " + fileLink.getFileName());
				if (fileLinkElement != null) {
					attachmentList.appendElement("li")
						.attr("style", "margin-bottom: 5px;")
						.appendChild(fileLinkElement);
				}
			}
		}

		body.appendElement("hr").attr("style", "margin: 20px 0; border: 1px solid #eee;");
		Element sourceInfo = body.appendElement("div")
			.attr("style",
				"margin-top: 15px; padding: 10px; background-color: #f1f3f4; border-radius: 5px; font-size: 14px; color: #666;");
		sourceInfo.appendText("🔗 ");
		sourceInfo.appendElement("strong").text("원본 공지사항:");
		Element sourceLink = buildExternalLink(document, notice.getLink(), StaticValue.ORIGINAL_NOTICE_SITE_NAME);
		if (sourceLink != null) {
			sourceInfo.appendText(" ").appendChild(sourceLink)
				.attr("style", "color: #1a73e8; text-decoration: none;");
		}

		return body.html();
	}

	private Element buildExternalLink(Document document, String url, String text) {
		if (!isHttpOrHttpsUrl(url)) {
			return null;
		}
		return document.createElement("a")
			.attr("href", url.trim())
			.attr("target", "_blank")
			.attr("rel", "noopener noreferrer")
			.attr("style", "color: #007bff; text-decoration: none;")
			.text(text == null ? "" : text);
	}

	private boolean isHttpOrHttpsUrl(String url) {
		if (url == null || url.isBlank()) {
			return false;
		}
		try {
			String scheme = java.net.URI.create(url.trim()).getScheme();
			return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private Post findExistingPost(CrawledNotice notice) {
		Post linkedPost = notice.getPost();
		if (linkedPost != null && !Boolean.TRUE.equals(linkedPost.getIsDeleted())) {
			if (linkedPost.getBoard() != null && notice.getTargetBoardId().equals(linkedPost.getBoard().getId())) {
				return linkedPost;
			}
			linkedPost.setIsDeleted(true);
			postWriter.save(linkedPost);
		}

		return null;
	}
}
