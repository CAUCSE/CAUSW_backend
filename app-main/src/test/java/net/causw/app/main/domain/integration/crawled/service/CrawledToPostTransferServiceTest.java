package net.causw.app.main.domain.integration.crawled.service;

import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.causw.app.main.domain.integration.crawled.entity.CrawledNotice;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeReader;
import net.causw.app.main.domain.integration.crawled.service.implementation.CrawledNoticeTransferProcessor;

@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledToPostTransferService 테스트")
class CrawledToPostTransferServiceTest {

	@InjectMocks
	private CrawledToPostTransferService crawledToPostTransferService;

	@Mock
	private CrawledNoticeReader crawledNoticeReader;

	@Mock
	private CrawledNoticeTransferProcessor crawledNoticeTransferProcessor;

	@Test
	@DisplayName("전송 대기 공지를 Processor에 위임한다")
	void transferToPosts_delegatesPendingNoticesToProcessor() {
		// given
		CrawledNotice firstNotice = notice("notice-1");
		CrawledNotice secondNotice = notice("notice-2");
		given(crawledNoticeReader.findPendingNotices()).willReturn(List.of(firstNotice, secondNotice));

		// when
		crawledToPostTransferService.transferToPosts();

		// then
		verify(crawledNoticeTransferProcessor).transfer("notice-1");
		verify(crawledNoticeTransferProcessor).transfer("notice-2");
	}

	@Test
	@DisplayName("한 공지 처리에 실패해도 다음 공지를 계속 처리한다")
	void transferToPosts_continuesWhenOneNoticeTransferFails() {
		// given
		CrawledNotice failedNotice = notice("notice-1");
		CrawledNotice nextNotice = notice("notice-2");
		given(crawledNoticeReader.findPendingNotices()).willReturn(List.of(failedNotice, nextNotice));
		given(failedNotice.getSiteId()).willReturn("site-id");
		given(failedNotice.getExternalId()).willReturn("external-id");
		doThrow(new IllegalStateException()).when(crawledNoticeTransferProcessor).transfer("notice-1");

		// when
		crawledToPostTransferService.transferToPosts();

		// then
		verify(crawledNoticeTransferProcessor).transfer("notice-1");
		verify(crawledNoticeTransferProcessor).transfer("notice-2");
	}

	private CrawledNotice notice(String id) {
		CrawledNotice notice = mock(CrawledNotice.class);
		given(notice.getId()).willReturn(id);
		return notice;
	}
}
