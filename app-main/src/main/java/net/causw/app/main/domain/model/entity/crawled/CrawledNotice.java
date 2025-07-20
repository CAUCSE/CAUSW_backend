package net.causw.app.main.domain.model.entity.crawled;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.base.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.causw.global.util.HashUtil;

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

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;

    @Column(name = "is_updated", nullable = false)
    private Boolean isUpdated;

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

        // 본문 내용의 해시값 생성
        String contentHash = generateContentHash(title, content, imageLink, crawledFileLinks);

        return CrawledNotice.builder()
                .type(type)
                .title(title)
                .content(content)
                .link(link)
                .author(author)
                .announceDate(parsedDate)
                .imageLink(imageLink)
                .crawledFileLinks(crawledFileLinks)
                .contentHash(contentHash)
                .lastModified(LocalDateTime.now())
                .isUpdated(false)
                .build();
    }

    //제목, 본문, 내용, 첨부파일로 해시 값 생성
    private static String generateContentHash(String title, String content, String imageLink, List<CrawledFileLink> fileLinks) {
        StringBuilder hashInput = new StringBuilder();
        hashInput.append(title != null ? title : "");
        hashInput.append(content != null ? content : "");
        hashInput.append(imageLink != null ? imageLink : "");
        
        if (fileLinks != null) {
            for (CrawledFileLink fileLink : fileLinks) {
                hashInput.append(fileLink.getFileName()).append(fileLink.getFileLink());
            }
        }
        
        return HashUtil.generateSHA256(hashInput.toString());
    }

    //내용 업데이트 메서드
    public void updateContent(String newTitle, String newContent, String newContentHash) {
        this.title = newTitle;
        this.content = newContent;
        this.contentHash = newContentHash;
        this.lastModified = LocalDateTime.now();
        this.isUpdated = true;
    }

    public void setIsUpdated(boolean isUpdated) {
        this.isUpdated = isUpdated;
    }
}
