package net.causw.adapter.persistence.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_event")
public class Event extends BaseEntity {
    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "image", nullable = false)
    private String image;

    @Setter
    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    public static Event of(
            String url,
            String image,
            Boolean isDeleted
    ) {
        return new Event(url, image, isDeleted);
    }

    public void update(String url, String image) {
        this.url = url;
        this.image = image;
    }
}
