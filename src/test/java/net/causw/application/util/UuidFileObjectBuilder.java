package net.causw.application.util;

import net.causw.adapter.persistence.base.BaseEntity;
import net.causw.adapter.persistence.uuidFile.UuidFile;
import net.causw.domain.model.enums.uuidFile.FilePath;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

public class UuidFileObjectBuilder extends UuidFile {

    public static UuidFile buildUuidFile(
            String id,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String uuid,
            String fileKey,
            String rawFileName,
            String extension,
            FilePath filePath,
            Boolean isUsed
    ) {
        UuidFile uuidFile = UuidFile.builder()
                .uuid(uuid)
                .fileKey(fileKey)
                .rawFileName(rawFileName)
                .extension(extension)
                .filePath(filePath)
                .isUsed(isUsed)
                .build();

        BaseEntityReflectionManager.setBaseEntityFields(
                uuidFile,
                id,
                createdAt,
                updatedAt
        );

        return uuidFile;
    }

}
