package net.causw.domain.model.enums.uuidFile;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FilePath {
    USER_PROFILE(
            "user/profile",
            5 * 1024 * 1024L, // 5MB
            1,
            List.of(FileExtensionType.IMAGE)
    ),
    USER_ADMISSION(
            "user/admission",
            5 * 1024 * 1024L, // 5MB
            5,
            List.of(FileExtensionType.IMAGE)
    ),
    USER_ACADEMIC_RECORD_APPLICATION(
            "user/academic-record-application",
            5 * 1024 * 1024L, // 5MB
            5,
            List.of(FileExtensionType.IMAGE)
    ),
    CIRCLE_PROFILE(
            "circle/profile",
            5 * 1024 * 1024L, // 5MB
            1,
            List.of(FileExtensionType.IMAGE)
    ),
    POST(
            "post",
            10 * 1024 * 1024L, // 10MB
            10,
            List.of(
                    FileExtensionType.IMAGE,
                    FileExtensionType.VIDEO,
                    FileExtensionType.AUDIO,
                    FileExtensionType.TEXT,
                    FileExtensionType.DOCUMENT,
                    FileExtensionType.COMPRESS,
                    FileExtensionType.ETC
            )
    ),
    CALENDAR("calendar",
            50 * 1024 * 1024L, // 50MB
            1,
            List.of(FileExtensionType.IMAGE)
    ),
    EVENT(
            "event",
            50 * 1024 * 1024L, // 50MB
            1,
            List.of(FileExtensionType.IMAGE)
    ),
    ETC(
            "etc",
            100 * 1024 * 1024L, // 100MB
            10,
            List.of(
                    FileExtensionType.IMAGE,
                    FileExtensionType.VIDEO,
                    FileExtensionType.AUDIO,
                    FileExtensionType.TEXT,
                    FileExtensionType.DOCUMENT,
                    FileExtensionType.COMPRESS,
                    FileExtensionType.ETC
            )
    );

    private final String directory;
    private final Long maxFileSize;
    private final Integer maxFileCount;
    private final List<FileExtensionType> fileExtensionList;
}
