package net.causw.adapter.persistence.crawled;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_crawled_file_link")
public class CrawledFileLink extends BaseEntity {
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_link", nullable = false)
    private String fileLink;

    public static CrawledFileLink of(
            String fileName,
            String fileLink
    ) {
        return CrawledFileLink.builder()
                .fileName(fileName)
                .fileLink(fileLink)
                .build();
    }
}
