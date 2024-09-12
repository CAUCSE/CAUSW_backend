package net.causw.adapter.persistence.inquiry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.causw.adapter.persistence.user.User;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.inquiry.InquiryDomainModel;
import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter
@Setter
@Entity
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

    private Inquiry(
            String id,
            String title,
            String content,
            User writer,
            Boolean isDeleted
    ){
        super(id);
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.isDeleted = isDeleted;
    }

    public static Inquiry of(
            String title,
            String content,
            User writer
    ) {
        Inquiry inquiry = new Inquiry();
        inquiry.setTitle(title);
        inquiry.setContent(content);
        inquiry.setWriter(writer);
        inquiry.setIsDeleted(false);
        return inquiry;
    }
}
