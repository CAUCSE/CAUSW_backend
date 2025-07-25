package net.causw.app.main.service.crawler;

import net.causw.app.main.domain.model.entity.crawled.CrawledNotice;
import java.time.LocalDate;
import java.util.Collections;

public class CrawledNoticeFixture {
    
    // 새 공지사항
    public static CrawledNotice newNotice() {
        CrawledNotice notice = CrawledNotice.of(
            "공지사항",
            "새 공지사항",
            "새로운 내용입니다.",
            "https://example.com/new",
            "관리자",
            LocalDate.now().toString(),
            null,
            Collections.emptyList()
        );
        notice.setIsUpdated(true);
        return notice;
    }
    
    // 수정된 공지사항 (크롤링된 버전)
    public static CrawledNotice updatedNotice() {
        return CrawledNotice.of(
            "공지사항",
            "수정된 공지사항",
            "수정된 내용입니다.",
            "https://example.com/updated",
            "관리자",
            LocalDate.now().toString(),
            null,
            Collections.emptyList()
        );
    }
    
    // 기존 공지사항 (DB에 저장된 버전)
    public static CrawledNotice existingNotice() {
        CrawledNotice notice = CrawledNotice.of(
            "공지사항",
            "기존 공지사항",
            "기존 내용입니다.",
            "https://example.com/updated", // 같은 링크
            "관리자",
            LocalDate.now().toString(),
            null,
            Collections.emptyList()
        );
        notice.setIsUpdated(false);
        // 기존 해시 설정 (다른 해시로 설정하여 변경 감지)
        notice.updateContent("기존 공지사항", "기존 내용입니다.", "oldContentHash");
        return notice;
    }
} 