package net.causw.app.main.domain.model.entity.uuidFile.joinEntity;

import jakarta.persistence.*;
import lombok.*;
import net.causw.app.main.domain.model.entity.ceremony.Ceremony;
import net.causw.app.main.domain.model.entity.uuidFile.UuidFile;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tb_ceremony_attach_image_uuid_file",
        indexes = {
                @Index(name = "idx_ceremony_attach_image_ceremony_id", columnList = "ceremony_id"),
                @Index(name = "idx_ceremony_attach_image_uuid_file_id", columnList = "uuid_file_id")
        })
public class CeremonyAttachImage extends JoinEntity {
    @Getter
    @Setter(AccessLevel.PUBLIC)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uuid_file_id", nullable = false, unique = true)
    public UuidFile uuidFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ceremony_id", nullable = false)
    private Ceremony ceremony;

    public static CeremonyAttachImage of(Ceremony ceremony, UuidFile uuidFile) {
        return CeremonyAttachImage.builder()
                .uuidFile(uuidFile)
                .ceremony(ceremony)
                .build();
    }
}

