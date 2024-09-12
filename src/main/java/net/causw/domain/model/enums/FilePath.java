package net.causw.domain.model.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum FilePath {
    USER_PROFILE("user/profile"),
    USER_ADMISSION("user/admission"),
    USER_ACADEMIC_RECORD_APPLICATION("user/academic-record-application"),
    CIRCLE_PROFILE("circle/profile"),
    POST("post"),
    CALENDAR("calendar"),
    EVENT("event"),
    ETC("etc");

    private final String directory;
}
