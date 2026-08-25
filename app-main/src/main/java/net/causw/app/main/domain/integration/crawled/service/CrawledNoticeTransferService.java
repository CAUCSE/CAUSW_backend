package net.causw.app.main.domain.integration.crawled.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.causw.app.main.domain.community.board.entity.Board;
import net.causw.app.main.domain.community.board.service.implementation.BoardReader;
import net.causw.app.main.domain.community.post.entity.Post;
import net.causw.app.main.domain.community.post.service.implementation.PostWriter;
import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.entity.CrawledPostImage;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeWriter;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledPostImageWriter;
import net.causw.app.main.domain.notification.notification.event.OfficialPostEvent;
import net.causw.app.main.domain.user.account.entity.user.User;
import net.causw.app.main.domain.user.account.service.implementation.UserReader;
import net.causw.app.main.shared.exception.errorcode.IntegrationErrorCode;
import net.causw.global.constant.StaticValue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
/**
 * 하나의 크롤링 공지를 대상 게시판의 Post로 영속화하는 서비스입니다.
 *
 * <p>기존 Post 갱신, 이미지 동기화, 원본 공지와 Post의 연결 및 전송 완료 처리를
 * 하나의 독립 트랜잭션으로 수행합니다.</p>
 */
@Service
public class CrawledNoticeTransferService {
	private final CrawledNoticeReader crawledNoticeReader;
	private final CrawledNoticeWriter crawledNoticeWriter;
	private final PostWriter postWriter;
	private final UserReader userReader;
	private final BoardReader boardReader;
	private final CrawledPostImageWriter crawledPostImageWriter;
	private final ApplicationEventPublisher applicationEventPublisher;

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
		Post existingPost = findExistingPost(notice);
		if (existingPost == null && !LocalDate.now().equals(notice.getAnnounceDate())) {
			crawledNoticeWriter.markTransferred(notice);
			return;
		}
		User systemUser = getSystemUser();
		Board board = boardReader.getById(notice.getTargetBoardId());
		Post post = processUpdatedNotice(notice, board, systemUser, existingPost);
		crawledNoticeWriter.markTransferred(notice, post);
	}

	/**
	 * 크롤링 작업에 사용하는 시스템 계정을 조회합니다.
	 *
	 * @return 크롤링 시스템 계정
	 * @throws net.causw.app.main.shared.exception.BaseRunTimeV2Exception 시스템 계정이 존재하지 않는 경우
	 */
	private User getSystemUser() {
		return userReader.findByEmail(StaticValue.SYSTEM_CRAWLER_ACCOUNT)
			.orElseThrow(IntegrationErrorCode.CRAWL_SYSTEM_USER_NOT_FOUND::toBaseException);
	}

	/**
	 * 기존 연결 Post를 갱신하거나 대상 게시판에 새 Post를 생성합니다.
	 *
	 * <p>이미지가 포함된 경우 이미지 연결 정보를 현재 크롤링 결과와 동기화하며,
	 * 새 Post를 생성한 경우에만 공식 게시글 알림 이벤트를 발행합니다.</p>
	 *
	 * @param notice 전송할 크롤링 공지
	 * @param board 게시글을 저장할 대상 게시판
	 * @param adminUser 게시글 작성자로 사용할 시스템 계정
	 * @param existingPost 기존에 연결된 Post. 없으면 새 Post를 생성합니다.
	 * @return 생성하거나 갱신한 Post
	 */
	private Post processUpdatedNotice(CrawledNotice notice, Board board, User adminUser, Post existingPost) {
		String title = (notice.getTitle() == null || notice.getTitle().isBlank())
			? "제목 없음" : notice.getTitle();
		String contentHtml = (notice.getContent() == null || notice.getContent().isBlank())
			? "<p>내용 없음</p>" : cleanUpHtml(notice.getContent(), notice.getLink());
		List<String> imageUrls = extractImageUrls(notice.getContent(), notice.getLink());
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
		postWriter.save(newPost);
		savePostImages(newPost, imageUrls);
		applicationEventPublisher.publishEvent(new OfficialPostEvent(board.getId(), newPost.getId(), title));
		return newPost;
	}

	/**
	 * 게시글에 연결할 크롤링 이미지 엔티티를 이미지 URL 순서대로 저장합니다.
	 *
	 * @param post 이미지를 연결할 게시글
	 * @param imageUrls 저장할 이미지 URL 목록
	 */
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

	/**
	 * 공지 본문의 이미지 태그에서 절대 또는 원본 상대 경로의 이미지 URL을 추출합니다.
	 *
	 * @param html 원본 HTML
	 * @param baseUri 상대 URL을 해석할 원본 공지 URL
	 * @return 비어 있지 않은 이미지 URL 목록
	 */
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

	/**
	 * 본문에서 이미지와 빈 문단을 제거해 Post 본문에 사용할 HTML을 만듭니다.
	 *
	 * @param html 원본 HTML
	 * @param baseUri 상대 URL을 해석할 원본 공지 URL
	 * @return 정리된 HTML. 입력이 비어 있으면 입력값을 그대로 반환합니다.
	 */
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

	/**
	 * 공지에 연결된 Post가 현재 대상 게시판에서 유효한지 확인합니다.
	 *
	 * <p>연결된 Post가 삭제되었거나 대상 게시판이 변경되었으면 새 Post를 생성할 수 있도록
	 * {@code null}을 반환합니다. 게시판이 변경된 경우 기존 Post는 삭제 처리합니다.</p>
	 *
	 * @param notice 기존 연결 Post를 확인할 크롤링 공지
	 * @return 유효한 연결 Post. 없거나 유효하지 않으면 {@code null}
	 */
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
