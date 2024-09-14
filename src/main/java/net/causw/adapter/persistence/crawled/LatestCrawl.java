package net.causw.adapter.persistence.crawled;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.CrawlCategory;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_latest_crawl")
public class LatestCrawl extends BaseEntity {
    @Column(name = "latest_url", nullable = false)
    private String latestUrl;

    @Enumerated(EnumType.STRING) // ENUM 타입을 문자열로 저장
    @Column(name = "crawl_category", nullable = false)
    private CrawlCategory crawlCategory;

    public static LatestCrawl of(
            String latestUrl,
            CrawlCategory crawlCategory
    ) {
        return LatestCrawl.builder()
                .latestUrl(latestUrl)
                .crawlCategory(crawlCategory)
                .build();
    }
}


