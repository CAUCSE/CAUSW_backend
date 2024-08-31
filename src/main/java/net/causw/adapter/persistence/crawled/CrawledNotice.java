package net.causw.adapter.persistence.crawled;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_crawled_notice")
public class CrawledNotice extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", name = "content", nullable = false)
    private String content;

    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "notice_date", nullable = false)
    private String noticeDate;

    public static CrawledNotice of(
            String title,
            String content,
            String link,
            String author,
            String noticeDate
    ) {
        return new CrawledNotice(title, content, link, author, noticeDate);
    }
}
