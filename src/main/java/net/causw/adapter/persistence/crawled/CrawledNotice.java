package net.causw.adapter.persistence.crawled;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
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

    @Column(name = "link", nullable = false, unique = true)
    private String link;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "announce_date", nullable = false)
    private LocalDate announceDate;

    @Column(name = "image_link", nullable = true)
    private String imageLink;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "crawled_notice_id", nullable = true)
    @Builder.Default
    private List<CrawledFileLink> crawledFileLinks = new ArrayList<>();

    public static CrawledNotice of(
            String type,
            String title,
            String content,
            String link,
            String author,
            String announceDate,
            String imageLink,
            List<CrawledFileLink> crawledFileLinks
    ) {
        // String -> LocalDate
        LocalDate parsedDate = LocalDate.parse(announceDate, DateTimeFormatter.ISO_LOCAL_DATE);
        // 새로운 공지에 대한 처리
        if (type.contains("NEW")) {
            type = type.replace("NEW", "").trim();
        }

        return CrawledNotice.builder()
                .type(type)
                .title(title)
                .content(content)
                .link(link)
                .author(author)
                .announceDate(parsedDate)
                .imageLink(imageLink)
                .crawledFileLinks(crawledFileLinks)
                .build();
    }
}
