package net.causw.adapter.persistence.UuidFile;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.FileType;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "uuid_file")
public class UuidFile extends BaseEntity {

    @Column(name = "file_url", unique = true, nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "directory", nullable = false)
    private FileType fileType;

}
