package net.causw.adapter.persistence.uuidFile;

import jakarta.persistence.*;
import lombok.*;
import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.domain.model.enums.FilePath;

@Getter
@Entity
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_uuid_file")
public class UuidFile extends BaseEntity {

    @Column(name = "uuid", unique = true, nullable = false)
    private String uuid;

    @Column(name = "file_key", unique = true, nullable = false)
    private String fileKey;

    @Column(name = "file_url", unique = true, nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_path", nullable = false)
    private FilePath filePath;

    public static UuidFile of(String uuid, String fileKey, String fileUrl, FilePath filePath) {
        return UuidFile.builder()
                .uuid(uuid)
                .fileKey(fileKey)
                .fileUrl(fileUrl)
                .filePath(filePath)
                .build();
    }

}
