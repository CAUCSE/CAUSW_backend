package net.causw.adapter.persistence.crawled;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_crawled_notice")
public class CrawledNotice extends BaseEntity {
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", name = "content", nullable = false)
    private String content;

    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "announce_date", nullable = false)
    private String announceDate;

    public static CrawledNotice of(
            String type,
            String title,
            String content,
            String link,
            String author,
            String announceDate
    ) {
        return new CrawledNotice(type, title, content, link, author, announceDate);
    }
}
