package net.causw.adapter.persistence.event;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "tb_event")
public class Event extends BaseEntity {
    @Column(name = "url", nullable = false)
    private String url;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "uuid_file_id", nullable = false)
    private UuidFile uuidFile;

    @Setter
    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    public static Event of(
            String url,
            UuidFile uuidFile,
            Boolean isDeleted
    ) {
        return new Event(url, uuidFile, isDeleted);
    }

    public void update(String url, UuidFile uuidFile) {
        this.url = url;
        this.uuidFile = uuidFile;
    }
}
