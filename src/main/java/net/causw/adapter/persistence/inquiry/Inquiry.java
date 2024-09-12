package net.causw.adapter.persistence.inquiry;

import lombok.*;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter
@Setter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "TB_INQUIRY")
public class Inquiry extends BaseEntity {
    @Column(name = "title",nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", name = "content", nullable = false)
    private String content;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "user_id", nullable = false)
    private User writer;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    public static Inquiry of(
            String title,
            String content,
            User writer
    ) {
        return Inquiry.builder()
                .title(title)
                .content(content)
                .writer(writer)
                .isDeleted(false)
                .build();
    }
}
