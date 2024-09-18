package net.causw.adapter.persistence.crawled;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_attachment")
public class Attachment extends BaseEntity {
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_link", nullable = false)
    private String fileLink;
}
