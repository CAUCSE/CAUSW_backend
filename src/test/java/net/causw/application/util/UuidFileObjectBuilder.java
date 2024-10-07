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

        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(uuidFile, id);

            Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(uuidFile, createdAt);

            Field updatedAtField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(uuidFile, updatedAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return uuidFile;
    }

}
